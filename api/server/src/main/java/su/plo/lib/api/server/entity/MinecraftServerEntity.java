package su.plo.lib.api.server.entity;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.entity.MinecraftEntity;
import su.plo.lib.api.server.world.MinecraftServerWorld;
import su.plo.lib.api.server.world.ServerPos3d;

public interface MinecraftServerEntity extends MinecraftEntity {

    @NotNull ServerPos3d getServerPosition();

    @NotNull ServerPos3d getServerPosition(@NotNull ServerPos3d position);

    @NotNull MinecraftServerWorld getWorld();
}
