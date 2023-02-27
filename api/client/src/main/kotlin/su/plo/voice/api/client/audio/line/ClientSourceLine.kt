package su.plo.voice.api.client.audio.line

import su.plo.lib.api.chat.MinecraftTextComponent
import su.plo.voice.proto.data.audio.line.SourceLine

interface ClientSourceLine : SourceLine, ClientPlayersSet {

    var volume: Double

    val translationComponent: MinecraftTextComponent
        get() = MinecraftTextComponent.translatable(translation)
}
