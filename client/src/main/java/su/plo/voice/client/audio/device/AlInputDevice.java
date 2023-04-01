package su.plo.voice.client.audio.device;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC11;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.device.InputDevice;
import su.plo.voice.api.client.event.audio.device.DeviceClosedEvent;
import su.plo.voice.api.client.event.audio.device.DeviceOpenEvent;
import su.plo.voice.api.client.event.audio.device.DevicePreOpenEvent;
import su.plo.voice.client.audio.AlUtil;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;

public final class AlInputDevice extends BaseAudioDevice implements InputDevice {

    private static final Logger LOGGER = LogManager.getLogger(AlInputDevice.class);

    private long devicePointer;
    private boolean started = false;

    public AlInputDevice(PlasmoVoiceClient client,
                         @Nullable String name,
                         @NotNull AudioFormat format) throws DeviceException {
        super(client, name, format);
        open();
    }

    @Override
    public void close() {
        if (isOpen()) {
            stop();
            ALC11.alcCaptureCloseDevice(devicePointer);
            AlUtil.checkErrors("Close capture device");
            this.devicePointer = 0L;

            LOGGER.info("Device {} closed", getName());
        }

        getVoiceClient().getEventBus().call(new DeviceClosedEvent(this));
    }

    @Override
    public boolean isOpen() {
        return devicePointer != 0L;
    }

    @Override
    public synchronized void start() {
        if (!isOpen() || started) return;

        ALC11.alcCaptureStart(devicePointer);
        AlUtil.checkErrors("Start device");

        this.started = true;
    }

    @Override
    public synchronized void stop() {
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
        if (!isOpen() || !started) return 0;

        int samples = ALC11.alcGetInteger(devicePointer, ALC11.ALC_CAPTURE_SAMPLES);
        AlUtil.checkErrors("Get available samples count");
        return samples;
    }

    @Override
    public short[] read(int bufferSize) {
        if (!isOpen() || bufferSize > available()) return null;

        short[] shorts = new short[bufferSize * getFormat().getChannels()];
        ALC11.alcCaptureSamples(devicePointer, shorts, bufferSize);
        AlUtil.checkErrors("Capture samples");

        return shorts;
    }

    @Override
    protected void open() throws DeviceException {
        if (isOpen()) throw new DeviceException("Device is already open");

        DevicePreOpenEvent preOpenEvent = new DevicePreOpenEvent(this);
        getVoiceClient().getEventBus().call(preOpenEvent);

        if (preOpenEvent.isCancelled()) {
            throw new DeviceException("Device opening has been canceled");
        }

        this.devicePointer = openDevice();

        LOGGER.info("Device {} initialized", getName());
        getVoiceClient().getEventBus().call(new DeviceOpenEvent(this));
    }

    private long openDevice() throws DeviceException {
        AudioFormat format = getFormat();
        String deviceName = getName();

        int alFormat = format.getChannels() == 2 ? AL11.AL_FORMAT_STEREO16 : AL11.AL_FORMAT_MONO16;

        long l;
        if (deviceName == null) {
            // default device
            l = ALC11.alcCaptureOpenDevice((ByteBuffer) null, (int) format.getSampleRate(), alFormat, getBufferSize());
        } else {
            l = ALC11.alcCaptureOpenDevice(deviceName, (int) format.getSampleRate(), alFormat, getBufferSize());
        }

        if (l != 0L && !AlUtil.checkAlcErrors(l, "Open device")) {
            return l;
        }

        throw new DeviceException("Failed to open OpenAL device");
    }
}
