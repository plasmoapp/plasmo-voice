package su.plo.voice.client.sound.capture;

import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.sound.Recorder;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

public class JavaxCaptureDevice implements CaptureDevice {
    private TargetDataLine device;

    public void open() throws IllegalStateException {
        if (isOpen()) {
            throw new IllegalStateException("Capture device already open");
        }

        device = get();
        if (device == null) {
            device = getDefault();

            if (device == null) {
                throw new IllegalStateException("Failed to open javax capture device");
            }
        }

        try {
            device.open(Recorder.getFormat());
        } catch (LineUnavailableException e) {
            throw new IllegalStateException(e.getMessage());
        }

        device.start();
        device.stop();
        device.flush();
    }

    public void start() {
        if (!isOpen()) {
            return;
        }

        device.start();
    }

    public void stop() {
        if (!isOpen()) {
            return;
        }

        device.stop();
        device.flush();
    }

    public void close() {
        if (!isOpen()) {
            return;
        }

        device.stop();
        device.flush();
        device.close();
    }

    public int available() {
        return 0;
    }

    public byte[] read(int frameSize) {
        byte[] buffer = new byte[frameSize];
        int read = device.read(buffer, 0, frameSize);
        if (read == -1) {
            return null;
        }

        return buffer;
    }

    public boolean isOpen() {
        return device != null && device.isOpen();
    }


    private static TargetDataLine get() {
        String micName = VoiceClient.getClientConfig().microphone.get();
        if (micName != null) {
            TargetDataLine mic = getByName(micName);
            if (mic != null) {
                return mic;
            }
        }
        return getDefault();
    }

    public static List<String> getNames() {
        List<String> names = new ArrayList<>();
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info lineInfo = new Line.Info(TargetDataLine.class);
            if (mixer.isLineSupported(lineInfo)) {
                names.add(mixerInfo.getName());
            }
        }
        return names;
    }

    private static TargetDataLine getByName(String name) {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info lineInfo = new Line.Info(TargetDataLine.class);
            if (mixer.isLineSupported(lineInfo)) {
                String deviceName = mixerInfo.getName();
                if (deviceName.equals(name)) {
                    try {
                        return (TargetDataLine) mixer.getLine(lineInfo);
                    } catch (Exception ignored) {}
                }
            }
        }
        return null;
    }

    private static TargetDataLine getDefault() {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, null);
        try {
            return (TargetDataLine) AudioSystem.getLine(info);
        } catch (Exception e) {
            return null;
        }
    }
}
