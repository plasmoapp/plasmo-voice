package su.plo.voice.client.audio.capture;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.AudioCapture;
import su.plo.voice.api.client.audio.device.InputDevice;
import su.plo.voice.api.client.event.audio.capture.AudioCaptureEvent;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.api.event.EventBus;
import su.plo.voice.client.config.ClientConfig;

import java.util.Optional;

public class VoiceAudioCapture implements AudioCapture {

    private final PlasmoVoiceClient voiceClient;
    private final EventBus eventBus;
    private final ClientConfig config;

    @Setter
    private volatile AudioEncoder<byte[], short[]> encoder;
    @Setter
    private volatile Encryption encryption;
    @Setter
    private volatile InputDevice device;
    @Getter
    @Setter
    private volatile Activation activation;

    private Thread thread;

    public VoiceAudioCapture(@NotNull PlasmoVoiceClient voiceClient,
                             @NotNull ClientConfig config,
                             @NotNull EventBus eventBus,
                             @Nullable AudioEncoder<byte[], short[]> encoder,
                             @Nullable Encryption encryption,
                             @Nullable InputDevice device,
                             @NotNull Activation activation) {
        this.voiceClient = voiceClient;
        this.config = config;
        this.eventBus = eventBus;
        this.encoder = encoder;
        this.encryption = encryption;
        this.device = device;
        this.activation = activation;
    }

    @Override
    public Optional<AudioEncoder<?, ?>> getEncoder() {
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

    private void run() {
        while (!thread.isInterrupted()) {
            try {
                if (!device.isOpen() && !voiceClient.getCurrentServerInfo().isPresent()) {
                    Thread.sleep(1_000L);
                    continue;
                }

                short[] samples = device.read();
                if (samples == null) {
                    Thread.sleep(5L);
                    continue;
                }

                AudioCaptureEvent captureEvent = new AudioCaptureEvent(this, samples);
                eventBus.call(captureEvent);
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
    }

    private void sendVoicePacket(short[] samples) {
    }

    private void sendVoiceEndPacket() {

    }
}
