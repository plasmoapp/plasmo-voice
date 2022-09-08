package su.plo.voice.client.audio.filter;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import su.plo.voice.api.client.audio.filter.AudioFilter;
import su.plo.voice.api.util.AudioUtil;
import su.plo.voice.api.util.CircularBuffer;
import su.plo.voice.config.entry.DoubleConfigEntry;

// https://stackoverflow.com/questions/36355992/how-to-increase-volume-amplitude-on-raw-audio-bytes
@RequiredArgsConstructor
public final class GainFilter implements AudioFilter {

    private final DoubleConfigEntry entry;

    @Setter
    private float volume;
    private final CircularBuffer<Float> highestValues = new CircularBuffer<>(48, -1F);

    @Override
    public short[] process(short[] samples) {
        this.volume = entry.value().floatValue();

        short highestValue = AudioUtil.getHighestAbsoluteSample(samples);
        float highestPossibleMultiplier = (float) (Short.MAX_VALUE - 1) / (float) highestValue;
        if (volume > highestPossibleMultiplier) {
            volume = highestPossibleMultiplier;
        }

        highestValues.put(volume);


        float minVolume = -1F;
        for (float highest : highestValues.getCollection()) {
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

        volume = Math.min(minVolume, volume);
        for (int i = 0; i < samples.length; i ++) {
            samples[i] *= volume;
        }

        return samples;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
