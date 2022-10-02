package su.plo.lib.server.command;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.chat.TextComponent;

public interface MinecraftChatHolder {

    void sendMessage(@NotNull TextComponent text);
}
