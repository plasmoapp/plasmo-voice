package su.plo.voice.client.crowdin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import org.apache.logging.log4j.LogManager
import su.plo.crowdin.PlasmoCrowdinLib
import java.io.File

object PlasmoCrowdinMod {

    val folderName = ".crowdin"

    private val logger = LogManager.getLogger("Plasmo Voice Crowdin")

    fun downloadTranslations(crowdinFolder: File) {
        if (!translationsOutdated(crowdinFolder)) return

        CoroutineScope(Dispatchers.Default).launch {
            logger.info("Downloading translations")

            val translations = try {
                PlasmoCrowdinLib.downloadRawTranslations("plasmo-voice", "client.json").await()
            } catch (e: Exception) {
                logger.warn("Failed to download translations: {}", e.message)
                return@launch
            }

            crowdinFolder.mkdirs()
            translations.forEach { (languageName, languageBytes) ->
                File(crowdinFolder, "$languageName.json").writeBytes(languageBytes)
            }
            saveTimestamp(crowdinFolder)

            logger.info("Translations downloaded")
        }
    }

    private fun saveTimestamp(crowdinFolder: File) {
        File(crowdinFolder, "timestamp").writeText(System.currentTimeMillis().toString())
    }

    private fun translationsOutdated(crowdinFolder: File): Boolean {
        var timestamp = 0L

        val timestampFile = File(crowdinFolder, "timestamp")
        if (timestampFile.exists()) {
            val timestampString = timestampFile.readText()
            try {
                timestamp = timestampString.toLong()
            } catch (ignored: NumberFormatException) {
            }
        }

        return System.currentTimeMillis() - timestamp > 86400 * 3 * 1000
    }
}
