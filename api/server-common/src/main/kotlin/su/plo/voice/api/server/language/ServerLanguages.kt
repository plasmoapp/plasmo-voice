package su.plo.voice.api.server.language

import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.component.McTranslatableText
import su.plo.slib.api.command.McChatHolder
import su.plo.slib.api.resource.ResourceLoader
import java.io.File
import java.util.concurrent.CompletableFuture

/**
 * Manages languages.
 */
interface ServerLanguages {

    /**
     * Gets or sets whether crowdin is enabled.
     *
     * `true` by default.
     *
     * @return `true` if crowdin is enabled, `false` otherwise.
     */
    var crowdinEnabled: Boolean

    /**
     * Registers a new language using resources.
     *
     * Reads the language list from resources using [ResourceLoader]. The list should specify the available
     * languages separated by '\n'. After reading the list, all languages from it will be read from resources
     * as 'languages/?.toml' and saved to 'languagesFolder/?.toml'. Languages created or edited in the
     * 'languagesFolder' will not be overwritten or deleted.
     *
     * Default language is 'en_us'
     *
     * @param resourceLoader   The resource loader for reading language resources.
     * @param languagesFolder  The folder where language files will be saved.
     * @return A CompletableFuture representing the completion of the registration process.
     */
    fun register(
        resourceLoader: ResourceLoader,
        languagesFolder: File
    ): CompletableFuture<Void>

    /**
     * Registers a new language using crowdin.
     *
     * This method works similarly to [register], but it also uses crowdin as the default language source.
     * Crowdin translations will be cached in 'languageFolder/.crowdin' for 3 days.
     *
     * @param crowdinProjectId The crowdin project ID for translations.
     * @param fileName         The name of the language file in crowdin (null for default).
     * @param resourceLoader   The resource loader for reading language resources.
     * @param languagesFolder  The folder where language files will be saved.
     * @return A CompletableFuture representing the completion of the registration process.
     */
    fun register(
        crowdinProjectId: String,
        fileName: String?,
        resourceLoader: ResourceLoader,
        languagesFolder: File
    ): CompletableFuture<Void?>

    /**
     * Gets server language data by name or default language if not found.
     *
     * @param languageName The name of the language to retrieve.
     * @return A map representing the server language data.
     */
    fun getServerLanguage(languageName: String?): Map<String, String>

    /**
     * Gets client language data by name or default language if not found.
     *
     * @param languageName The name of the language to retrieve.
     * @return A map representing the client language data.
     */
    fun getClientLanguage(languageName: String?): Map<String, String>

    /**
     * Gets the default server language.
     *
     * @return A map representing the default server language data.
     */
    val serverLanguage: Map<String, String>
        get() = getServerLanguage(null)

    /**
     * Gets server language data based on the chat holder's language setting.
     *
     * @param holder The chat holder whose language setting is used.
     * @return A map representing the server language data.
     */
    fun getServerLanguage(holder: McChatHolder): Map<String, String> =
        getServerLanguage(holder.language)

    /**
     * Gets the default client language.
     *
     * @return A map representing the default client language data.
     */
    val clientLanguage: Map<String, String>
        get() = getClientLanguage(null)

    /**
     * Gets client language data based on the chat holder's language setting.
     *
     * @param holder The chat holder whose language setting is used.
     * @return A map representing the client language data.
     */
    fun getClientLanguage(holder: McChatHolder): Map<String, String> {
        return getClientLanguage(holder.language)
    }

    /**
     * Translates text using the server language.
     *
     * @param text   The text to be translated.
     * @param holder The chat holder whose language setting is used.
     * @param key    The translation key.
     * @return A [McTextComponent] representing the translated text.
     */
    fun translate(
        text: McTranslatableText,
        holder: McChatHolder,
        key: String
    ): McTextComponent? {
        val language = getServerLanguage(holder.language)
        return if (!language.containsKey(key)) text else McTextComponent.literal(language[key]!!)
    }
}
