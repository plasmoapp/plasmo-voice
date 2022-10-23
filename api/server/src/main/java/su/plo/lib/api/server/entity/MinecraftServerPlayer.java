package su.plo.lib.api.server.entity;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.entity.MinecraftPlayer;
import su.plo.lib.api.server.command.MinecraftCommandSource;

import java.util.Collection;
import java.util.Optional;

public interface MinecraftServerPlayer extends MinecraftServerEntity, MinecraftCommandSource, MinecraftPlayer {

    void sendPacket(@NotNull String channel, byte[] data);

    void kick(@NotNull MinecraftTextComponent reason);

    boolean canSee(@NotNull MinecraftServerPlayer player);

    Collection<String> getRegisteredChannels();

    Optional<MinecraftServerEntity> getSpectatorTarget();
}
