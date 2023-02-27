package su.plo.voice.api.server.audio.source

import su.plo.voice.api.server.player.VoiceServerPlayer
import su.plo.voice.proto.data.audio.source.PlayerSourceInfo

interface ServerPlayerSource : ServerPositionalSource<PlayerSourceInfo> {

    val player: VoiceServerPlayer
}
