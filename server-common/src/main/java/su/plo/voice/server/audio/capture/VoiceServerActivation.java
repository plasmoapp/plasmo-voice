package su.plo.voice.server.audio.capture;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VoiceServerActivation extends VoiceActivation implements ServerActivation {

    @Getter
    private final AddonContainer addon;

    @Getter
    @Setter
    private String permission;

    public VoiceServerActivation(@NotNull AddonContainer addon,
                                 @NotNull String name,
                                 @NotNull String translation,
                                 @NotNull String icon,
                                 @NotNull String permission,
                                 List<Integer> distances,
                                 int defaultDistance,
                                 boolean proximity,
                                 boolean transitive,
                                 boolean stereoSupported,
                                 int weight) {
        super(name, translation, icon, distances, defaultDistance, proximity, stereoSupported, transitive, weight);

        this.addon = addon;
        this.transitive = transitive;
        this.permission = permission;
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
