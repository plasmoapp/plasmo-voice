package su.plo.lib.api.client.entity;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.entity.MinecraftPlayer;

public interface MinecraftClientPlayer extends MinecraftPlayer {

    void sendChatMessage(@NotNull MinecraftTextComponent text);

    void sendActionbarMessage(@NotNull MinecraftTextComponent text);
}
