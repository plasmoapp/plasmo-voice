package su.plo.voice.proxy.player;

import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.proxy.player.MinecraftProxyPlayer;
import su.plo.voice.api.proxy.PlasmoVoiceProxy;
import su.plo.voice.api.proxy.player.VoiceProxyPlayer;
import su.plo.voice.proto.data.player.VoicePlayerInfo;
import su.plo.voice.server.player.BaseVoicePlayer;

@ToString(doNotUseGetters = true, callSuper = true)
public final class VoiceProxyPlayerConnection
        extends BaseVoicePlayer<MinecraftProxyPlayer>
        implements VoiceProxyPlayer {

    private final PlasmoVoiceProxy voiceProxy;

    @Setter
    private boolean voiceDisabled;
    @Setter
    private boolean microphoneMuted;
    @Setter
    private boolean muted;

    public VoiceProxyPlayerConnection(@NotNull PlasmoVoiceProxy voiceProxy,
                                      @NotNull MinecraftProxyPlayer instance) {
        super(voiceProxy, instance);

        this.voiceProxy = voiceProxy;
    }

    public boolean hasVoiceChat() {
        return voiceProxy.getUdpConnectionManager()
                .getConnectionByPlayerId(instance.getUUID())
                .isPresent();
    }

    public @NotNull VoicePlayerInfo getInfo() {
        checkVoiceChat();

        return new VoicePlayerInfo(
                instance.getUUID(),
                instance.getName(),
                muted,
                isVoiceDisabled(),
                isMicrophoneMuted()
        );
    }

    public synchronized boolean update(@NotNull VoicePlayerInfo playerInfo) {
        checkVoiceChat();

        boolean changed = false;
        if (playerInfo.isMuted() != muted) {
            muted = playerInfo.isMuted();
            changed = true;
        }

        if (playerInfo.isVoiceDisabled() != voiceDisabled) {
            voiceDisabled = playerInfo.isVoiceDisabled();
            changed = true;
        }

        if (playerInfo.isMicrophoneMuted() != microphoneMuted) {
            microphoneMuted = playerInfo.isMicrophoneMuted();
            changed = true;
        }

        return changed;
    }
}
