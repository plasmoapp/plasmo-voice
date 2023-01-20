package su.plo.lib.api.server.command;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;

public interface MinecraftChatHolder {

    void sendMessage(@NotNull MinecraftTextComponent text);

    void sendMessage(@NotNull String text);

    void sendActionBar(@NotNull String text);

    void sendActionBar(@NotNull MinecraftTextComponent text);

    @NotNull String getLanguage();
}
