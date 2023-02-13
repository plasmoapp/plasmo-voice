package su.plo.voice.server.audio.capture;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.player.MinecraftServerPlayer;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VoiceServerActivation extends VoiceActivation implements ServerActivation {

    @Getter
    private final AddonContainer addon;

    @Getter
    private final Set<String> permissions;

    public VoiceServerActivation(@NotNull AddonContainer addon,
                                 @NotNull String name,
                                 @NotNull String translation,
                                 @NotNull String icon,
                                 Set<String> permissions,
                                 List<Integer> distances,
                                 int defaultDistance,
                                 boolean proximity,
                                 boolean transitive,
                                 boolean stereoSupported,
                                 int weight) {
        super(name, translation, icon, distances, defaultDistance, proximity, stereoSupported, transitive, weight);

        this.addon = addon;
        this.transitive = transitive;
        this.permissions = permissions;
    }

    @Override
    public void addPermission(@NotNull String permission) {
        permissions.add(permission);
    }

    @Override
    public void removePermission(@NotNull String permission) {
        permissions.remove(permission);
    }

    @Override
    public void clearPermissions() {
        permissions.clear();
    }

    @Override
    public boolean checkPermissions(@NotNull VoicePlayer player) {
        return checkPermissions(player.getInstance());
    }

    @Override
    public boolean checkPermissions(@NotNull MinecraftServerPlayer serverPlayer) {
        return permissions.stream().anyMatch(serverPlayer::hasPermission);
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
