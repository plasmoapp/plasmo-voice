package su.plo.lib.api.server.entity;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.entity.MinecraftPlayerEntity;
import su.plo.lib.api.server.player.MinecraftServerPlayer;

import java.util.Collection;
import java.util.Optional;

public interface MinecraftServerPlayerEntity extends MinecraftServerPlayer, MinecraftServerEntity, MinecraftPlayerEntity {

    boolean canSee(@NotNull MinecraftServerPlayerEntity player);

    Collection<String> getRegisteredChannels();

    Optional<MinecraftServerEntity> getSpectatorTarget();
}
