package su.plo.voice.server.connection;

import com.google.common.collect.Maps;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.server.config.ServerConfig;
import su.plo.voice.api.server.connection.TcpServerConnectionManager;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.audio.capture.CaptureInfo;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;
import su.plo.voice.proto.data.audio.codec.CodecInfo;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;
import su.plo.voice.proto.data.encryption.EncryptionInfo;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.clientbound.*;
import su.plo.voice.server.BaseVoiceServer;

import javax.crypto.Cipher;
import java.security.PublicKey;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class VoiceTcpConnectionManager implements TcpServerConnectionManager {

    private final BaseVoiceServer voiceServer;
    @Getter
    private final byte[] aesEncryptionKey;

    private final Object playerStateLock = new Object();

    public VoiceTcpConnectionManager(BaseVoiceServer voiceServer) {
        this.voiceServer = voiceServer;

        UUID key = UUID.randomUUID();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeLong(key.getMostSignificantBits());
        out.writeLong(key.getLeastSignificantBits());

        this.aesEncryptionKey = out.toByteArray();
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
        if (!voiceServer.getUdpServer().isPresent() || !voiceServer.getConfig().isPresent()) return;

        UUID secret = voiceServer.getUdpConnectionManager()
                .getSecretByPlayerId(player.getInstance().getUUID());

        ServerConfig.Host host = voiceServer.getConfig().get().getHost();
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
                port
        ));
    }

    @Override
    public void sendConfigInfo(@NotNull VoicePlayer receiver) {
        if (!voiceServer.getUdpServer().isPresent() || !voiceServer.getConfig().isPresent()) return;

        ServerConfig config = voiceServer.getConfig().get();
        ServerConfig.Voice voiceConfig = config.getVoice();
        ServerConfig.Voice.Opus opusConfig = voiceConfig.getOpus();

        Map<String, String> codecParams = Maps.newHashMap();
        codecParams.put("mode", opusConfig.getMode());
        codecParams.put("bitrate", String.valueOf(opusConfig.getBitrate()));


        EncryptionInfo aesEncryption;
        try {
            PublicKey publicKey = receiver.getPublicKey()
                    .orElseThrow(() -> new IllegalStateException(receiver + " has empty public key"));

            Cipher encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            aesEncryption = new EncryptionInfo(
                    "AES/CBC/PKCS5Padding",
                    encryptCipher.doFinal(aesEncryptionKey)
            );
        } catch (Exception e) {
            LogManager.getLogger().error("Failed to encode encryption data: {}", e.toString());
            e.printStackTrace();
            return;
        }


        receiver.sendPacket(new ConfigPacket(
                UUID.fromString(config.getServerId()),
                new CaptureInfo(
                        voiceConfig.getSampleRate(),
                        voiceConfig.getMtuSize(),
                        new CodecInfo("opus", codecParams)
                ),
                aesEncryption,
                voiceServer.getSourceLineManager()
                        .getLines()
                        .stream()
                        .map(line -> (VoiceSourceLine) line)
                        .collect(Collectors.toList()),
                voiceServer.getActivationManager()
                        .getActivations()
                        .stream()
                        .map(activation -> (VoiceActivation) activation) // waytoodank
                        .filter(activation -> receiver.getInstance().hasPermission("voice.activation." + activation.getName()))
                        .collect(Collectors.toList()),
                getPlayerPermissions(receiver)
        ));
    }

    @Override
    public void sendPlayerList(@NotNull VoicePlayer receiver) {
        synchronized (playerStateLock) {
            receiver.sendPacket(new PlayerListPacket(
                    voiceServer.getUdpConnectionManager().getConnections()
                            .stream()
                            .filter(connection -> receiver.getInstance().canSee(connection.getPlayer().getInstance()))
                            .map(connection -> connection.getPlayer().getInfo())
                            .collect(Collectors.toList())
            ));
        }
    }

    @Override
    public void broadcastPlayerInfoUpdate(@NotNull VoicePlayer player) {
        synchronized (playerStateLock) {
            broadcast(new PlayerInfoUpdatePacket(
                    player.getInfo()
            ), (player1) -> player1.getInstance().canSee(player.getInstance()));
        }
    }

    private Map<String, Boolean> getPlayerPermissions(@NotNull VoicePlayer player) {
        Map<String, Boolean> permissions = Maps.newHashMap();

        voiceServer.getPlayerManager()
                .getSynchronizedPermissions()
                .forEach(permission ->
                        permissions.put(permission, player.getInstance().hasPermission(permission))
                );

        return permissions;
    }
}
