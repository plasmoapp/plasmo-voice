package su.plo.voice.api.server.audio.source;

import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.proto.data.audio.source.SourceInfo;

public interface ServerAudioSource<S extends SourceInfo> extends AudioSource<S, VoiceServerPlayer> {
}
