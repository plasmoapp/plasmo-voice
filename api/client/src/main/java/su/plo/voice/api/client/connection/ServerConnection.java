package su.plo.voice.api.client.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.encryption.EncryptionInfo;
import su.plo.voice.proto.data.player.VoicePlayerInfo;
import su.plo.voice.proto.packets.Packet;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ServerConnection {

    Collection<VoicePlayerInfo> getPlayers();

    Optional<VoicePlayerInfo> getPlayerById(@NotNull UUID playerId);

    Optional<VoicePlayerInfo> getClientPlayer();

    void sendPacket(Packet<?> packet);

    void close();

    @NotNull PrivateKey getPrivateKey();

    @NotNull PublicKey getPublicKey();

    Optional<EncryptionInfo> getEncryptionInfo();
}
