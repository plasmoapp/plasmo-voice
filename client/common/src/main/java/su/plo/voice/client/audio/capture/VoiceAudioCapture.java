package su.plo.voice.client.audio.capture;

import com.google.common.base.Strings;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.audio.codec.CodecException;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.AudioCapture;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.api.client.audio.device.DeviceFactory;
import su.plo.voice.api.client.audio.device.InputDevice;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.client.event.audio.capture.AudioCaptureEvent;
import su.plo.voice.api.client.socket.UdpClient;
import su.plo.voice.api.client.util.AudioUtil;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.api.encryption.EncryptionException;
import su.plo.voice.api.util.Params;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.config.capture.ConfigClientActivation;
import su.plo.voice.proto.data.capture.Activation;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerAudioEndPacket;
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket;

import javax.sound.sampled.AudioFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

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

    private ClientActivation proximityActivation;

    private final ListMultimap<Activation.Order, ClientActivation> activations = Multimaps.synchronizedListMultimap(
            Multimaps.newListMultimap(
                    Maps.newEnumMap(Activation.Order.class), CopyOnWriteArrayList::new
            )
    );
    private Map<UUID, ClientActivation> activationById = Maps.newConcurrentMap();

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
    public @NotNull Collection<ClientActivation> getActivations() {
        return activationById.values();
    }

    @Override
    public void registerActivation(@NotNull ClientActivation activation) {
        activations.put(activation.getOrder(), activation);
        activationById.put(activation.getId(), activation);
    }

    @Override
    public void unregisterActivation(@NotNull ClientActivation activation) {
        if (activations.remove(activation.getOrder(), activation)) {
            activationById.remove(activation.getId());
        }
    }

    @Override
    public void unregisterActivation(@NotNull UUID activationId) {
        ClientActivation activation = activationById.remove(activationId);
        if (activation != null) activations.remove(activation.getOrder(), activation);
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

        // initialize proximity activation
        Optional<ClientConfig.Server> serverConfig = config.getServers().getById(serverInfo.getServerId());
        if (!serverConfig.isPresent()) throw new IllegalStateException("Server config is empty");

        ConfigClientActivation proximityConfig = serverConfig.get().getProximityActivation(
                serverInfo.getVoiceInfo().getProximityActivation()
        );
        // set global proximity activation type
        proximityConfig.getConfigType().set(config.getVoice().getActivationType().value());
        proximityConfig.getConfigType().setDefault(config.getVoice().getActivationType().value());
        if (proximityConfig.getConfigType().value() == ClientActivation.Type.INHERIT) {
            LOGGER.warn("Proximity activation type cannot be INHERIT. Changed to PUSH_TO_TALK");
            proximityConfig.getConfigType().set(ClientActivation.Type.PUSH_TO_TALK);
        }

        this.proximityActivation = new VoiceClientActivation(
                config,
                proximityConfig,
                serverInfo.getVoiceInfo().getProximityActivation()
        );

        // register custom activations
        for (Activation serverActivation : serverInfo.getVoiceInfo().getActivations()) {
            ClientActivation activation = new VoiceClientActivation(
                    config,
                    serverConfig.get().getActivation(serverActivation.getId(), serverActivation),
                    serverActivation
            );

            registerActivation(activation);
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

                ClientActivation.Result result = proximityActivation.process(samples);
                byte[] encoded = processActivation(proximityActivation, result, samples, null);

                for (ClientActivation activation : activations.values()) {
                    if (activation.getType() == ClientActivation.Type.INHERIT ||
                            activation.getType() == ClientActivation.Type.VOICE) {
                        encoded = processActivation(activation, result, samples, encoded);
                        continue;
                    }

                    encoded = processActivation(activation, activation.process(samples), samples, encoded);
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

    private byte[] processActivation(ClientActivation activation, ClientActivation.Result result, short[] samples, byte[] encoded) {
        if (encoded == null) {
            encoded = encode(samples);
        }

        if (result == ClientActivation.Result.ACTIVATED) {
            sendVoicePacket(activation, encoded);
        } else if (result == ClientActivation.Result.END) {
            sendVoicePacket(activation, encoded);
            sendVoiceEndPacket(activation);
        }

        return encoded;
    }

    private byte[] encode(short[] samples) {
        byte[] encoded;
        if (encoder != null) {
            try {
                encoded = encoder.encode(samples);
            } catch (CodecException e) {
                LOGGER.error("Failed to encode audio data", e);
                return null;
            }
        } else {
            encoded = AudioUtil.shortsToBytes(samples);
        }

        if (encryption != null) {
            try {
                encoded = encryption.encrypt(encoded);
            } catch (EncryptionException e) {
                LOGGER.error("Failed to encrypt audio data", e);
                return null;
            }
        }

        return encoded;
    }

    private void sendVoicePacket(ClientActivation activation, byte[] encoded) {
        Optional<UdpClient> udpClient = voiceClient.getUdpClientManager().getClient();
        if (!udpClient.isPresent()) return;

        udpClient.get().sendPacket(new PlayerAudioPacket(
                sequenceNumber++,
                encoded,
                (short) activation.getDistance()
        ));
    }

    private void sendVoiceEndPacket(ClientActivation activation) {
        if (encoder != null) encoder.reset();

        Optional<ServerConnection> connection = voiceClient.getServerConnection();
        if (!connection.isPresent()) return;


        connection.get().sendPacket(new PlayerAudioEndPacket(
                sequenceNumber++,
                (short) activation.getDistance()
        ));
    }
}
