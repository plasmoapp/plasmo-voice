package su.plo.lib.api.entity;

import org.jetbrains.annotations.NotNull;

public interface MinecraftPlayer extends MinecraftEntity {

    @NotNull String getName();

    boolean isSpectator();

    boolean isSneaking();

    boolean hasLabelScoreboard();
}
