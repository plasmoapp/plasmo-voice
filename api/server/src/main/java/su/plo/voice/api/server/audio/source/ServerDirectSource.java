package su.plo.voice.api.server.audio.source;

import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.proto.data.audio.source.DirectSourceInfo;

/**
 * @see AudioDirectSource
 */
public interface ServerDirectSource extends AudioDirectSource<VoiceServerPlayer>, ServerAudioSource<DirectSourceInfo> {
}
