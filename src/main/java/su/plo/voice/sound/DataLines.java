package su.plo.voice.sound;

import su.plo.voice.client.VoiceClient;

import javax.sound.sampled.*;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class DataLines {
    public static TargetDataLine getMicrophone() {
        String micName = VoiceClient.config.microphone;
        if (micName != null) {
            TargetDataLine mic = getMicrophoneByName(micName);
            if (mic != null) {
                return mic;
            }
        }
        return getDefaultMicrophone();
    }

    public static SourceDataLine getSpeaker() {
        String speakerName = VoiceClient.config.speaker;
        if (speakerName != null) {
            SourceDataLine speaker = getSpeakerByName(speakerName);
            if (speaker != null) {
                return speaker;
            }
        }
        return getDefaultSpeaker();
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

    public static SourceDataLine getSpeakerByName(String name) {
        return getDeviceByName(SourceDataLine.class, name);
    }

    public static <T> T getDeviceByName(Class<T> lineClass, String name) {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info lineInfo = new Line.Info(lineClass);
            if (mixer.isLineSupported(lineInfo)) {
                String deviceName;
                try { // fix russian names KKomrade
                    deviceName = new String(mixerInfo.getName().getBytes("Windows-1252"), "Windows-1251");
                } catch (UnsupportedEncodingException ignored) {
                    deviceName = mixerInfo.getName();
                }

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

    public static List<String> getSpeakerNames() {
        return getDeviceNames(SourceDataLine.class);
    }

    public static List<String> getDeviceNames(Class<?> lineClass) {
        List<String> names = new ArrayList<>();
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info lineInfo = new Line.Info(lineClass);
            if (mixer.isLineSupported(lineInfo)) {
                String name;
                try { // fix russian names KKomrade
                    name = new String(mixerInfo.getName().getBytes("Windows-1252"), "Windows-1251");
                } catch (UnsupportedEncodingException ignored) {
                    name = mixerInfo.getName();
                }
                names.add(name);
            }
        }
        return names;
    }
}