package su.plo.lib.api.server.entity;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.entity.MinecraftPlayerEntity;
import su.plo.lib.api.server.player.MinecraftServerPlayer;

import java.util.Collection;
import java.util.Optional;

public interface MinecraftServerPlayerEntity extends MinecraftServerPlayer, MinecraftServerEntity, MinecraftPlayerEntity {

    /**
     * @return true if the specified player is not being hidden from this player
     */
    boolean canSee(@NotNull MinecraftServerPlayerEntity player);

    /**
     * @return collection of registered mods channels
     */
    Collection<String> getRegisteredChannels();

    /**
     * Gets the entity which is followed by the player when in spectator mode
     *
     * @return the followed entity
     */
    Optional<MinecraftServerEntity> getSpectatorTarget();
}
