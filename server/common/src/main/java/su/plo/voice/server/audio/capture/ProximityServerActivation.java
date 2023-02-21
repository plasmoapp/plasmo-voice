package su.plo.voice.server.audio.capture;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.permission.PermissionDefault;
import su.plo.voice.api.event.EventPriority;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.capture.BaseProximityServerActivation;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEndEvent;
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEvent;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerAudioEndPacket;
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket;
import su.plo.voice.server.config.VoiceServerConfig;

public final class ProximityServerActivation extends BaseProximityServerActivation {

    private ServerActivation activation;

    public ProximityServerActivation(@NotNull PlasmoVoiceServer voiceServer) {
        super(voiceServer, "proximity", PermissionDefault.TRUE);
    }

    public void register(@NotNull VoiceServerConfig config) {
        voiceServer.getActivationManager().unregister(VoiceActivation.PROXIMITY_ID);
        voiceServer.getSourceLineManager().unregister(VoiceActivation.PROXIMITY_ID);

        ServerActivation.Builder builder = voiceServer.getActivationManager().createBuilder(
                voiceServer,
                VoiceActivation.PROXIMITY_NAME,
                "pv.activation.proximity",
                "plasmovoice:textures/icons/microphone.png",
                "pv.activation.proximity",
                1
        );
        this.activation = builder
                .setDistances(config.voice().proximity().distances())
                .setDefaultDistance(config.voice().proximity().defaultDistance())
                .setProximity(true)
                .setTransitive(true)
                .setStereoSupported(false)
                .build();

        voiceServer.getSourceLineManager().register(
                voiceServer,
                VoiceSourceLine.PROXIMITY_NAME,
                "pv.activation.proximity",
                "plasmovoice:textures/icons/speaker.png",
                1
        );
    }

    @EventSubscribe(priority = EventPriority.HIGHEST)
    public void onPlayerSpeak(@NotNull PlayerSpeakEvent event) {
        if (activation == null) return;

        VoiceServerPlayer player = (VoiceServerPlayer) event.getPlayer();
        PlayerAudioPacket packet = event.getPacket();

        if (event.isCancelled() ||
                !activation.checkPermissions(player) ||
                voiceServer.getConfig() == null ||
                !voiceServer.getConfig()
                        .voice()
                        .proximity()
                        .distances()
                        .contains((int) packet.getDistance())
        ) return;

        getPlayerSource(player, packet.getActivationId(), packet.isStereo())
                .ifPresent((source) -> sendAudioPacket(player, source, packet));
    }

    @EventSubscribe(priority = EventPriority.HIGHEST)
    public void onPlayerSpeakEnd(@NotNull PlayerSpeakEndEvent event) {
        if (activation == null) return;

        VoiceServerPlayer player = (VoiceServerPlayer) event.getPlayer();
        PlayerAudioEndPacket packet = event.getPacket();

        if (event.isCancelled() ||
                !activation.checkPermissions(player) ||
                voiceServer.getConfig() == null ||
                !voiceServer.getConfig()
                        .voice()
                        .proximity()
                        .distances()
                        .contains((int) packet.getDistance())
        ) return;

        getPlayerSource(player, packet.getActivationId(), null)
                .ifPresent((source) -> sendAudioEndPacket(source, packet));
    }
}
