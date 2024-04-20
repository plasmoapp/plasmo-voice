package su.plo.voice.server.language

import com.google.common.base.Charsets
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Maps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import su.plo.config.toml.Toml
import su.plo.config.toml.TomlWriter
import su.plo.crowdin.CrowdinLib
import su.plo.slib.api.language.ServerTranslator
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.voice.api.server.resource.ResourceLoader
import su.plo.voice.api.server.language.ServerLanguages
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import java.util.concurrent.CompletableFuture

class VoiceServerLanguages(
    private val serverTranslator: ServerTranslator,
    override var crowdinEnabled: Boolean = true
) : ServerLanguages {

    private val languages: MutableMap<String, VoiceServerLanguage> = Maps.newHashMap()

    @Synchronized
    override fun register(
        resourceLoader: ResourceLoader,
        languagesFolder: File
    ): CompletableFuture<Void> {
        try {
            val languages: MutableMap<String, VoiceServerLanguage> = Maps.newHashMap()

            val languagesList = readLanguagesList(resourceLoader)
            languagesList.forEach { languageName ->
                val languageFile = File(languagesFolder, String.format("%s.toml", languageName))
                val language = loadLanguage(resourceLoader, languageFile, languageName)
                languages[languageName] = language
            }

            return CompletableFuture.runAsync {
                register(languages, languagesFolder)
            }
        } catch (e: IOException) {
            throw IllegalStateException("Failed to load languages", e)
        }
    }

    override fun register(
        crowdinProjectId: String,
        fileName: String?,
        resourceLoader: ResourceLoader,
        languagesFolder: File
    ): CompletableFuture<Void?> = CoroutineScope(Dispatchers.Default).future {
        registerSync(crowdinProjectId, fileName, resourceLoader, languagesFolder)
        null
    }

    override fun getServerLanguage(languageName: String?) =
        getLanguage(languageName, LanguageScope.SERVER)

    override fun getClientLanguage(languageName: String?) =
        getLanguage(languageName, LanguageScope.CLIENT)

    private fun registerSync(
        crowdinProjectId: String,
        fileName: String?,
        resourceLoader: ResourceLoader,
        languagesFolder: File
    ) {
        try {
            if (crowdinEnabled) {
                try {
                    downloadCrowdinTranslations(crowdinProjectId, fileName, languagesFolder)
                } catch (e: Exception) {
                    LOGGER.warn(
                        "Failed to download crowdin project {} ({}) translations: {}",
                        crowdinProjectId,
                        fileName,
                        e.message
                    )
                }
            }

            val crowdinFolder = File(languagesFolder, ".crowdin")
            if (!crowdinFolder.exists()) {
                register(resourceLoader, languagesFolder).get()
                return
            }

            val languages: MutableMap<String, VoiceServerLanguage> = Maps.newHashMap()

            // load from languages/list
            val languagesList = readLanguagesList(resourceLoader)
            languagesList.forEach { languageName ->
                val languageFileName = "$languageName.toml"

                val crowdinFile = File(crowdinFolder, languageFileName)
                val languageFile = File(languagesFolder, languageFileName)

                val language = loadLanguage(resourceLoader, crowdinFile, languageFile, languageName)
                languages[languageName] = language
            }

            // merge defaults from jar and from crowdin, map based on crowdin
            val crowdinTranslations = crowdinFolder.listFiles()
            if (crowdinTranslations != null) {
                for (crowdinFile in crowdinTranslations) {
                    if (crowdinFile.name == "timestamp") continue

                    val languageName = crowdinFile.name.split(".")[0]
                    if (languages.containsKey(languageName)) continue

                    val languageFile = File(languagesFolder, crowdinFile.name)

                    val language = loadLanguage(resourceLoader, crowdinFile, languageFile, languageName)
                    languages[languageName] = language
                }
            }

            register(languages, languagesFolder)
        } catch (e: IOException) {
            throw IllegalStateException("Failed to load languages", e)
        }
    }

    @Throws(Exception::class)
    private fun downloadCrowdinTranslations(
        crowdinProjectId: String,
        fileName: String?,
        languagesFolder: File
    ) {
        var timestamp = 0L
        val crowdinFolder = File(languagesFolder, ".crowdin")
        val timestampFile = File(crowdinFolder, "timestamp")
        if (timestampFile.exists()) {
            val timestampString = String(Files.readAllBytes(timestampFile.toPath()))
            try {
                timestamp = timestampString.toLong()
            } catch (ignored: NumberFormatException) {
            }
        }

        // check timestamp, if outdated, download from crowdin and use it as defaults
        if (System.currentTimeMillis() - timestamp < 86400 * 3 * 1000) return

        val rawTranslations: Map<String, ByteArray> = CrowdinLib
            .downloadRawTranslations(crowdinProjectId, fileName)
            .get()

        // write timestamp file
        crowdinFolder.mkdirs()
        Files.write(
            timestampFile.toPath(),
            System.currentTimeMillis().toString().toByteArray()
        )

        if (rawTranslations.isEmpty()) return

        // write translations files
        for ((languageName, translationBytes) in rawTranslations) {
            Files.write(
                File(crowdinFolder, "$languageName.toml").toPath(),
                translationBytes
            )
        }
    }

    @Synchronized
    @Throws(IOException::class)
    private fun register(
        languages: MutableMap<String, VoiceServerLanguage>,
        languagesFolder: File
    ) {
        if (languages.isEmpty()) return

        val defaultLanguage = languages.getOrDefault(
            serverTranslator.defaultLanguage,
            languages[languages.keys.first()]!!
        )

        // load from languagesFolder if not found in list and use default language as defaults
        languagesFolder.mkdirs()

        for (file in languagesFolder.listFiles()!!) {
            if (file.isDirectory) continue

            val fileName = file.name
            if (!fileName.endsWith(".toml")) continue

            val languageName = fileName.substring(0, fileName.length - 5)

            if (languages.containsKey(languageName)) continue

            val language = loadLanguage(file, null)
            language.merge(defaultLanguage)

            languages[languageName] = language
        }

        // save all languages
        for ((key, value) in languages) {
            val languageFile = File(languagesFolder, "$key.toml")
            saveLanguage(languageFile, value)
        }

        // merge languages
        for ((languageName, value) in languages) {
            val language = this.languages.computeIfAbsent(languageName) { value }
            language.merge(value)
            language.merge(defaultLanguage)
        }

        this.languages.forEach { (languageName, language) ->
            if (languages.containsKey(languageName)) return@forEach
            language.merge(defaultLanguage)
        }

        this.languages.forEach { (languageName, language) ->
            serverTranslator.register(languageName, language.serverLanguage)
        }
    }

    private fun getLanguage(languageName: String?, scope: LanguageScope): Map<String, String> {
        val language = languages[languageName?.lowercase() ?: serverTranslator.defaultLanguage]
        if (languageName == null && language == null) return ImmutableMap.of()
        if (language == null) return getLanguage(null, scope)

        return if (scope == LanguageScope.SERVER) language.serverLanguage else language.clientLanguage
    }

    @Throws(IOException::class)
    private fun loadLanguage(
        resourceLoader: ResourceLoader,
        crowdinTranslation: File,
        languageFile: File,
        languageName: String
    ): VoiceServerLanguage {
        // defaults are based on crowdin translations then merged with defaults from jar (if exist)
        var crowdinDefaults: Toml? = null
        if (crowdinTranslation.exists()) {
            FileInputStream(crowdinTranslation).use { inputStream ->
                val inputStreamReader = InputStreamReader(inputStream, Charsets.UTF_8)
                crowdinDefaults = Toml().read(inputStreamReader)
            }
        }

        var jarDefaults: Toml? = null
        try {
            resourceLoader.load("languages/$languageName.toml")
                .use { inputStream ->
                    if (inputStream == null) return@use

                    val inputStreamReader = InputStreamReader(inputStream, Charsets.UTF_8)
                    jarDefaults = Toml().read(inputStreamReader)
                }
        } catch (ignored: Exception) {
        }

        if (crowdinDefaults == null && jarDefaults == null) {
            throw IOException("Both crowdin and jar defaults are null for language $languageName")
        }

        val tomlLanguage = Toml()
        if (languageFile.exists()) {
            try {
                tomlLanguage.read(languageFile)
            } catch (e: Exception) {
                throw IOException("Failed to load language " + languageFile.name, e)
            }
        }

        val language = VoiceServerLanguage(tomlLanguage, null)
        if (crowdinDefaults != null) language.merge(VoiceServerLanguage(crowdinDefaults!!, null))
        if (jarDefaults != null) language.merge(VoiceServerLanguage(jarDefaults!!, null))

        return language
    }

    @Throws(IOException::class)
    private fun loadLanguage(
        resourceLoader: ResourceLoader,
        languageFile: File,
        languageName: String
    ): VoiceServerLanguage {
        try {
            resourceLoader.load("languages/$languageName.toml").use { inputStream ->
                if (inputStream == null) throw IOException("Resource 'languages/$languageName.toml' not found")
                val inputStreamReader = InputStreamReader(inputStream, Charsets.UTF_8)

                val defaults: Toml = Toml().read(inputStreamReader)
                return loadLanguage(languageFile, defaults)
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Failed to load language $languageName", e)
        }
    }

    @Throws(IOException::class)
    private fun loadLanguage(
        languageFile: File,
        defaults: Toml?
    ): VoiceServerLanguage {
        val language = Toml()
        if (languageFile.exists()) {
            try {
                language.read(languageFile)
            } catch (e: Exception) {
                throw IOException("Failed to load language " + languageFile.name, e)
            }
        }

        return VoiceServerLanguage(language, defaults)
    }

    @Throws(IOException::class)
    private fun saveLanguage(
        languageFile: File,
        language: VoiceServerLanguage
    ) {
        try {
            TomlWriter().write(language.original, languageFile)
        } catch (e: Exception) {
            throw IOException("Failed to save language", e)
        }
    }

    private fun readLanguagesList(resourceLoader: ResourceLoader): List<String> {
        resourceLoader.load("languages/list")
            .use { inputStream ->
                val languagesList = inputStream ?: throw IllegalStateException("Resource 'languages/list' not found")

                BufferedReader(InputStreamReader(languagesList, StandardCharsets.UTF_8))
                    .use { br ->
                        return br.readLines().filter { it.isNotEmpty() }
                    }
            }
    }

    private enum class LanguageScope {
        CLIENT,
        SERVER
    }

    private class VoiceServerLanguage(
        language: Toml,
        defaults: Toml?
    ) {

        var original: Map<String, Any>
        val serverLanguage: MutableMap<String, String>
        val clientLanguage: MutableMap<String, String>

        init {
            original = if (defaults == null) language.toMap() else mergeMaps(language.toMap(), defaults.toMap())
            serverLanguage = mergeMaps(language, "server", defaults)
            clientLanguage = mergeMaps(language, "client", defaults)
        }

        fun merge(language: VoiceServerLanguage) {
            original = mergeMaps(original, language.original)

            language.serverLanguage.forEach(serverLanguage::putIfAbsent)
            language.clientLanguage.forEach(clientLanguage::putIfAbsent)
        }

        @Suppress("UNCHECKED_CAST")
        private fun mergeMaps(
            language: Map<String, Any>,
            defaults: Map<String, Any>
        ): Map<String, Any> {
            val merged: MutableMap<String, Any> = Maps.newConcurrentMap()
            merged.putAll(language)

            for ((key, value) in defaults) {
                if (value is Map<*, *>) {
                    merged[key] = mergeMaps(
                        (language[key] ?: Maps.newHashMap<String, Any>()) as Map<String, Any>,
                        value as Map<String, Any>
                    )
                } else if (!language.containsKey(key)) {
                    merged[key] = value
                }
            }

            return merged
        }

        private fun mergeMaps(
            language: Toml,
            scope: String,
            defaults: Toml?
        ): MutableMap<String, String> {
            val defaultsMap: MutableMap<String, String> = Maps.newConcurrentMap()
            if (defaults != null) {
                defaultsMap.putAll(
                    languageToMapOfStrings(
                        if (defaults.getTable(scope) == null) Toml() else defaults.getTable(scope)
                    )
                )
            }

            val languageMap = languageToMapOfStrings(
                if (language.getTable(scope) == null) Toml() else language.getTable(scope)
            )

            defaultsMap.putAll(languageMap)
            return defaultsMap
        }

        private fun languageToMapOfStrings(language: Toml): Map<String, String> {
            val languageMap: MutableMap<String, String> = Maps.newHashMap()

            language.toMap().forEach { (key, value) ->
                if (value is Map<*, *>) {
                    val tableContents = languageToMapOfStrings(language.getTable(key))

                    tableContents.forEach { (contentKey, contentValue) ->
                        languageMap["$key.$contentKey"] = contentValue
                    }
                } else {
                    languageMap[key] = value.toString()
                }
            }
            return languageMap
        }
    }

    companion object {
        private val LOGGER = McLoggerFactory.createLogger("VoiceServerLanguages")
    }
}
