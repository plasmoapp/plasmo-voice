package su.plo.voice.client.audio.capture;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.audio.codec.CodecException;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.AudioCapture;
import su.plo.voice.api.client.audio.device.DeviceFactory;
import su.plo.voice.api.client.audio.device.InputDevice;
import su.plo.voice.api.client.config.keybind.KeyBinding;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.client.event.audio.capture.AudioCaptureEvent;
import su.plo.voice.api.client.socket.UdpClient;
import su.plo.voice.api.client.util.AudioUtil;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.api.encryption.EncryptionException;
import su.plo.voice.api.util.Params;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerAudioEndPacket;
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket;

import javax.sound.sampled.AudioFormat;
import java.util.Optional;

public class VoiceAudioCapture implements AudioCapture {

    private static final Logger LOGGER = LogManager.getLogger();

    private final PlasmoVoiceClient voiceClient;
    private final ClientConfig config;

    @Setter
    private volatile AudioEncoder encoder;
    @Setter
    private volatile Encryption encryption;
    @Setter
    private volatile InputDevice device;
    @Getter
    @Setter
    private volatile Activation activation;

    private Thread thread;

    private long sequenceNumber;

    public VoiceAudioCapture(@NotNull PlasmoVoiceClient voiceClient,
                             @NotNull ClientConfig config) {
        this.voiceClient = voiceClient;
        this.config = config;
    }

    @Override
    public Optional<AudioEncoder> getEncoder() {
        return Optional.ofNullable(encoder);
    }

    @Override
    public Optional<Encryption> getEncryption() {
        return Optional.ofNullable(encryption);
    }

    @Override
    public Optional<InputDevice> getDevice() {
        return Optional.ofNullable(device);
    }

    @Override
    public void initialize(@NotNull ServerInfo serverInfo) {
        // initialize input device
        AudioFormat format = new AudioFormat(
                (float) serverInfo.getVoiceInfo().getSampleRate(),
                16,
                1,
                true,
                false
        );

        int bufferSize = (serverInfo.getVoiceInfo().getSampleRate() / 1_000) * 20;

        if (config.getVoice().getUseJavaxInput().value()) {
            try {
                openJavaxDevice(format);
            } catch (Exception e) {
                LOGGER.error("Failed to open Javax input device", e);
                return;
            }
        } else {
            try {
                openAlDevice(format);
            } catch (Exception e) {
                LOGGER.error("Failed to open OpenAL input device, falling back to Javax input device", e);

                try {
                    openJavaxDevice(format);
                } catch (Exception ex) {
                    LOGGER.error("Failed to open Javax input device", ex);
                    return;
                }
            }
        }

        // initialize encoder
        if (Strings.emptyToNull(serverInfo.getVoiceInfo().getCodec()) != null) {
            this.encoder = voiceClient.getCodecManager().createEncoder(
                    serverInfo.getVoiceInfo().getCodec(),
                    Params.builder()
                            .set("sampleRate", serverInfo.getVoiceInfo().getSampleRate())
                            .set("bufferSize", bufferSize)
                            .set("application", 2048) // todo: configurable?
                            .build()
            );
        }

        // initialize encryption
        if (serverInfo.getEncryption().isPresent()) {
            this.encryption = serverInfo.getEncryption().get();
        }

        KeyBinding pttKeyBinding = config.getKeyBindings().getKeyBinding("key.plasmo_voice.ptt").get();
        KeyBinding priorityKeyBinding = config.getKeyBindings().getKeyBinding("key.plasmo_voice.priority_ptt").get();

        // initialize activation
        if (config.getVoice().getVoiceActivation().value()) {
            this.activation = new VoiceActivation(
                    voiceClient,
                    config.getVoice(),
                    priorityKeyBinding
            );
        } else {
            this.activation = new PushToTalkActivation(
                    voiceClient,
                    pttKeyBinding,
                    priorityKeyBinding
            );
        }

        LOGGER.info("Audio capture initialized");
    }

