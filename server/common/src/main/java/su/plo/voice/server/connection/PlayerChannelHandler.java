package su.plo.voice.server.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.api.server.audio.source.ServerAudioSource;
import su.plo.voice.api.server.connection.TcpServerPacketManager;
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEndEvent;
import su.plo.voice.api.server.event.connection.TcpPacketReceivedEvent;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketHandler;
import su.plo.voice.proto.packets.tcp.clientbound.LanguagePacket;
import su.plo.voice.proto.packets.tcp.clientbound.SourceInfoPacket;
import su.plo.voice.proto.packets.tcp.serverbound.*;
import su.plo.voice.server.player.BaseVoicePlayer;
import su.plo.voice.server.util.version.ServerVersionUtil;
import su.plo.voice.util.version.SemanticVersion;

import java.security.KeyFactory;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;
import java.util.Optional;

public final class PlayerChannelHandler implements ServerPacketTcpHandler {

    private final PlasmoVoiceServer voiceServer;
    private final TcpServerPacketManager tcpConnections;
    private final VoiceServerPlayer player;

    public PlayerChannelHandler(@NotNull PlasmoVoiceServer voiceServer,
                                @NotNull VoiceServerPlayer player) {
        this.voiceServer = voiceServer;
        this.tcpConnections = voiceServer.getTcpPacketManager();
        this.player = player;
    }

    public void handlePacket(Packet<PacketHandler> packet) {
        if (!voiceServer.getUdpServer().isPresent()) return;

        TcpPacketReceivedEvent event = new TcpPacketReceivedEvent(player, packet);
        voiceServer.getEventBus().fire(event);
        if (event.isCancelled()) return;

        try {
            packet.handle(this);
        } catch (Exception e) {
            BaseVoice.DEBUG_LOGGER.log("Failed to handle packet ({}): {}", packet, e);
        }
    }

    @Override
    public void handle(@NotNull PlayerInfoPacket packet) {
        SemanticVersion serverVersion = SemanticVersion.parse(voiceServer.getVersion());
        SemanticVersion clientVersion = SemanticVersion.parse(packet.getVersion());

        if (clientVersion.major() != serverVersion.major()) {
            ServerVersionUtil.suggestSupportedVersion(player, serverVersion, packet.getMinecraftVersion());
            return;
        }

        BaseVoicePlayer<?> voicePlayer = (BaseVoicePlayer<?>) player;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(packet.getPublicKey());

            voicePlayer.setPublicKey(keyFactory.generatePublic(publicKeySpec));
        } catch (Exception e) {
            BaseVoice.LOGGER.error("Failed to generate RSA public key: {}", e.toString());
            e.printStackTrace();
            return;
        }

        voicePlayer.setVoiceDisabled(packet.isVoiceDisabled());
        voicePlayer.setMicrophoneMuted(packet.isMicrophoneMuted());

        tcpConnections.connect(player);
    }

    @Override
    public void handle(@NotNull PlayerStatePacket packet) {
        if (!player.hasVoiceChat()) return;

        BaseVoicePlayer<?> voicePlayer = (BaseVoicePlayer<?>) player;
        voicePlayer.setVoiceDisabled(packet.isVoiceDisabled());
        voicePlayer.setMicrophoneMuted(packet.isMicrophoneMuted());

        tcpConnections.broadcastPlayerInfoUpdate(player);
    }

    @Override
    public void handle(@NotNull PlayerActivationDistancesPacket packet) {
        BaseVoicePlayer<?> voicePlayer = (BaseVoicePlayer<?>) player;
        packet.getDistanceByActivationId().forEach((activationId, distance) -> {
            Optional<ServerActivation> activation = voiceServer.getActivationManager().getActivationById(activationId);
            if (!activation.isPresent()) return;

            voicePlayer.setActivationDistance(activation.get(), distance);
        });
    }

    @Override
    public void handle(@NotNull PlayerAudioEndPacket packet) {
        if (!player.hasVoiceChat()) return;
        if (voiceServer.getMuteManager().getMute(player.getInstance().getUuid()).isPresent()) return;
        if (player.isMicrophoneMuted()) return;

        voiceServer.getEventBus().fire(new PlayerSpeakEndEvent(player, packet));
    }

    @Override
    public void handle(@NotNull SourceInfoRequestPacket packet) {
        if (!player.hasVoiceChat()) return;

        Optional<? extends ServerAudioSource<?>> source = voiceServer.getSourceLineManager()
                .getLines()
                .stream()
                .map(line -> line.getSourceById(packet.getSourceId()).orElse(null))
                .filter(Objects::nonNull)
                .findFirst();
        if (!source.isPresent()) return;

        if (source.get().notMatchFilters(player)) {
            BaseVoice.LOGGER.warn(
                    "{} tried to request a source {} to which he doesn't have access",
                    player.getInstance().getName(), source.get().getSourceInfo()
            );
            return;
        }

        player.sendPacket(new SourceInfoPacket(source.get().getSourceInfo()));
    }

    @Override
    public void handle(@NotNull LanguageRequestPacket packet) {
        player.sendPacket(new LanguagePacket(
                packet.getLanguage(),
                voiceServer.getLanguages().getClientLanguage(packet.getLanguage())
        ));
    }
}
