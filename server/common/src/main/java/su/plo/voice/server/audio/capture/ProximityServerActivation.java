package su.plo.voice.server.audio.capture;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.server.permission.PermissionDefault;
import su.plo.voice.api.event.EventPriority;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.audio.source.ServerPlayerSource;
import su.plo.voice.api.server.event.audio.capture.ServerActivationRegisterEvent;
import su.plo.voice.api.server.event.audio.capture.ServerActivationUnregisterEvent;
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEndEvent;
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerAudioEndPacket;
import su.plo.voice.proto.packets.udp.cllientbound.SourceAudioPacket;
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket;
import su.plo.voice.server.config.ServerConfig;

import java.util.Optional;
import java.util.UUID;

public final class ProximityServerActivation {

    private final PlasmoVoiceServer voiceServer;

    public ProximityServerActivation(@NotNull PlasmoVoiceServer voiceServer) {
        this.voiceServer = voiceServer;
    }

    public void register(@NotNull ServerConfig config) {
        voiceServer.getActivationManager().register(
                voiceServer,
                VoiceActivation.PROXIMITY_NAME,
                "key.plasmovoice.proximity",
                "plasmovoice:textures/icons/microphone.png",
                config.getVoice().getDistances(),
                config.getVoice().getDefaultDistance(),
                true,
                false,
                1
        );
    }

    @EventSubscribe
    public void onPlayerSpeak(@NotNull PlayerSpeakEvent event) {
        PlayerAudioPacket packet = event.getPacket();

        getPlayerSource(event.getPlayer(), packet.getActivationId(), packet.isStereo()).ifPresent((source) -> {
            SourceAudioPacket sourcePacket = new SourceAudioPacket(
                    packet.getSequenceNumber(),
                    (byte) source.getState(),
                    packet.getData(),
                    source.getId(),
                    packet.getDistance()
            );
            source.sendAudioPacket(sourcePacket, packet.getDistance());
        });
    }

    @EventSubscribe
    public void onPlayerSpeakEnd(@NotNull PlayerSpeakEndEvent event) {
        PlayerAudioEndPacket packet = event.getPacket();

        getPlayerSource(event.getPlayer(), packet.getActivationId(), true).ifPresent((source) -> {
            SourceAudioEndPacket sourcePacket = new SourceAudioEndPacket(source.getId(), packet.getSequenceNumber());
            source.sendPacket(sourcePacket, packet.getDistance());
        });
    }

    @EventSubscribe(priority = EventPriority.HIGHEST)
    public void onActivationRegister(@NotNull ServerActivationRegisterEvent event) {
        if (event.isCancelled()) return;

        ServerActivation activation = event.getActivation();
        if (activation.getName().equals(VoiceActivation.PROXIMITY_NAME)) {
            voiceServer.getMinecraftServer()
                    .getPermissionsManager()
                    .register("voice.activation." + activation.getName(), PermissionDefault.TRUE);
        }
    }

    @EventSubscribe(priority = EventPriority.HIGHEST)
    public void onActivationUnregister(@NotNull ServerActivationUnregisterEvent event) {
        if (event.isCancelled()) return;

        ServerActivation activation = event.getActivation();
        if (activation.getName().equals(VoiceActivation.PROXIMITY_NAME)) {
            voiceServer.getMinecraftServer()
                    .getPermissionsManager()
                    .unregister("voice.activation." + activation.getName());
        }
    }

    private Optional<ServerPlayerSource> getPlayerSource(@NotNull VoicePlayer player,
                                                         @NotNull UUID activationId,
                                                         boolean isStereo) {
        if (!activationId.equals(VoiceActivation.PROXIMITY_ID)) return Optional.empty();

        Optional<ServerActivation> activation = voiceServer.getActivationManager()
                .getActivationById(activationId);
        if (!activation.isPresent()) return Optional.empty();

        Optional<ServerSourceLine> sourceLine = voiceServer.getSourceLineManager()
                .getLineById(VoiceSourceLine.PROXIMITY_ID);
        if (!sourceLine.isPresent()) return Optional.empty();

        isStereo = isStereo && activation.get().isStereoSupported();
        ServerPlayerSource source = voiceServer.getSourceManager().createPlayerSource(
                voiceServer,
                player,
                sourceLine.get(),
                "opus",
                isStereo
        );
        source.setStereo(isStereo);

        return Optional.of(source);
    }
}