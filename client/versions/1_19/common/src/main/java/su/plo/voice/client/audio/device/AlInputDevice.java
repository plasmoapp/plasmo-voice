package su.plo.voice.client.audio.device;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC11;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.AudioDevice;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.device.DeviceType;
import su.plo.voice.api.client.audio.device.InputDevice;
import su.plo.voice.api.client.event.audio.device.DeviceClosedEvent;
import su.plo.voice.api.client.event.audio.device.DeviceOpenEvent;
import su.plo.voice.api.client.event.audio.device.DevicePreOpenEvent;
import su.plo.voice.api.util.Params;
import su.plo.voice.client.audio.AlUtil;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;

public class AlInputDevice extends BaseAudioDevice implements InputDevice {

    private static final Logger LOGGER = LogManager.getLogger(AlInputDevice.class);

    private final PlasmoVoiceClient client;
    private final @Nullable String name;

    private AudioFormat format;
    @Getter
    private int bufferSize;
    private long devicePointer;
    private boolean started = false;

    public AlInputDevice(PlasmoVoiceClient client, @Nullable String name) {
        this.client = client;
        this.name = name;
    }

    @Override
    public CompletableFuture<AudioDevice> open(@NotNull AudioFormat format, @NotNull Params params) throws DeviceException {
        if (isOpen()) throw new DeviceException("Device is already open");
        checkNotNull(params, "params cannot be null");

        DevicePreOpenEvent preOpenEvent = new DevicePreOpenEvent(this, params);
        client.getEventBus().call(preOpenEvent);

        if (preOpenEvent.isCancelled()) {
            throw new DeviceException("Device opening has been canceled");
        }

        CompletableFuture<AudioDevice> future = new CompletableFuture<>();

        try {
            this.format = format;
            this.bufferSize = ((int) format.getSampleRate() / 1_000) * 20;
            this.devicePointer = openDevice(name, format);
        } catch (DeviceException e) {
            future.completeExceptionally(e);
            return future;
        }

        LOGGER.info("Device " + name + " initialized");
        client.getEventBus().call(new DeviceOpenEvent(this));

        future.complete(this);
        return future;
    }

    @Override
    public CompletableFuture<AudioDevice> close() {
        CompletableFuture<AudioDevice> future = new CompletableFuture<>();

        if (isOpen()) {
            stop();
            ALC11.alcCaptureCloseDevice(devicePointer);
            AlUtil.checkErrors("Close capture device");
            this.devicePointer = 0L;

            LOGGER.info("Device " + name + " closed");
        }

        client.getEventBus().call(new DeviceClosedEvent(this));

        future.complete(this);
        return future;
    }

    @Override
    public boolean isOpen() {
        return devicePointer != 0L;
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
        if (!isOpen() || started) return;

        ALC11.alcCaptureStart(devicePointer);
        AlUtil.checkErrors("Start device");

        this.started = true;
    }

    @Override
    public void stop() {
        if (!isOpen() || !started) return;

        ALC11.alcCaptureStop(devicePointer);
        AlUtil.checkErrors("Stop capture device");
        started = false;

        int available = available();
        short[] data = new short[available];
        ALC11.alcCaptureSamples(devicePointer, data, data.length);
        AlUtil.checkErrors("Capture available samples");
    }

    @Override
    public int available() {
        int samples = ALC11.alcGetInteger(devicePointer, ALC11.ALC_CAPTURE_SAMPLES);
        AlUtil.checkErrors("Get available samples count");
        return samples;
    }

    @Override
    public short[] read(int bufferSize) {
        if (!isOpen() || bufferSize > available()) return null;
        short[] shorts = new short[bufferSize * format.getChannels()];
        ALC11.alcCaptureSamples(devicePointer, shorts, bufferSize);
        AlUtil.checkErrors("Capture samples");

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

    private long openDevice(String deviceName, AudioFormat format) throws DeviceException {
        int alFormat = format.getChannels() == 2 ? AL11.AL_FORMAT_STEREO16 : AL11.AL_FORMAT_MONO16;

        long l;
        if (deviceName == null) {
            // default device
            l = ALC11.alcCaptureOpenDevice((ByteBuffer) null, (int) format.getSampleRate(), alFormat, bufferSize);
        } else {
            l = ALC11.alcCaptureOpenDevice(deviceName, (int) format.getSampleRate(), alFormat, bufferSize);
        }

        if (l != 0L && !AlUtil.checkAlcErrors(l, "Open device")) {
            return l;
        }

        throw new DeviceException("Failed to open OpenAL device");
    }
}
