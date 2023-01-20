package su.plo.voice.api.proxy.audio.source;

import su.plo.voice.api.proxy.player.VoiceProxyPlayer;
import su.plo.voice.api.server.audio.source.AudioSource;
import su.plo.voice.proto.data.audio.source.SourceInfo;

public interface ProxyAudioSource<S extends SourceInfo> extends AudioSource<S, VoiceProxyPlayer> {
}
