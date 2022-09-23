package su.plo.lib.client.entity;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.pos.Pos3d;

import java.util.UUID;

public interface MinecraftEntity {

    @NotNull Pos3d getPosition();

    float getHitBoxWidth();

    float getHitBoxHeight();

    boolean isInvisibleTo(@NotNull MinecraftPlayer player);

    @NotNull UUID getUUID();

    boolean isSneaking();
}
