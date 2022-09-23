package su.plo.voice.client.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.entity.MinecraftEntity;
import su.plo.lib.client.entity.MinecraftPlayer;
import su.plo.voice.proto.data.pos.Pos3d;

import java.util.UUID;

@RequiredArgsConstructor
public class ModEntity implements MinecraftEntity {

    @Getter
    private final Entity instance;

    private final Pos3d position = new Pos3d();

    @Override
    public @NotNull Pos3d getPosition() {
        position.setX(instance.position().x());
        position.setY(instance.position().y());
        position.setZ(instance.position().z());

        return position;
    }

    @Override
    public float getHitBoxWidth() {
        return instance.getBbWidth();
    }

    @Override
    public float getHitBoxHeight() {
        return instance.getBbHeight();
    }

    @Override
    public boolean isInvisibleTo(@NotNull MinecraftPlayer player) {
        return instance.isInvisibleTo(((ModPlayer) player).getInstance());
    }

    @Override
    public @NotNull UUID getUUID() {
        return instance.getUUID();
    }

    @Override
    public boolean isSneaking() {
        return instance.isDescending();
    }
}
