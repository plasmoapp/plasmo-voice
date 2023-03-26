package su.plo.lib.mod.server.world;

import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.entity.MinecraftServerEntity;
import su.plo.lib.api.server.world.MinecraftServerWorld;

import java.util.Objects;

//#if MC>=11900

//#if MC>=11903
import net.minecraft.core.registries.BuiltInRegistries;
//#else
//$$ import net.minecraft.core.Registry;
//#endif

//#endif

@RequiredArgsConstructor
public final class ModServerWorld implements MinecraftServerWorld {

    private final ResourceLocation key;
    private final ServerLevel level;

    @Override
    public @NotNull String getKey() {
        return key.toString();
    }

    @Override
    public void sendGameEvent(@NotNull MinecraftServerEntity entity, @NotNull String gameEvent) {
        //#if MC>=11900
        Entity serverEntity = entity.getInstance();
        level.getServer().execute(() -> level.gameEvent(serverEntity, parseGameEvent(gameEvent), serverEntity.position()));
        //#endif
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

    //#if MC>=11900
    private GameEvent parseGameEvent(@NotNull String gameEventName) {
        //#if MC>=11903
        return BuiltInRegistries.GAME_EVENT.get(new ResourceLocation(gameEventName));
        //#else
        //$$ return Registry.GAME_EVENT.get(new ResourceLocation(gameEventName));
        //#endif
    }
    //#endif
}
