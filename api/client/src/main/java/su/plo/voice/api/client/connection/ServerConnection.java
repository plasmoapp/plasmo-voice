package su.plo.voice.api.client.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.encryption.EncryptionInfo;
import su.plo.voice.proto.data.player.VoicePlayerInfo;
import su.plo.voice.proto.packets.Packet;

import java.net.SocketAddress;
import java.security.KeyPair;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ServerConnection {

    @NotNull SocketAddress getRemoteAddress();

    @NotNull String getRemoteIp();

    Collection<VoicePlayerInfo> getPlayers();

    Optional<VoicePlayerInfo> getPlayerById(@NotNull UUID playerId);

    Optional<VoicePlayerInfo> getClientPlayer();

    void sendPacket(Packet<?> packet);

    void close();

    @NotNull KeyPair getKeyPair();

    void setKeyPair(@NotNull KeyPair keyPair);

    Optional<EncryptionInfo> getEncryptionInfo();
}
