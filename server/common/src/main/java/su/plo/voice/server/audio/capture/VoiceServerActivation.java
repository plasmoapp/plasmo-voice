package su.plo.voice.server.audio.capture;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.proto.data.capture.VoiceActivation;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VoiceServerActivation extends VoiceActivation implements ServerActivation {

    public VoiceServerActivation(@NotNull String name,
                                 @NotNull String translation,
                                 @NotNull String hudIconLocation,
                                 @NotNull String sourceIconLocation,
                                 List<Integer> distances,
                                 int defaultDistance,
                                 boolean transitive,
                                 int weight) {
        super(name, translation, hudIconLocation, sourceIconLocation, distances, defaultDistance, weight);
        this.transitive = transitive;
    }

    @Override
    public void setDistances(List<Integer> distances) {
        this.distances = checkNotNull(distances);
    }

    @Override
    public void setTransitive(boolean transitive) {
        this.transitive = transitive;
    }
}
