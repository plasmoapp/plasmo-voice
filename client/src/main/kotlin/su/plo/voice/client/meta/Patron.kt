package su.plo.voice.client.meta

import java.util.UUID

data class Patron(
    val uuid: UUID,
    val name: String,
    val tier: String
)
