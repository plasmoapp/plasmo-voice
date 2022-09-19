package su.plo.voice.client.player;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.chat.TextComponent;

public interface ClientPlayer {

    void sendChatMessage(@NotNull TextComponent text);

    void sendActionbarMessage(@NotNull TextComponent text);
}
