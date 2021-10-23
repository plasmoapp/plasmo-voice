package su.plo.voice.client.sound;

import su.plo.voice.client.utils.AudioUtils;
import su.plo.voice.client.utils.CircularFloatBuffer;

// https://stackoverflow.com/questions/36355992/how-to-increase-volume-amplitude-on-raw-audio-bytes
// with some improvements
public class VolumeAdjuster {
    private final CircularFloatBuffer highestValues = new CircularFloatBuffer(48, -1F);

    public void adjust(byte[] samples, float targetVolume) {
        short[] shorts = AudioUtils.bytesToShorts(samples);
        adjust(shorts, targetVolume);

        for (int i = 0; i < samples.length; i += 2) {
            byte[] bytes = AudioUtils.shortToBytes((short) (shorts[i / 2] * targetVolume));
            samples[i] = bytes[0];
            samples[i + 1] = bytes[1];
        }
    }

    public void adjust(short[] samples, float targetVolume) {
        short highestValue = getHighestAbsoluteSample(samples);
        float highestPossibleMultiplier = (float) (Short.MAX_VALUE - 1) / (float) highestValue;
        if (targetVolume > highestPossibleMultiplier) {
            targetVolume = highestPossibleMultiplier;
        }

        highestValues.put(targetVolume);


        float minVolume = -1F;
        for (float highest : highestValues.getBuffer()) {
            if (highest < 0F) {
                continue;
            }

            if (minVolume < 0F) {
                minVolume = highest;
                continue;
            }

            if (highest < highest) {
                minVolume = highest;
            }
        }

        targetVolume = Math.min(minVolume, targetVolume);

        for (int i = 0; i < samples.length; i ++) {
            samples[i] *= targetVolume;
        }
    }

    private short getHighestAbsoluteSample(short[] samples) {
        short max = 0;
        for (short sample : samples) {
            if (sample == Short.MIN_VALUE) {
                sample += 1;
            }

            short abs = (short) Math.abs(sample);
            if (abs > max) {
                max = abs;
            }
        }

        return max;
    }
}
