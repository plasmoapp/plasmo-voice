package su.plo.lib.server.entity;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.entity.MinecraftPlayer;
import su.plo.lib.server.command.MinecraftCommandSource;

public interface MinecraftServerPlayer extends MinecraftServerEntity, MinecraftCommandSource, MinecraftPlayer {

    void sendPacket(@NotNull String channel, byte[] data);

    boolean canSee(@NotNull MinecraftServerPlayer player);
}
