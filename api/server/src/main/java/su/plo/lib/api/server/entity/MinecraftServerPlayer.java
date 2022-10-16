package su.plo.lib.api.server.entity;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.entity.MinecraftPlayer;
import su.plo.lib.api.server.command.MinecraftCommandSource;

public interface MinecraftServerPlayer extends MinecraftServerEntity, MinecraftCommandSource, MinecraftPlayer {

    void sendPacket(@NotNull String channel, byte[] data);

    boolean canSee(@NotNull MinecraftServerPlayer player);
}
