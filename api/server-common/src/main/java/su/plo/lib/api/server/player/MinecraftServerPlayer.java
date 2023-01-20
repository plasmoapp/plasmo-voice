package su.plo.lib.api.server.player;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.voice.proto.data.player.MinecraftGameProfile;

import java.util.UUID;

public interface MinecraftServerPlayer extends MinecraftCommandSource {

    @NotNull MinecraftGameProfile getGameProfile();

    @NotNull UUID getUUID();

    @NotNull String getName();

    void sendPacket(@NotNull String channel, byte[] data);

    void kick(@NotNull MinecraftTextComponent reason);
}
