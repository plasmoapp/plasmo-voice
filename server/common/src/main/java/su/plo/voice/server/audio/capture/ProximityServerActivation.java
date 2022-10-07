package su.plo.voice.server.audio.capture;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.server.permission.PermissionDefault;
import su.plo.voice.api.event.EventPriority;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.api.server.event.audio.capture.ServerActivationRegisterEvent;
import su.plo.voice.api.server.event.audio.capture.ServerActivationUnregisterEvent;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;
import su.plo.voice.server.config.ServerConfig;

@RequiredArgsConstructor
public final class ProximityServerActivation {

    private final PlasmoVoiceServer voiceServer;

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
}
