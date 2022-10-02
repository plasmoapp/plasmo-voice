package su.plo.lib.entity;

import org.jetbrains.annotations.NotNull;

public interface MinecraftPlayer extends MinecraftEntity {

    @NotNull String getName();

    boolean hasLabelScoreboard();
}
