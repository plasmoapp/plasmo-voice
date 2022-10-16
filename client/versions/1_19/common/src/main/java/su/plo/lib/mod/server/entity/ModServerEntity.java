package su.plo.lib.mod.server.entity;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.lib.api.server.entity.MinecraftServerEntity;
import su.plo.lib.api.server.world.MinecraftServerWorld;
import su.plo.lib.api.server.world.ServerPos3d;
import su.plo.lib.mod.entity.ModEntity;

public final class ModServerEntity extends ModEntity<Entity> implements MinecraftServerEntity {

    private final MinecraftServerLib minecraftServer;

    public ModServerEntity(@NotNull MinecraftServerLib minecraftServer,
                           @NotNull Entity instance) {
        super(instance);

        this.minecraftServer = minecraftServer;
    }

    @Override
    public @NotNull ServerPos3d getServerPosition() {
        return new ServerPos3d(
                minecraftServer.getWorld(instance.getLevel()),
                instance.position().x(),
                instance.position().y(),
                instance.position().z(),
                instance.getXRot(),
                instance.getYRot()
        );
    }

    @Override
    public @NotNull ServerPos3d getServerPosition(@NotNull ServerPos3d position) {
        position.setWorld(minecraftServer.getWorld(instance.getLevel()));

        position.setX(instance.position().x());
        position.setY(instance.position().y());
        position.setZ(instance.position().z());

        position.setYaw(instance.getXRot());
        position.setPitch(instance.getYRot());

        return position;
    }

    @Override
    public @NotNull MinecraftServerWorld getWorld() {
        return minecraftServer.getWorld(instance.getLevel());
    }
}
