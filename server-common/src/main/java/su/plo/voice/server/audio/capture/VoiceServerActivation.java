package su.plo.voice.server.audio.capture;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VoiceServerActivation extends VoiceActivation implements ServerActivation {

    @Getter
    private final AddonContainer addon;

    public VoiceServerActivation(@NotNull AddonContainer addon,
                                 @NotNull String name,
                                 @NotNull String translation,
                                 @NotNull String icon,
                                 List<Integer> distances,
                                 int defaultDistance,
                                 boolean proximity,
                                 boolean transitive,
                                 boolean stereoSupported,
                                 int weight) {
        super(name, translation, icon, distances, defaultDistance, true, stereoSupported, transitive, weight);

        this.addon = addon;
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

    @Override
    public void setProximity(boolean proximity) {
        this.proximity = proximity;
    }

    @Override
    public boolean equals(Object o) {
        return o == this || super.equals(o);
    }
}
