package su.plo.lib.api.client.entity;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.TextComponent;
import su.plo.lib.api.entity.MinecraftPlayer;

public interface MinecraftClientPlayer extends MinecraftPlayer {

    void sendChatMessage(@NotNull TextComponent text);

    void sendActionbarMessage(@NotNull TextComponent text);
}
