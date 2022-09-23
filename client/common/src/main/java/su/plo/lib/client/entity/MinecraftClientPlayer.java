package su.plo.lib.client.entity;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.chat.TextComponent;

public interface MinecraftClientPlayer extends MinecraftPlayer {

    void sendChatMessage(@NotNull TextComponent text);

    void sendActionbarMessage(@NotNull TextComponent text);
}
