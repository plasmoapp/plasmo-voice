package su.plo.voice.client.sound.capture;

import org.lwjgl.openal.ALC11;
import su.plo.voice.client.sound.openal.AlUtil;
import su.plo.voice.client.sound.openal.CustomSoundEngine;
import su.plo.voice.client.utils.AudioUtils;

public class AlCaptureDevice implements CaptureDevice {
    private long pointer;
    private boolean started;

    public void open() throws IllegalStateException {
        if (isOpen()) {
            throw new IllegalStateException("Capture device already open");
        }
        this.pointer = CustomSoundEngine.openCaptureDevice();
    }

    public void start() {
        if (!isOpen() || started) {
            return;
        }

        ALC11.alcCaptureStart(pointer);
        AlUtil.checkErrors("Start capture device");
        this.started = true;
    }

    public void stop() {
        if (!isOpen() || !started) {
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

    public byte[] read(int frameSize) {
        if ((frameSize / 2) > available()) {
            return null;
        }
        short[] shorts = new short[frameSize / 2];
        ALC11.alcCaptureSamples(pointer, shorts, shorts.length);
        AlUtil.checkErrors("Capture samples");

        return AudioUtils.shortsToBytes(shorts);
    }

    public boolean isOpen() {
        return pointer != 0L;
    }
}
