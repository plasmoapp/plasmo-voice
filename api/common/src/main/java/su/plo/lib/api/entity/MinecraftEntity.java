package su.plo.lib.api.entity;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.pos.Pos3d;

import java.util.UUID;

public interface MinecraftEntity {

    /**
     * Gets the entity id
     */
    int getId();

    @NotNull UUID getUUID();

    @NotNull Pos3d getPosition();

    @NotNull Pos3d getPosition(@NotNull Pos3d position);

    @NotNull Pos3d getLookAngle();

    @NotNull Pos3d getLookAngle(@NotNull Pos3d lookAngle);

    double getEyeHeight();

    float getHitBoxWidth();

    float getHitBoxHeight();

    boolean isInvisibleTo(@NotNull MinecraftPlayer player);

    /**
     * Gets the backed entity object
     */
    <T> T getInstance();
}
