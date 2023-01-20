package su.plo.voice.server.audio.capture;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.permission.PermissionDefault;
import su.plo.voice.api.event.EventPriority;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.capture.BaseProximityServerActivation;
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEndEvent;
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEvent;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerAudioEndPacket;
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket;
import su.plo.voice.server.config.VoiceServerConfig;

public final class ProximityServerActivation extends BaseProximityServerActivation {

    public ProximityServerActivation(@NotNull PlasmoVoiceServer voiceServer) {
        super(voiceServer, "proximity", PermissionDefault.TRUE);
    }

    public void register(@NotNull VoiceServerConfig config) {
        voiceServer.getActivationManager().register(
                voiceServer,
                VoiceActivation.PROXIMITY_NAME,
                "activation.plasmovoice.proximity",
                "plasmovoice:textures/icons/microphone.png",
                config.voice().proximity().distances(),
                config.voice().proximity().defaultDistance(),
                true,
                true,
                false,
                1
        );

        voiceServer.getSourceLineManager().register(
                voiceServer,
                VoiceSourceLine.PROXIMITY_NAME,
                "activation.plasmovoice.proximity",
                "plasmovoice:textures/icons/speaker.png",
                1
        );
    }

    @EventSubscribe(priority = EventPriority.HIGHEST)
    public void onPlayerSpeak(@NotNull PlayerSpeakEvent event) {
        VoiceServerPlayer player = (VoiceServerPlayer) event.getPlayer();
        PlayerAudioPacket packet = event.getPacket();

        if (event.isCancelled() ||
                !player.getInstance().hasPermission(getActivationPermission()) ||
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
        VoiceServerPlayer player = (VoiceServerPlayer) event.getPlayer();
        PlayerAudioEndPacket packet = event.getPacket();

        if (event.isCancelled() ||
                !player.getInstance().hasPermission(getActivationPermission()) ||
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
