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
     * Registers a new language using resources
     *
     * <p>
     *     Reads <b>languages/list</b> from resources using {@link ResourceLoader}
     *     <br/>
     *     In the <b>list</b> you should specify list of languages separated by <b>\n</b>
     *     <br/>
     *     After reading the list, all languages from it will be read from resources <b>languages/*.toml</b>
     *     and saved to <b>languageFolder/*.toml</b>
     * </p>
     *
     * <p>
     *     You can edit and create new languages in languagesFolder, they not will be overwritten or deleted
     * </p>
     *
     * <p>Default language is <b>en_us</b>, can be changed in server config</p>
     */
    void register(@NotNull ResourceLoader resourceLoader,
                  @NotNull File languagesFolder);

    /**
     * Registers a new language using crowdin
     *
     * <p>Works as {@link #register(ResourceLoader, File)}, but also uses crowdin as defauls</p>
     *
     * <p>Crowdin translations will be cached in <b>languageFolder/.crowdin</b> for <b>3 days</b></p>
     *
     * <p>Default language is <b>en_us</b>, can be changed in server config</p>
     */
    void register(@NotNull String crowdinProjectId,
                  @Nullable String fileName,
                  @NotNull ResourceLoader resourceLoader,
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
