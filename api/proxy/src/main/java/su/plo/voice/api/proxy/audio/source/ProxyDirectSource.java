package su.plo.voice.api.proxy.audio.source;

import su.plo.voice.api.proxy.player.VoiceProxyPlayer;
import su.plo.voice.api.server.audio.source.AudioDirectSource;
import su.plo.voice.proto.data.audio.source.DirectSourceInfo;

/**
 * @see AudioDirectSource
 */
public interface ProxyDirectSource extends AudioDirectSource<VoiceProxyPlayer>, ProxyAudioSource<DirectSourceInfo> {
}
