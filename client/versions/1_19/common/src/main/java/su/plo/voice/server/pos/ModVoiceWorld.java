package su.plo.voice.server.pos;

import lombok.RequiredArgsConstructor;
import net.minecraft.server.level.ServerLevel;
import su.plo.voice.api.server.pos.VoiceWorld;

import java.util.Objects;

@RequiredArgsConstructor
public final class ModVoiceWorld implements VoiceWorld {

    private final ServerLevel level;

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object != null && this.getClass() == object.getClass()) {
            ModVoiceWorld world = (ModVoiceWorld) object;
            return this.level == world.level;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.level);
    }
}
