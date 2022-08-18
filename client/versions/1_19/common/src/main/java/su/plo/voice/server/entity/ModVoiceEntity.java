package su.plo.voice.server.entity;

import lombok.RequiredArgsConstructor;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.entity.VoiceEntity;
import su.plo.voice.api.server.pos.ServerPos3d;

import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public final class ModVoiceEntity implements VoiceEntity {

    private final PlasmoVoiceServer voiceServer;
    private final Entity entity;

    @Override
    public int getId() {
        return entity.getId();
    }

    @Override
    public @NotNull UUID getUUID() {
        return entity.getUUID();
    }

    @Override
    public <T> T getObject() {
        return (T) entity;
    }

    @Override
    public @NotNull ServerPos3d getPosition() {
        return new ServerPos3d(
                voiceServer.getWorldManager().wrap(entity.getLevel()),
                entity.position().x(),
                entity.position().y(),
                entity.position().z(),
                entity.getXRot(),
                entity.getYRot()
        );
    }

    @Override
    public @NotNull ServerPos3d getPosition(@NotNull ServerPos3d position) {
        position.setWorld(voiceServer.getWorldManager().wrap(entity.getLevel()));

        position.setX(entity.position().x());
        position.setY(entity.position().y());
        position.setZ(entity.position().z());

        position.setYaw(entity.getXRot());
        position.setPitch(entity.getYRot());

        return position;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object != null && this.getClass() == object.getClass()) {
            ModVoiceEntity world = (ModVoiceEntity) object;
            return this.entity == world.entity;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.entity);
    }
}
