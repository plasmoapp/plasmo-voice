package su.plo.voice.client.sound;

import su.plo.voice.client.VoiceClient;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

public class DataLines {
    public static TargetDataLine getMicrophone() {
        String micName = VoiceClient.getClientConfig().microphone.get();
        if (micName != null) {
            TargetDataLine mic = getMicrophoneByName(micName);
            if (mic != null) {
                return mic;
            }
        }
        return getDefaultMicrophone();
    }

    public static String getMicrophoneName() {
        String microphoneName = VoiceClient.getClientConfig().microphone.get();
        if (microphoneName != null) {
            return microphoneName;
        }

        return getMicrophoneNames().get(0);
    }

    public static TargetDataLine getDefaultMicrophone() {
        return getDefaultDevice(TargetDataLine.class);
    }

    public static SourceDataLine getDefaultSpeaker() {
        return getDefaultDevice(SourceDataLine.class);
    }

    public static <T> T getDefaultDevice(Class<T> lineClass) {
        DataLine.Info info = new DataLine.Info(lineClass, null);
        try {
            return lineClass.cast(AudioSystem.getLine(info));
        } catch (Exception e) {
            return null;
        }
    }

    public static TargetDataLine getMicrophoneByName(String name) {
        return getDeviceByName(TargetDataLine.class, name);
    }

    public static <T> T getDeviceByName(Class<T> lineClass, String name) {
        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info lineInfo = new Line.Info(lineClass);
            if (mixer.isLineSupported(lineInfo)) {
                String deviceName = mixerInfo.getName();
                // todo fix
//                try { // fix russian names KKomrade
//                    deviceName = new String(mixerInfo.getName().getBytes("Windows-1252"), "Windows-1251");
//                } catch (UnsupportedEncodingException ignored) {
//                    deviceName = mixerInfo.getName();
//                }

                if (deviceName.equals(name)) {
                    try {
                        return lineClass.cast(mixer.getLine(lineInfo));
                    } catch (Exception ignored) {}
                }
            }
        }
        return null;
    }

    public static List<String> getMicrophoneNames() {
        return getDeviceNames(TargetDataLine.class);
    }

    public static List<String> getDeviceNames(Class<?> lineClass) {
        List<String> names = new ArrayList<>();
        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info lineInfo = new Line.Info(lineClass);
            if (mixer.isLineSupported(lineInfo)) {
                names.add(mixerInfo.getName());
                // todo fix
//                try { // fix russian names KKomrade
//                    name = new String(mixerInfo.getName().getBytes("Windows-1252"), "Windows-1251");
//                } catch (UnsupportedEncodingException ignored) {
//                    name = mixerInfo.getName();
//                }
//                names.add(name);
            }
        }
        return names;
    }
}