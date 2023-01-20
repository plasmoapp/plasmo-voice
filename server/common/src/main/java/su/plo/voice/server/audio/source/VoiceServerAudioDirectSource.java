package su.plo.voice.server.audio.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.PlasmoVoice;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.audio.source.ServerDirectSource;
import su.plo.voice.api.server.connection.UdpConnectionManager;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.api.server.socket.UdpConnection;

public final class VoiceServerAudioDirectSource
        extends BaseAudioDirectSource<VoiceServerPlayer>
        implements ServerDirectSource {

    public VoiceServerAudioDirectSource(@NotNull PlasmoVoice voice,
                                        @NotNull UdpConnectionManager<VoiceServerPlayer, ? extends UdpConnection<?>> udpConnections,
                                        @NotNull AddonContainer addon,
                                        @NotNull ServerSourceLine line,
                                        @Nullable String codec,
                                        boolean stereo) {
        super(voice, udpConnections, addon, line, codec, stereo);
    }
}
