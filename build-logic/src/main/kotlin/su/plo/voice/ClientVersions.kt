package su.plo.voice

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class ClientVersion(
    val project: String,
    val mcVersion: Int,
    val mappings: String,
    val parent: String?,
    val extraMappings: String?,
)

fun clientVersions(file: File): List<ClientVersion> =
    file.reader().use { Gson().fromJson(it, object : TypeToken<List<ClientVersion>>() {}.type) }
