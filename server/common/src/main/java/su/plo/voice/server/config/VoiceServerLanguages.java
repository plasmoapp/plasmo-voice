package su.plo.voice.server.config;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.command.MinecraftChatHolder;

public final class VoiceServerLanguages extends BaseServerLanguages {

    public <T> T getLanguage(@NotNull String languageName)  {
        return getLanguage(ServerLanguage.class, languageName);
    }

    public <T> T getLanguage(@NotNull MinecraftChatHolder chatHolder) {
        return getLanguage(ServerLanguage.class, chatHolder.getLanguage());
    }
}
