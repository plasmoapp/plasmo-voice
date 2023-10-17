package su.plo.voice.proxy.connection;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.proxy.connection.McProxyServerConnection;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.proxy.player.VoiceProxyPlayer;
import su.plo.voice.api.proxy.server.RemoteServer;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;
import su.plo.voice.proto.packets.PacketUtil;
import su.plo.voice.proto.packets.tcp.clientbound.*;
import su.plo.voice.proxy.BaseVoiceProxy;
import su.plo.voice.proxy.config.VoiceProxyConfig;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public final class ServerToPlayerChannelHandler implements ClientPacketTcpHandler {

    private final BaseVoiceProxy voiceProxy;
    @Setter
    @Getter
    private VoiceProxyPlayer player;

    @Override
    public void handle(@NotNull ConnectionPacket packet) {
//        if (isCurrentServerWithoutVoice()) {
//            voiceProxy.getUdpConnectionManager().removeConnection(player);
//            return;
//        }

        Optional<RemoteServer> remoteServer = getCurrentRemoteServer();
        if (!remoteServer.isPresent()) return;

        if (!remoteServer.get().isAesEncryptionKeySet()) {
            McProxyServerConnection connection = player.getInstance().getServer();
            if (connection != null) {
                try {
                    byte[] aesEncryptionKey = voiceProxy.getConfig().aesEncryptionKey();

                    SecretKey key = new SecretKeySpec(
                            PacketUtil.getUUIDBytes(voiceProxy.getConfig().forwardingSecret()),
                            "HmacSHA256"
                    );
                    Mac mac = Mac.getInstance("HmacSHA256");
                    mac.init(key);
                    mac.update(aesEncryptionKey, 0, aesEncryptionKey.length);
                    byte[] signature = mac.doFinal();

                    ByteArrayDataOutput output = ByteStreams.newDataOutput();
                    PacketUtil.writeBytes(output, signature);
                    PacketUtil.writeBytes(output, aesEncryptionKey);

                    connection.sendPacket(BaseVoiceProxy.SERVICE_CHANNEL_STRING, output.toByteArray());
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }
            }
        }

        UUID secret = voiceProxy.getUdpConnectionManager().setPlayerRemoteSecret(
                player.getInstance().getUuid(),
                packet.getSecret()
        );

        BaseVoice.DEBUG_LOGGER.log(
                "{} secret: {}; remote secret: {}",
                player.getInstance().getName(),
                secret, packet.getSecret()
        );

        voiceProxy.getUdpConnectionManager()
                .getConnectionByPlayerId(player.getInstance().getUuid())
                .ifPresent((connection) -> connection.setRemoteServer(remoteServer.get()));

        sendConnectionPacket(secret);
        throw new CancelForwardingException();
    }

    @Override
    public void handle(@NotNull ConfigPlayerInfoPacket packet) {
    }

    @Override
    public void handle(@NotNull PlayerInfoRequestPacket packet) {
    }

    @Override
    public void handle(@NotNull LanguagePacket packet) {
        Map<String, String> language = Maps.newHashMap(packet.getLanguage());
        language.putAll(voiceProxy.getMinecraftServer().getLanguages().getClientLanguage(packet.getLanguageName()));

        player.sendPacket(new LanguagePacket(packet.getLanguageName(), language));
        throw new CancelForwardingException();
    }

    @Override
    public void handle(@NotNull ConfigPacket packet) {
        Set<VoiceActivation> activations = Sets.newHashSet();
        if (!packet.getActivations().containsAll(voiceProxy.getActivationManager().getActivations())) {
            activations.addAll(
                    voiceProxy.getActivationManager()
                            .getActivations()
                            .stream()
                            .filter(activation -> activation.checkPermissions(player))
                            .map(activation -> (VoiceActivation) activation) // waytoodank
                            .collect(Collectors.toSet())
            );

            if (activations.size() > 0) activations.addAll(packet.getActivations());
        }

        Set<VoiceSourceLine> sourceLines = Sets.newHashSet();
        if (!packet.getSourceLines().containsAll(voiceProxy.getSourceLineManager().getLines())) {
            sourceLines.addAll(
                    voiceProxy.getSourceLineManager()
                            .getLines()
                            .stream()
                            .map((line) -> line.getSourceLineForPlayer(player))
                            .collect(Collectors.toSet())
            );

            if (sourceLines.size() > 0) sourceLines.addAll(packet.getSourceLines());
        }

        if (activations.size() > 0 || sourceLines.size() > 0) {
            player.sendPacket(new ConfigPacket(
                    packet.getServerId(),
                    packet.getCaptureInfo(),
                    packet.getEncryption(),
                    sourceLines.size() > 0 ? sourceLines : Sets.newHashSet(packet.getSourceLines()),
                    activations.size() > 0 ? activations : Sets.newHashSet(packet.getActivations()),
                    packet.getPermissions()
            ));
            throw new CancelForwardingException();
        }
    }

    @Override
    public void handle(@NotNull PlayerListPacket packet) {
//        if (isCurrentServerWithoutAesKey()) return;
//
//        System.out.println(
//                voiceProxy.getUdpConnectionManager().getConnections()
//                        .stream()
////                        .filter((connection) ->
////                                player.getInstance()
////                                        .getTabList()
////                                        .containsEntry(connection.getPlayer().getInstance().getUUID())
////                        )
//                        .filter(connection -> !connection.getPlayer().equals(this.player))
//                        .map(connection -> connection.getPlayer().createPlayerInfo())
//                        .collect(Collectors.toList())
//        );
//        player.sendPacket(new PlayerListPacket(
//                voiceProxy.getUdpConnectionManager().getConnections()
//                        .stream()
////                        .filter((connection) ->
////                                player.getInstance()
////                                        .getTabList()
////                                        .containsEntry(connection.getPlayer().getInstance().getUUID())
////                        )
//                        .filter(connection -> !connection.getPlayer().equals(this.player))
//                        .map(connection -> connection.getPlayer().createPlayerInfo())
//                        .collect(Collectors.toList())
//        ));
//
//        throw new CancelForwardingException();
    }

    @Override
    public void handle(@NotNull PlayerInfoUpdatePacket packet) {
//        if (isCurrentServerWithoutAesKey()) return;
//
//        voiceProxy.getPlayerManager().getPlayerById(packet.getPlayerInfo().getPlayerId()).ifPresent((player) -> {
//            boolean update = ((VoiceProxyPlayerConnection) player).update(packet.getPlayerInfo());
//            if (update) {
//                voiceProxy.getPlayerManager().broadcast(packet);
//            }
//        });
//
//        throw new CancelForwardingException();
    }

    @Override
    public void handle(@NotNull PlayerDisconnectPacket packet) {
//        if (isCurrentServerWithoutAesKey()) return;
//        throw new CancelForwardingException();
    }

    @Override
    public void handle(@NotNull SourceAudioEndPacket packet) {
    }

    @Override
    public void handle(@NotNull SourceInfoPacket packet) {
    }

    @Override
    public void handle(@NotNull SelfSourceInfoPacket packet) {
    }

    @Override
    public void handle(@NotNull SourceLineRegisterPacket packet) {
    }

    @Override
    public void handle(@NotNull SourceLineUnregisterPacket packet) {
    }

    @Override
    public void handle(@NotNull SourceLinePlayerAddPacket packet) {
    }

    @Override
    public void handle(@NotNull SourceLinePlayerRemovePacket packet) {
    }

    @Override
    public void handle(@NotNull SourceLinePlayersListPacket packet) {
    }

    @Override
    public void handle(@NotNull ActivationRegisterPacket packet) {
    }

    @Override
    public void handle(@NotNull ActivationUnregisterPacket packet) {
    }

    @Override
    public void handle(@NotNull DistanceVisualizePacket packet) {
    }

    @Override
    public void handle(@NotNull AnimatedActionBarPacket packet) {
    }

    private boolean isCurrentServerWithoutAesKey() {
        return !getCurrentRemoteServer()
                .map(RemoteServer::isAesEncryptionKeySet)
                .orElse(false);
    }

    private Optional<RemoteServer> getCurrentRemoteServer() {
        McProxyServerConnection server = player.getInstance().getServer();
        if (server == null) return Optional.empty();

        return voiceProxy.getRemoteServerManager().getServer(server.getServerInfo().getName());
    }

    private void sendConnectionPacket(@NotNull UUID secret) {
        VoiceProxyConfig.VoiceHost host = voiceProxy.getConfig().host();
        VoiceProxyConfig.VoiceHost.Public hostPublic = host.hostPublic();

        String ip = host.ip();
        if (hostPublic != null) ip = hostPublic.ip();

        int port = hostPublic != null ? hostPublic.port() : host.port();
        if (port == 0) {
            port = host.port();
            if (port == 0) {
                port = voiceProxy.getUdpProxyServer().get()
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
}
