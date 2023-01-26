package su.plo.voice.api.server.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.chat.MinecraftTranslatableText;
import su.plo.lib.api.server.command.MinecraftChatHolder;

import java.io.File;
import java.util.Map;

public interface ServerLanguages {

    /**
     * Registers new language
     * Default language is en_us, can be changed in config
     */
    void register(@NotNull ResourceLoader resourceLoader,
                  @NotNull File languagesFolder);

    /**
     * Gets the server language by name or default language if not found
     */
    Map<String, String> getServerLanguage(@Nullable String languageName);

    /**
     * Gets the client language by name or default language if not found
     */
    Map<String, String> getClientLanguage(@Nullable String languageName);

    /**
     * Gets the default server language
     */
    default Map<String, String> getServerLanguage() {
        return getServerLanguage((String) null);
    }

    /**
     * Gets the server language by chat holder
     */
    default Map<String, String> getServerLanguage(@NotNull MinecraftChatHolder holder) {
        return getServerLanguage(holder.getLanguage());
    }

    /**
     * Gets the default client language
     */
    default Map<String, String> getClientLanguage() {
        return getClientLanguage((String) null);
    }

    /**
     * Gets the client language by chat holder
     */
    default Map<String, String> getClientLanguage(@NotNull MinecraftChatHolder holder) {
        return getClientLanguage(holder.getLanguage());
    }

    /**
     * Translates text using server language
     */
    default MinecraftTextComponent translate(@NotNull MinecraftTranslatableText text,
                                             @NotNull MinecraftChatHolder holder,
                                             @NotNull String key) {
        Map<String, String> language = getServerLanguage(holder.getLanguage());
        if (!language.containsKey(key)) return text;

        return MinecraftTextComponent.literal(language.get(key));
    }
}
