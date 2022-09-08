package su.plo.voice.client.audio.device;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.AudioDevice;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.device.DeviceType;
import su.plo.voice.api.client.audio.device.InputDevice;
import su.plo.voice.api.client.event.audio.device.DeviceClosedEvent;
import su.plo.voice.api.client.event.audio.device.DeviceOpenEvent;
import su.plo.voice.api.client.event.audio.device.DevicePreOpenEvent;
import su.plo.voice.api.util.AudioUtil;
import su.plo.voice.api.util.Params;

import javax.sound.sampled.*;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;

public class JavaxInputDevice extends BaseAudioDevice implements InputDevice {

    private static final Logger LOGGER = LogManager.getLogger(JavaxInputDevice.class);

    private final PlasmoVoiceClient client;
    private final @Nullable String name;

    private AudioFormat format;
    @Getter
    private int bufferSize;
    private TargetDataLine device;

    public JavaxInputDevice(PlasmoVoiceClient client, @Nullable String name) {
        this.client = client;
        this.name = name;
    }

    @Override
    public CompletableFuture<AudioDevice> open(@NotNull AudioFormat format, @NotNull Params params) throws DeviceException {
        checkNotNull(params, "params cannot be null");

        DevicePreOpenEvent preOpenEvent = new DevicePreOpenEvent(this, params);
        client.getEventBus().call(preOpenEvent);

        if (preOpenEvent.isCancelled()) {
            throw new DeviceException("Device opening has been canceled");
        }

        CompletableFuture<AudioDevice> future = new CompletableFuture<>();

        try {
            this.format = format;
            this.bufferSize = ((int) format.getSampleRate() / 1_000) * 2 * 20;

            this.device = openDevice(name, format);
            device.open(format);
        } catch (DeviceException e) {
            future.completeExceptionally(e);
            return future;
        } catch (LineUnavailableException e) {
            future.completeExceptionally(new DeviceException("Failed to open javax device", e));
            return future;
        }

        LOGGER.info("Device " + name + " initialized");

        client.getEventBus().call(new DeviceOpenEvent(this));

        future.complete(this);
        return future;
    }

    @Override
    public CompletableFuture<AudioDevice> close() {
        if (!isOpen()) {
            device.stop();
            device.flush();
            device.close();
            this.device = null;
        }

        client.getEventBus().call(new DeviceClosedEvent(this));
        return CompletableFuture.completedFuture(this);
    }

    @Override
    public boolean isOpen() {
        return device != null && device.isOpen();
    }

    @Override
    public @Nullable String getName() {
        return name;
    }

    @Override
    public Optional<AudioFormat> getFormat() {
        return Optional.ofNullable(format);
    }

    @Override
    public void start() {
        if (!isOpen()) return;
        device.start();
    }

    @Override
    public void stop() {
        if (!isOpen()) return;

        device.stop();
        device.flush();
    }

    @Override
    public int available() {
        return device.available();
    }

    @Override
    public short[] read(int bufferSize) {
        if (!isOpen()) throw new IllegalStateException("Device is not open");

        byte[] samples = new byte[bufferSize];
        int read = device.read(samples, 0, bufferSize);
        if (read == -1) {
            return null;
        }

        short[] shorts = AudioUtil.bytesToShorts(samples);
        shorts = processFilters(shorts);

        return shorts;
    }

    @Override
    public short[] read() {
        return read(bufferSize);
    }

    @Override
    public DeviceType getType() {
        return DeviceType.INPUT;
    }

    private TargetDataLine openDevice(String deviceName, AudioFormat format) throws DeviceException {
        if (deviceName == null) {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            try {
                return (TargetDataLine) AudioSystem.getLine(info);
            } catch (Exception e) {
                throw new DeviceException("No devices available", e);
            }
        }

        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            DataLine.Info lineInfo = new DataLine.Info(TargetDataLine.class, format);

            if (mixer.isLineSupported(lineInfo)) {
                String lineName = mixerInfo.getName();
                if (lineName.equals(deviceName)) {
                    try {
                        return (TargetDataLine) mixer.getLine(lineInfo);
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        throw new DeviceException("Device not found");
    }
}
