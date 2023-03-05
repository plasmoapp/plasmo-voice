package su.plo.lib.api.server.entity;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.entity.MinecraftEntity;
import su.plo.lib.api.server.world.MinecraftServerWorld;
import su.plo.lib.api.server.world.ServerPos3d;

public interface MinecraftServerEntity extends MinecraftEntity {

    /**
     * @return a new instance of an entity's {@link ServerPos3d}
     */
    @NotNull ServerPos3d getServerPosition();

    /**
     * Copies all position info to provided {@link ServerPos3d} instance without creating a new instance of {@link ServerPos3d}
     */
    @NotNull ServerPos3d getServerPosition(@NotNull ServerPos3d position);

    /**
     * @return entity's current world
     */
    @NotNull MinecraftServerWorld getWorld();
}
