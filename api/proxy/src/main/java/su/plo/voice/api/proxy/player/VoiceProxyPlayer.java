package su.plo.voice.api.proxy.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.proxy.player.MinecraftProxyPlayer;
import su.plo.voice.api.proxy.PlasmoVoiceProxy;
import su.plo.voice.api.server.player.PlayerModLoader;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.player.VoicePlayerInfo;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;

import java.security.PublicKey;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@ToString(doNotUseGetters = true, callSuper = true)
public final class VoiceProxyPlayer implements VoicePlayer {

    private final PlasmoVoiceProxy voiceProxy;
    @Getter
    private final MinecraftProxyPlayer instance;

    @Setter
    private boolean voiceDisabled;
    @Setter
    private boolean microphoneMuted;
    @Setter
    private boolean muted;

    public void sendPacket(@NotNull Packet<?> packet) {
        instance.sendPacket("plasmo:voice/v2", PacketTcpCodec.encode(packet));
    }

    public boolean hasVoiceChat() {
        return voiceProxy.getUdpConnectionManager()
                .getConnectionByPlayerId(instance.getUUID())
                .isPresent();
    }

    @Override
    public Optional<PlayerModLoader> getModLoader() {
        return Optional.empty();
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

    public boolean isVoiceDisabled() {
        checkVoiceChat();
        return voiceDisabled;
    }

    public boolean isMicrophoneMuted() {
        checkVoiceChat();
        return microphoneMuted;
    }

    @Override
    public int getActivationDistanceById(@NotNull UUID activationId) {
        return 0;
    }

    @Override
    public void visualizeDistance(int radius, int hexColor) {

    }

    @Override
    public Optional<PublicKey> getPublicKey() {
        return Optional.empty();
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

    private void checkVoiceChat() {
        if (!hasVoiceChat()) throw new IllegalStateException("Player is not connected to UDP server");
    }
}
