package su.plo.voice.client.sound.openal;

import org.lwjgl.openal.ALC11;

public class CaptureDevice {
    private long pointer;
    private boolean started;

    public void open() {
        if (isOpen()) {
            throw new IllegalStateException("Capture device already open");
        }
        this.pointer = CustomSoundEngine.openCaptureDevice();
    }

    public void start() {
        if (!isOpen()) {
            return;
        }
        if (started) {
            return;
        }

        ALC11.alcCaptureStart(pointer);
        AlUtil.checkErrors("Start capture device");
        this.started = true;
    }

    public void stop() {
        if (!isOpen()) {
            return;
        }
        if (!started) {
            return;
        }

        ALC11.alcCaptureStop(pointer);
        AlUtil.checkErrors("Stop capture device");
        started = false;

        int available = available();
        short[] data = new short[available];
        ALC11.alcCaptureSamples(pointer, data, data.length);
        AlUtil.checkErrors("Capture available samples");
    }

    public void close() {
        if (!isOpen()) {
            return;
        }

        stop();
        ALC11.alcCaptureCloseDevice(pointer);
        AlUtil.checkErrors("Close capture device");
        pointer = 0L;
    }

    public int available() {
        int samples = ALC11.alcGetInteger(pointer, ALC11.ALC_CAPTURE_SAMPLES);
        AlUtil.checkErrors("Get available samples count");
        return samples;
    }

    public void read(short[] data) {
        int available = available();
        if (data.length > available) {
            throw new IllegalStateException(String.format("Failed to read from microphone: Capacity %s, available %s", data.length, available));
        }
        ALC11.alcCaptureSamples(pointer, data, data.length);
        AlUtil.checkErrors("Capture samples");
    }

    public boolean isOpen() {
        return pointer != 0L;
    }
}
