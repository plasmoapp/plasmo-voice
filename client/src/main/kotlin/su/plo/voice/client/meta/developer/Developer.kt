package su.plo.voice.client.meta.developer

import java.util.*

data class Developer(
    val uuid: UUID,
    val name: String,
    val role: DeveloperRole,
    val socialLinkName: String,
    val socialLinkUrl: String,
    val aliases: List<String> = emptyList()
)
