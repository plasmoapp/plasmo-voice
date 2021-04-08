package su.plo.voice.sound;

import org.jetbrains.annotations.Nullable;
import su.plo.voice.client.VoiceClient;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

public class DataLines {

    @Nullable
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

    @Nullable
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

    @Nullable
    public static TargetDataLine getDefaultMicrophone() {
        return getDefaultDevice(TargetDataLine.class);
    }

    @Nullable
    public static SourceDataLine getDefaultSpeaker() {
        return getDefaultDevice(SourceDataLine.class);
    }

    @Nullable
    public static <T> T getDefaultDevice(Class<T> lineClass) {
        DataLine.Info info = new DataLine.Info(lineClass, null);
        try {
            return lineClass.cast(AudioSystem.getLine(info));
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static TargetDataLine getMicrophoneByName(String name) {
        return getDeviceByName(TargetDataLine.class, name);
    }

    @Nullable
    public static SourceDataLine getSpeakerByName(String name) {
        return getDeviceByName(SourceDataLine.class, name);
    }

    @Nullable
    public static <T> T getDeviceByName(Class<T> lineClass, String name) {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info lineInfo = new Line.Info(lineClass);
            if (mixer.isLineSupported(lineInfo)) {
                if (mixerInfo.getName().equals(name)) {
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
                names.add(mixerInfo.getName());
            }
        }
        return names;
    }

}