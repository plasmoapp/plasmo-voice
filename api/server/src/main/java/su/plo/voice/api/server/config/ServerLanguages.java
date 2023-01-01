package su.plo.voice.api.server.config;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.command.MinecraftChatHolder;

import java.io.File;

public interface ServerLanguages {

    void register(@NotNull ResourceLoader resourceLoader,
                  @NotNull File languagesFolder,
                  @NotNull Class<?> languageClass,
                  @NotNull String defaultLanguageName);

    <T> T getLanguage(@NotNull Class<?> languageClass, @NotNull String languageName);

    <T> T getLanguage(@NotNull Class<?> languageClass, @NotNull MinecraftChatHolder chatHolder);
}
