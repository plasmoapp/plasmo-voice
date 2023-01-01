package su.plo.lib.paper.entity;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.entity.MinecraftPlayer;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.lib.api.server.entity.MinecraftServerEntity;
import su.plo.lib.api.server.world.MinecraftServerWorld;
import su.plo.lib.api.server.world.ServerPos3d;
import su.plo.voice.proto.data.pos.Pos3d;

import java.util.UUID;

@RequiredArgsConstructor
public class PaperServerEntity<E extends LivingEntity> implements MinecraftServerEntity {

    protected final MinecraftServerLib minecraftServer;
    protected final E instance;

    private final Pos3d position = new Pos3d();
    private final Pos3d lookAngle = new Pos3d();

    private Location location;

    @Override
    public int getId() {
        return instance.getEntityId();
    }

    @Override
    public @NotNull UUID getUUID() {
        return instance.getUniqueId();
    }

    @Override
    public @NotNull Pos3d getPosition() {
        return getPosition(position);
    }

    @Override
    public @NotNull Pos3d getPosition(@NotNull Pos3d position) {
        fetchLocation();

        position.setX(location.getX());
        position.setY(location.getY());
        position.setZ(location.getZ());

        return position;
    }

    @Override
    public @NotNull Pos3d getLookAngle() {
        return getLookAngle(lookAngle);
    }

    @Override
    public @NotNull Pos3d getLookAngle(@NotNull Pos3d lookAngle) {
        Vector vector = instance.getLocation().getDirection();
        lookAngle.setX(vector.getX());
        lookAngle.setY(vector.getY());
        lookAngle.setZ(vector.getZ());

        return lookAngle;
    }

    @Override
    public double getEyeHeight() {
        return instance.getEyeHeight();
    }

    @Override
    public float getHitBoxWidth() {
        return (float) instance.getBoundingBox().getWidthX();
    }

    @Override
    public float getHitBoxHeight() {
        return (float) instance.getBoundingBox().getHeight();
    }

    @Override
    public boolean isInvisibleTo(@NotNull MinecraftPlayer player) {
        throw new IllegalStateException("Not implemented"); // todo: implement
    }

    @Override
    public <T> T getInstance() {
        return (T) instance;
    }

    @Override
    public @NotNull ServerPos3d getServerPosition() {
        fetchLocation();

        return new ServerPos3d(
                minecraftServer.getWorld(instance.getWorld()),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }

    @Override
    public @NotNull ServerPos3d getServerPosition(@NotNull ServerPos3d position) {
        fetchLocation();

        position.setWorld(minecraftServer.getWorld(instance.getWorld()));

        position.setX(location.getX());
        position.setY(location.getY());
        position.setZ(location.getZ());

        position.setYaw(location.getYaw());
        position.setPitch(location.getPitch());

        return position;
    }

    @Override
    public @NotNull MinecraftServerWorld getWorld() {
        return minecraftServer.getWorld(instance.getWorld());
    }

    private void fetchLocation() {
        if (location == null) {
            this.location = instance.getLocation();
        } else {
            instance.getLocation(location);
        }
    }
}
