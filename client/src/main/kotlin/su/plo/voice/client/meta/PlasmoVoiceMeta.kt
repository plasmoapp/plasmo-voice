package su.plo.voice.client.meta

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import su.plo.voice.BuildConstants
import su.plo.voice.client.meta.developer.Developer
import su.plo.voice.client.meta.developer.DeveloperRole
import java.net.URL
import java.util.*

data class PlasmoVoiceMeta(
    val developers: List<Developer>,
    val patrons: List<Patron>
) {
    companion object {

        val META: PlasmoVoiceMeta
            get() = meta

        private var meta = PlasmoVoiceMeta(
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
                )
            ), listOf()
        )

        private val gson = Gson()

        fun fetch(languageName: String) {
            CoroutineScope(Dispatchers.Default).launch { fetchSync(languageName) }
        }

        private fun fetchSync(languageName: String) {
            // todo: cache?

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
                return
            }

            val developers: List<Developer> = gson.fromJson(
                metaJson.get("developers"),
                object : TypeToken<List<Developer>>() {}.type
            )

            val patrons: List<Patron> = gson.fromJson(
                metaJson.get("patrons"),
                object : TypeToken<List<Patron>>() {}.type
            )

            this.meta = PlasmoVoiceMeta(developers, patrons)
        }
    }
}
