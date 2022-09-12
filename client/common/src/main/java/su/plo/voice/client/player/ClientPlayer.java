package su.plo.voice.client.player;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.chat.Text;

public interface ClientPlayer {

    void sendChatMessage(@NotNull Text text);

    void sendActionbarMessage(@NotNull Text text);
}
