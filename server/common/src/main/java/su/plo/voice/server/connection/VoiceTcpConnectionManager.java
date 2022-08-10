package su.plo.voice.server.connection;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.server.connection.TcpServerConnectionManager;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.EncryptionInfo;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.clientbound.ClientPacketTcpHandler;
import su.plo.voice.proto.packets.tcp.clientbound.ConfigPacket;
import su.plo.voice.proto.packets.tcp.clientbound.ConnectionPacket;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.config.ServerConfig;

import java.util.UUID;
import java.util.function.Predicate;

public final class VoiceTcpConnectionManager implements TcpServerConnectionManager {

    private final BaseVoiceServer server;
    private final EncryptionInfo aesEncryption;

    public VoiceTcpConnectionManager(BaseVoiceServer server) {
        this.server = server;

        UUID key = UUID.randomUUID();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeLong(key.getMostSignificantBits());
        out.writeLong(key.getLeastSignificantBits());

        this.aesEncryption = new EncryptionInfo("AES/CBC/PKCS5Padding", out.toByteArray());
    }

    @Override
    public void broadcast(@NotNull Packet<ClientPacketTcpHandler> packet, @Nullable Predicate<VoicePlayer> filter) {
        for (VoicePlayer player : server.getPlayerManager().getPlayers()) {
            if (filter == null || filter.test(player)) player.sendPacket(packet);
        }
    }

    @Override
    public void connect(@NotNull VoicePlayer player) {
        UUID secret = server.getUdpConnectionManager().getSecretByPlayerId(player.getUUID());

        ServerConfig.Host host = server.getConfig().getHost();
        ServerConfig.Host.Public hostPublic = host.getHostPublic();

        String ip = host.getIp();
        if (ip.isEmpty() && hostPublic != null) ip = hostPublic.getIp();

        int port = hostPublic != null ? hostPublic.getPort() : host.getPort();
        if (port == 0) {
            port = host.getPort();
            if (port == 0) {
                port = server.getMinecraftServerPort();
                if (port <= 0) port = 60606;
            }
        }

        player.sendPacket(new ConnectionPacket(
                secret,
                ip,
                port,
                aesEncryption
        ));
    }

    @Override
    public void sendConfigInfo(@NotNull VoicePlayer player) {
        ServerConfig config = server.getConfig();

        player.sendPacket(new ConfigPacket(
                UUID.fromString(config.getServerId()),
                config.getVoice().getSampleRate(),
                "opus",
                config.getVoice().getDistances(),
                config.getVoice().getDefaultDistance(),
                config.getVoice().getMaxPriorityDistance()
        ));
    }
}
