package su.plo.voice.client.meta

import com.google.common.collect.Maps
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import su.plo.voice.BuildConstants
import su.plo.voice.client.meta.developer.Developer
import su.plo.voice.client.meta.developer.DeveloperRole
import java.io.File
import java.net.URL
import java.util.*
import kotlin.time.Duration
import kotlin.time.DurationUnit

data class PlasmoVoiceMeta(
    val developers: List<Developer>,
    val patrons: List<Patron>,
    val lastUpdateMs: Long
) {
    companion object {

        val META: PlasmoVoiceMeta
            get() = meta

        private val defaultMeta = PlasmoVoiceMeta(
            listOf(
                Developer(
                    UUID.fromString("2714d55f-ffef-4655-a93e-d8ca13230e76"),
                    "KPidS",
                    DeveloperRole.HUIX,
                    "Twitch",
                    "https://twitch.tv/kpids",
                    listOf("CoolStory_Bob")
                ),
                Developer(
                    UUID.fromString("8f552657-df1d-42cd-89c6-c176e195f703"),
                    "Apehum",
                    DeveloperRole.PROGRAMMING,
                    "Telegram",
                    "https://t.me/arehum",
                    listOf("GNOME__")
                ),
                Developer(
                    UUID.fromString("cfb727e7-efcc-4596-8c2b-9c6e38c8eea4"),
                    "Venterok",
                    DeveloperRole.ARTIST,
                    "Telegram",
                    "https://t.me/venterrok"
                ),
            ),
            listOf(),
            0L
        )

        private var meta = defaultMeta

        private val metaByLanguage: MutableMap<String, PlasmoVoiceMeta> = Maps.newConcurrentMap()
        private val cacheFile = File("config/plasmovoice/meta_cache.json")

        private val gson = Gson()
        private val cacheType = object : TypeToken<Map<String, PlasmoVoiceMeta>>() {}.type

        fun fetch(languageName: String) {
            CoroutineScope(Dispatchers.Default).launch { fetchOrLoadSync(languageName) }
        }

        private fun fetchOrLoadSync(languageName: String) {
            if (metaByLanguage.isEmpty()) {
                loadCache()
            }

            var meta = metaByLanguage[languageName]

            if (meta == null || System.currentTimeMillis() - meta.lastUpdateMs > Duration.parse("1d").inWholeMilliseconds) {
                meta = fetchSync(languageName)
                metaByLanguage[languageName] = meta
                saveCache()
            }

            this.meta = meta
        }

        private fun fetchSync(languageName: String): PlasmoVoiceMeta {
            val url = URL("https://plasmovoice.com/meta.json?language=${languageName}")

            val metaJson = try {
                val connection = url.openConnection()
                connection.addRequestProperty("User-Agent", "Plasmo Voice " + BuildConstants.VERSION)
                connection.connectTimeout = 3_000

                gson.fromJson(
                    connection.getInputStream().bufferedReader().use { it.readText() },
                    JsonElement::class.java
                ).asJsonObject
            } catch (e: Exception) {
                e.printStackTrace()
                return defaultMeta
            }

            val developers: List<Developer> = gson.fromJson(
                metaJson.get("developers"),
                object : TypeToken<List<Developer>>() {}.type
            )

            val patrons: List<Patron> = gson.fromJson(
                metaJson.get("patrons"),
                object : TypeToken<List<Patron>>() {}.type
            )

            return PlasmoVoiceMeta(developers, patrons, System.currentTimeMillis())
        }

        @Synchronized
        private fun loadCache() {
            if (!cacheFile.exists()) return

            val cache = try {
                gson.fromJson<Map<String, PlasmoVoiceMeta>>(cacheFile.bufferedReader(), cacheType) ?: return
            } catch (_: Exception) {
                return
            }

            metaByLanguage.putAll(cache)
        }

        @Synchronized
        private fun saveCache() {
            cacheFile.parentFile.mkdirs()
            cacheFile.writer().use {
                gson.toJson(metaByLanguage, it)
            }
        }
    }
}
