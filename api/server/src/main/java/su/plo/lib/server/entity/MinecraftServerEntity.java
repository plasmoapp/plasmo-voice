package su.plo.lib.server.entity;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.entity.MinecraftEntity;
import su.plo.lib.server.world.MinecraftServerWorld;
import su.plo.lib.server.world.ServerPos3d;

public interface MinecraftServerEntity extends MinecraftEntity {

    @NotNull ServerPos3d getServerPosition();

    @NotNull ServerPos3d getServerPosition(@NotNull ServerPos3d position);

    @NotNull MinecraftServerWorld getWorld();
}
