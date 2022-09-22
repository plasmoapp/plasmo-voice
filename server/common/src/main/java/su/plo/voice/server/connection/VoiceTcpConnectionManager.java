package su.plo.voice.server.connection;

import com.google.common.collect.Maps;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.server.connection.TcpServerConnectionManager;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.socket.UdpConnection;
import su.plo.voice.proto.data.EncryptionInfo;
import su.plo.voice.proto.data.VoicePlayerInfo;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.clientbound.*;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.config.ServerConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class VoiceTcpConnectionManager implements TcpServerConnectionManager {

    private final BaseVoiceServer voiceServer;
    private final EncryptionInfo aesEncryption;

    private final Object playerStateLock = new Object();

    public VoiceTcpConnectionManager(BaseVoiceServer voiceServer) {
        this.voiceServer = voiceServer;

        UUID key = UUID.randomUUID();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeLong(key.getMostSignificantBits());
        out.writeLong(key.getLeastSignificantBits());

        this.aesEncryption = new EncryptionInfo("AES/CBC/PKCS5Padding", out.toByteArray());
    }

    @Override
    public void broadcast(@NotNull Packet<ClientPacketTcpHandler> packet, @Nullable Predicate<VoicePlayer> filter) {
        for (VoicePlayer player : voiceServer.getPlayerManager().getPlayers()) {
            if ((filter == null || filter.test(player)) && player.hasVoiceChat())
                player.sendPacket(packet);
        }
    }

    @Override
    public void broadcast(@NotNull Packet<ClientPacketTcpHandler> packet) {
        broadcast(packet, null);
    }

    @Override
    public void connect(@NotNull VoicePlayer player) {
        UUID secret = voiceServer.getUdpConnectionManager().getSecretByPlayerId(player.getUUID());

        ServerConfig.Host host = voiceServer.getConfig().getHost();
        ServerConfig.Host.Public hostPublic = host.getHostPublic();

        String ip = host.getIp();
        if (ip.isEmpty() && hostPublic != null) ip = hostPublic.getIp();

        int port = hostPublic != null ? hostPublic.getPort() : host.getPort();
        if (port == 0) {
            port = host.getPort();
            if (port == 0) {
                port = voiceServer.getUdpServer().get()
                        .getRemoteAddress().get()
                        .getPort();
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
    public void sendConfigInfo(@NotNull VoicePlayer receiver) {
        ServerConfig config = voiceServer.getConfig();
        ServerConfig.Voice voiceConfig = config.getVoice();

        receiver.sendPacket(new ConfigPacket(
                UUID.fromString(config.getServerId()),
                voiceConfig.getSampleRate(),
                "opus",
                voiceServer.getSourceLineManager()
                        .getLines()
                        .stream()
                        .map(line -> (VoiceSourceLine) line)
                        .collect(Collectors.toList()),
                voiceServer.getActivationManager()
                        .getActivations()
                        .stream()
                        .map(activation -> (VoiceActivation) activation) // waytoodank
                        .collect(Collectors.toList()),
                getPlayerPermissions(receiver)
        ));
    }

    @Override
    public void sendPlayerList(@NotNull VoicePlayer receiver) {
        synchronized (playerStateLock) {
            List<VoicePlayerInfo> players = new ArrayList<>();

            for (UdpConnection connection : voiceServer.getUdpConnectionManager().getConnections()) {
                if (receiver.canSee(connection.getPlayer())) {
                    players.add(connection.getPlayer().getInfo());
                }
            }

            receiver.sendPacket(new PlayerListPacket(players));
        }
    }

    @Override
    public void broadcastPlayerInfoUpdate(@NotNull VoicePlayer player) {
        synchronized (playerStateLock) {
            broadcast(new PlayerInfoUpdatePacket(
                    player.getInfo()
            ), (player1) -> player1.canSee(player));
        }
    }

    private Map<String, Boolean> getPlayerPermissions(@NotNull VoicePlayer player) {
        Map<String, Boolean> permissions = Maps.newHashMap();

        voiceServer.getPlayerManager()
                .getSynchronizedPermissions()
                .forEach(permission ->
                        permissions.put(permission, player.hasPermission(permission))
                );

        return permissions;
    }
}
