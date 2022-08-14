package su.plo.voice.server.entity;

import lombok.RequiredArgsConstructor;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.entity.VoiceEntity;

import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public final class ModVoiceEntity implements VoiceEntity {

    private final Entity entity;

    @Override
    public @NotNull UUID getUUID() {
        return entity.getUUID();
    }

    @Override
    public <T> T getObject() {
        return (T) entity;
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
