package su.plo.voice.proxy.audio.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.PlasmoVoice;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.proxy.audio.source.ProxyDirectSource;
import su.plo.voice.api.proxy.player.VoiceProxyPlayer;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.connection.UdpConnectionManager;
import su.plo.voice.api.server.socket.UdpConnection;
import su.plo.voice.server.audio.source.BaseAudioDirectSource;

public final class VoiceProxyAudioDirectSource extends BaseAudioDirectSource<VoiceProxyPlayer> implements ProxyDirectSource {

    public VoiceProxyAudioDirectSource(@NotNull PlasmoVoice voice,
                                       @NotNull UdpConnectionManager<VoiceProxyPlayer, ? extends UdpConnection<?>> udpConnections,
                                       @NotNull AddonContainer addon,
                                       @NotNull ServerSourceLine line,
                                       @Nullable String codec,
                                       boolean stereo) {
        super(voice, udpConnections, addon, line, codec, stereo);
    }
}
