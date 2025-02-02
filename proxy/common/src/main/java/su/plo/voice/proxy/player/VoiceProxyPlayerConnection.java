package su.plo.voice.proxy.player;

import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.proxy.player.McProxyPlayer;
import su.plo.voice.api.proxy.PlasmoVoiceProxy;
import su.plo.voice.api.proxy.event.connection.TcpPacketSendEvent;
import su.plo.voice.api.proxy.player.VoiceProxyPlayer;
import su.plo.voice.api.server.event.player.PlayerInfoCreateEvent;
import su.plo.voice.proto.data.player.VoicePlayerInfo;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;
import su.plo.voice.proxy.BaseVoiceProxy;
import su.plo.voice.server.player.BaseVoicePlayer;

@ToString(doNotUseGetters = true, callSuper = true)
public final class VoiceProxyPlayerConnection
        extends BaseVoicePlayer<McProxyPlayer>
        implements VoiceProxyPlayer {

    private final PlasmoVoiceProxy voiceProxy;

    @Setter
    private boolean muted;

    public VoiceProxyPlayerConnection(
            @NotNull PlasmoVoiceProxy voiceProxy,
            @NotNull McProxyPlayer instance
    ) {
        super(voiceProxy, instance);

        this.voiceProxy = voiceProxy;
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        TcpPacketSendEvent event = new TcpPacketSendEvent(this, packet);
        if (!voiceProxy.getEventBus().fire(event)) return;

        byte[] encoded = PacketTcpCodec.encode(packet);

        instance.sendPacket(BaseVoiceProxy.CHANNEL_STRING, encoded);
    }

    public boolean hasVoiceChat() {
        return voiceProxy.getUdpConnectionManager()
                .getConnectionByPlayerId(instance.getUuid())
                .isPresent();
    }

    public @NotNull VoicePlayerInfo createPlayerInfo() {
        checkVoiceChat();

        VoicePlayerInfo voicePlayerInfo = new VoicePlayerInfo(
                instance.getUuid(),
                instance.getName(),
                muted,
                isVoiceDisabled(),
                isMicrophoneMuted()
        );

        PlayerInfoCreateEvent event = new PlayerInfoCreateEvent(this, voicePlayerInfo);
        voiceProxy.getEventBus().fire(event);

        return event.getVoicePlayerInfo();
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
