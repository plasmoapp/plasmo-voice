package su.plo.lib.api.server.command;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;

public interface MinecraftChatHolder {

    void sendMessage(@NotNull MinecraftTextComponent text);

    default void sendMessage(@NotNull String text) {
        sendMessage(MinecraftTextComponent.literal(text));
    }

    void sendActionBar(@NotNull MinecraftTextComponent text);

    default void sendActionBar(@NotNull String text) {
        sendActionBar(MinecraftTextComponent.literal(text));
    }

    @NotNull String getLanguage();
}