    private void openAlDevice(@NotNull AudioFormat format) throws Exception {
        Optional<DeviceFactory> alFactory = voiceClient.getDeviceFactoryManager().getDeviceFactory("AL_INPUT");
        if (!alFactory.isPresent()) throw new IllegalStateException("OpenAL input factory is not registered");

        this.device = (InputDevice) alFactory.get().openDevice(format, Strings.emptyToNull(config.getVoice().getInputDevice().value()), Params.EMPTY).get();
    }

    private void openJavaxDevice(@NotNull AudioFormat format) throws Exception {
        Optional<DeviceFactory> alFactory = voiceClient.getDeviceFactoryManager().getDeviceFactory("JAVAX_INPUT");
        if (!alFactory.isPresent()) throw new IllegalStateException("Javax input factory is not registered");

        this.device = (InputDevice) alFactory.get().openDevice(format, Strings.emptyToNull(config.getVoice().getInputDevice().value()), Params.EMPTY).get();
    }

    @Override
    public void start() {
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                return;
            }
        }

        this.thread = new Thread(this::run);
        thread.setName("Voice Audio Capture");
        thread.start();
    }

    @Override
    public void stop() {
        thread.interrupt();
    }

    @Override
    public boolean isActive() {
        return thread != null;
    }

    private void run() {
        while (!thread.isInterrupted()) {
            try {
                if (!device.isOpen() || !voiceClient.getServerInfo().isPresent()) {
                    Thread.sleep(1_000L);
                    continue;
                }

                device.start();
                short[] samples = device.read();
                if (samples == null) {
                    Thread.sleep(5L);
                    continue;
                }

                AudioCaptureEvent captureEvent = new AudioCaptureEvent(this, samples);
                voiceClient.getEventBus().call(captureEvent);
                if (captureEvent.isCancelled()) continue;

                Activation.Result result = activation.process(samples);
                if (result == Activation.Result.ACTIVATED) {
                    sendVoicePacket(samples);
                } else if (result == Activation.Result.END) {
                    sendVoicePacket(samples);
                    sendVoiceEndPacket();
                }
            } catch (InterruptedException ignored) {
                break;
            }
        }

        cleanup();
    }

    private void cleanup() {
        this.sequenceNumber = 0L;
        if (encoder != null) encoder.close();
        if (device.isOpen()) {
            device.close();
        }

        this.thread = null;
    }

    private void sendVoicePacket(short[] samples) {
        Optional<UdpClient> udpClient = voiceClient.getUdpClientManager().getClient();
        if (!udpClient.isPresent()) return;

        short distance = getSendDistance();
        if (distance <= 0) return;

        byte[] encoded;
        if (encoder != null) {
            try {
                encoded = encoder.encode(samples);
            } catch (CodecException e) {
                LOGGER.error("Failed to encode audio data", e);
                return;
            }
        } else {
            encoded = AudioUtil.shortsToBytes(samples);
        }

        if (encryption != null) {
            try {
                encoded = encryption.encrypt(encoded);
            } catch (EncryptionException e) {
                LOGGER.error("Failed to encrypt audio data", e);
                return;
            }
        }

        udpClient.get().sendPacket(new PlayerAudioPacket(
                sequenceNumber++,
                encoded,
                distance
        ));
    }

    private void sendVoiceEndPacket() {
        if (encoder != null) encoder.reset();

        Optional<ServerConnection> connection = voiceClient.getServerConnection();
        if (!connection.isPresent()) return;

        short distance = getSendDistance();
        if (distance <= 0) return;

        connection.get().sendPacket(new PlayerAudioEndPacket(sequenceNumber++, distance));
    }

    private short getSendDistance() {
        Optional<ServerInfo> serverInfo = voiceClient.getServerInfo();
        if (!serverInfo.isPresent()) return 0;

        Optional<ClientConfig.Server> configServer = config.getServers().getById(serverInfo.get().getServerId());
        if (!configServer.isPresent()) return 0;

        return activation.isActivePriority() ?
                configServer.get().getPriorityDistance().value().shortValue()
                : configServer.get().getDistance().value().shortValue();
    }
}
