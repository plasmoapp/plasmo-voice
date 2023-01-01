package su.plo.lib.paper.world;

import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.world.MinecraftServerWorld;

import java.util.Objects;

@RequiredArgsConstructor
public final class PaperServerWorld implements MinecraftServerWorld {

    private final World level;

    @Override
    public @NotNull String getKey() {
        return level.getKey().toString();
    }

    @Override
    public <T> T getInstance() {
        return (T) level;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object != null && this.getClass() == object.getClass()) {
            PaperServerWorld world = (PaperServerWorld) object;
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
