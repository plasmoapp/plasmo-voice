package su.plo.voice.server.connection;

import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.server.config.ServerConfig;
import su.plo.voice.api.server.connection.TcpServerConnectionManager;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.proto.data.audio.capture.CaptureInfo;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;
import su.plo.voice.proto.data.audio.codec.CodecInfo;
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

    private final Object playerStateLock = new Object();

    public VoiceTcpConnectionManager(@NotNull BaseVoiceServer voiceServer) {
        this.voiceServer = voiceServer;
    }

    @Override
    public void broadcast(@NotNull Packet<ClientPacketTcpHandler> packet, @Nullable Predicate<VoiceServerPlayer> filter) {
        for (VoiceServerPlayer player : voiceServer.getPlayerManager().getPlayers()) {
            if ((filter == null || filter.test(player)) && player.hasVoiceChat())
                player.sendPacket(packet);
        }
    }

    @Override
    public void connect(@NotNull VoiceServerPlayer player) {
        if (!voiceServer.getUdpServer().isPresent() || voiceServer.getConfig() == null) return;

        UUID secret = voiceServer.getUdpConnectionManager()
                .getSecretByPlayerId(player.getInstance().getUUID());

        ServerConfig.Host host = voiceServer.getConfig().host();
        ServerConfig.Host.Public hostPublic = host.hostPublic();

        String ip = host.ip();
        if (ip.isEmpty() && hostPublic != null) ip = hostPublic.ip();

        int port = hostPublic != null ? hostPublic.port() : host.port();
        if (port == 0) {
            port = host.port();
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
    public void sendConfigInfo(@NotNull VoiceServerPlayer receiver) {
        if (!voiceServer.getUdpServer().isPresent() || voiceServer.getConfig() == null) return;

        ServerConfig config = voiceServer.getConfig();
        ServerConfig.Voice voiceConfig = config.voice();
        ServerConfig.Voice.Opus opusConfig = voiceConfig.opus();

        Map<String, String> codecParams = Maps.newHashMap();
        codecParams.put("mode", opusConfig.mode());
        codecParams.put("bitrate", String.valueOf(opusConfig.bitrate()));


        EncryptionInfo aesEncryption;
        try {
            PublicKey publicKey = receiver.getPublicKey()
                    .orElseThrow(() -> new IllegalStateException(receiver + " has empty public key"));

            Cipher encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            aesEncryption = new EncryptionInfo(
                    "AES/CBC/PKCS5Padding",
                    encryptCipher.doFinal(voiceConfig.aesEncryptionKey())
            );
        } catch (Exception e) {
            LogManager.getLogger().error("Failed to encode encryption data: {}", e.toString());
            e.printStackTrace();
            return;
        }

        receiver.sendPacket(new ConfigPacket(
                UUID.fromString(config.serverId()),
                new CaptureInfo(
                        voiceConfig.sampleRate(),
                        voiceConfig.mtuSize(),
                        new CodecInfo("opus", codecParams)
                ),
                aesEncryption,
                voiceServer.getSourceLineManager()
                        .getLines()
                        .stream()
                        .map(line -> line.getPlayerSourceLine(receiver))
                        .collect(Collectors.toSet()),
                voiceServer.getActivationManager()
                        .getActivations()
                        .stream()
                        .filter(activation -> receiver.getInstance().hasPermission(activation.getPermission()))
                        .map(activation -> (VoiceActivation) activation) // waytoodank
                        .collect(Collectors.toSet()),
                getPlayerPermissions(receiver)
        ));
    }

    @Override
    public void sendPlayerList(@NotNull VoiceServerPlayer receiver) {
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
    public void broadcastPlayerInfoUpdate(@NotNull VoiceServerPlayer player) {
        synchronized (playerStateLock) {
            broadcast(new PlayerInfoUpdatePacket(
                    player.getInfo()
            ), (player1) -> player1.getInstance().canSee(player.getInstance()));
        }
    }

    private Map<String, Boolean> getPlayerPermissions(@NotNull VoiceServerPlayer player) {
        Map<String, Boolean> permissions = Maps.newHashMap();

        voiceServer.getPlayerManager()
                .getSynchronizedPermissions()
                .forEach(permission ->
                        permissions.put(permission, player.getInstance().hasPermission(permission))
                );

        return permissions;
    }
}
