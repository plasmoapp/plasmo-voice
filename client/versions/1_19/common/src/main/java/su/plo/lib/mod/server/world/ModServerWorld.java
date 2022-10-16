package su.plo.lib.mod.server.world;

import lombok.RequiredArgsConstructor;
import net.minecraft.server.level.ServerLevel;
import su.plo.lib.api.server.world.MinecraftServerWorld;

import java.util.Objects;

@RequiredArgsConstructor
public final class ModServerWorld implements MinecraftServerWorld {

    private final ServerLevel level;

    @Override
    public <T> T getInstance() {
        return (T) level;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object != null && this.getClass() == object.getClass()) {
            ModServerWorld world = (ModServerWorld) object;
            return this.level == world.level;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(level);
    }
}
