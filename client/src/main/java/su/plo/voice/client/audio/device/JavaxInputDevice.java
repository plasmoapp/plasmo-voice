package su.plo.voice.client.audio.device;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.device.InputDevice;
import su.plo.voice.api.client.event.audio.device.DeviceClosedEvent;
import su.plo.voice.api.client.event.audio.device.DeviceOpenEvent;
import su.plo.voice.api.client.event.audio.device.DevicePreOpenEvent;
import su.plo.voice.api.util.AudioUtil;

import javax.sound.sampled.*;

public final class JavaxInputDevice extends BaseAudioDevice implements InputDevice {

    private TargetDataLine device;

    public JavaxInputDevice(PlasmoVoiceClient client,
                            @Nullable String name,
                            @NotNull AudioFormat format) throws DeviceException {
        super(client, name, format);
        open();
    }

    @Override
    public synchronized void close() {
        if (!isOpen()) {
            device.stop();
            device.flush();
            device.close();
            this.device = null;
        }

        getVoiceClient().getEventBus().call(new DeviceClosedEvent(this));
    }

    @Override
    public boolean isOpen() {
        return device != null && device.isOpen();
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
        return device.available() / 2;
    }

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    public short[] read(int bufferSize) {
        if (!isOpen() || bufferSize > available()) return null;

        byte[] samples = new byte[bufferSize * 2];
        int read = device.read(samples, 0, samples.length);
        if (read == -1) return null;

        return AudioUtil.bytesToShorts(samples);
    }

    @Override
    protected void open() throws DeviceException {
        if (isOpen()) throw new DeviceException("Device is already open");

        DevicePreOpenEvent preOpenEvent = new DevicePreOpenEvent(this);
        getVoiceClient().getEventBus().call(preOpenEvent);

        if (preOpenEvent.isCancelled()) {
            throw new DeviceException("Device opening has been canceled");
        }

        try {
            this.device = openDevice();
            device.open(getFormat());
        } catch (LineUnavailableException e) {
            throw new DeviceException("Failed to open javax device", e);
        }

        BaseVoice.LOGGER.info("Device {} initialized", getName());

        getVoiceClient().getEventBus().call(new DeviceOpenEvent(this));
    }

    private TargetDataLine openDevice() throws DeviceException {
        AudioFormat format = getFormat();
        String deviceName = getName();

        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixers) {
            if (!mixerInfo.getName().equals(deviceName)) continue;

            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            DataLine.Info lineInfo = new DataLine.Info(TargetDataLine.class, format);
            if (!mixer.isLineSupported(lineInfo)) continue;

            try {
                return (TargetDataLine) mixer.getLine(lineInfo);
            } catch (Exception ignored) {
            }
        }

        throw new DeviceException("Device not found");
    }
}
