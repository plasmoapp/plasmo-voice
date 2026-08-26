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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PlayerChannelHandler implements ServerPacketTcpHandler {

    private static final long STATE_BROADCAST_INTERVAL_MS = 250L;
    private static final long LANGUAGE_RESPONSE_INTERVAL_MS = 1_000L;

    private final PlasmoVoiceServer voiceServer;
    private final TcpServerPacketManager tcpConnections;
    private final VoiceServerPlayer player;

    private final AtomicBoolean stateBroadcastScheduled = new AtomicBoolean();
    private volatile long lastStateBroadcast;

    private final AtomicBoolean languageResponseScheduled = new AtomicBoolean();
    private volatile long lastLanguageResponse;
    private volatile String requestedLanguage;

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
            ServerVersionUtil.suggestSupportedVersion(player, packet.getMinecraftVersion());
            return;
        }

        SemanticVersion minVersion = SemanticVersion.parse("2.0.0");
        try {
            minVersion = SemanticVersion.parse(voiceServer.getConfig().voice().clientModMinVersion());
        } catch (IllegalArgumentException ignored) {
        }

        if (clientVersion.asInt() < minVersion.asInt()) {
            ServerVersionUtil.suggestSupportedVersion(player, packet.getMinecraftVersion());
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
        voicePlayer.setModVersion(packet.getVersion());

        tcpConnections.connect(player);
    }

    @Override
    public void handle(@NotNull PlayerStatePacket packet) {
        if (!player.hasVoiceChat()) return;

        BaseVoicePlayer<?> voicePlayer = (BaseVoicePlayer<?>) player;

        boolean voiceDisabledChanged = voicePlayer.setVoiceDisabled(packet.isVoiceDisabled());
        boolean microphoneMutedChanged = voicePlayer.setMicrophoneMuted(packet.isMicrophoneMuted());

        if (!voiceDisabledChanged && !microphoneMutedChanged) return;

        long elapsed = System.currentTimeMillis() - lastStateBroadcast;
        if (elapsed >= STATE_BROADCAST_INTERVAL_MS) {
            broadcastPlayerState();
            return;
        }

        if (!stateBroadcastScheduled.compareAndSet(false, true)) return;

        scheduleInMainThread(this::flushPlayerState, STATE_BROADCAST_INTERVAL_MS - elapsed);
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
            if (BaseVoice.DEBUG_LOGGER.enabled()) {
                BaseVoice.DEBUG_LOGGER.warn(
                        "{} tried to request a source {} to which he doesn't have access",
                        player.getInstance().getName(), source.get().getSourceInfo()
                );
            }
            return;
        }

        source.get()
                .resolveSourceInfo()
                .thenAccept(sourceInfo -> player.sendPacket(new SourceInfoPacket(sourceInfo)));
    }

    @Override
    public void handle(@NotNull LanguageRequestPacket packet) {
        this.requestedLanguage = packet.getLanguage();

        long elapsed = System.currentTimeMillis() - lastLanguageResponse;
        if (elapsed >= LANGUAGE_RESPONSE_INTERVAL_MS) {
            sendLanguage();
            return;
        }

        if (!languageResponseScheduled.compareAndSet(false, true)) return;

        scheduleInMainThread(this::flushLanguage, LANGUAGE_RESPONSE_INTERVAL_MS - elapsed);
    }

    private void broadcastPlayerState() {
        this.lastStateBroadcast = System.currentTimeMillis();

        tcpConnections.broadcastPlayerInfoUpdate(player);
    }

    private void flushPlayerState() {
        stateBroadcastScheduled.set(false);
        if (!player.hasVoiceChat()) return;

        broadcastPlayerState();
    }

    private void sendLanguage() {
        String language = requestedLanguage;
        if (language == null) return;

        this.lastLanguageResponse = System.currentTimeMillis();

        player.sendPacket(new LanguagePacket(
                language,
                voiceServer.getLanguages().getClientLanguage(language)
        ));
    }

    private void flushLanguage() {
        languageResponseScheduled.set(false);

        sendLanguage();
    }

    private void scheduleInMainThread(@NotNull Runnable runnable, long delayMs) {
        voiceServer.getBackgroundExecutor().schedule(
                () -> voiceServer.getMinecraftServer().executeInMainThread(runnable),
                delayMs,
                TimeUnit.MILLISECONDS
        );
    }
}
