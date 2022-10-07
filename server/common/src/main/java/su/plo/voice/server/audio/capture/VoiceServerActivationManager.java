package su.plo.voice.server.audio.capture;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.addon.AddonManager;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.api.server.audio.capture.ServerActivationManager;
import su.plo.voice.api.server.event.audio.capture.ServerActivationRegisterEvent;
import su.plo.voice.api.server.event.audio.capture.ServerActivationUnregisterEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.player.VoicePlayerManager;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;
import su.plo.voice.proto.packets.tcp.clientbound.ActivationRegisterPacket;
import su.plo.voice.proto.packets.tcp.clientbound.ActivationUnregisterPacket;
import su.plo.voice.server.player.VoiceServerPlayer;

import java.util.*;

public final class VoiceServerActivationManager implements ServerActivationManager {

    private final PlasmoVoiceServer voiceServer;
    private final AddonManager addons;
    private final VoicePlayerManager players;
    private final Map<UUID, ServerActivation> activationById = Maps.newConcurrentMap();

    public VoiceServerActivationManager(@NotNull PlasmoVoiceServer voiceServer) {
        this.voiceServer = voiceServer;
        this.addons = voiceServer.getAddonManager();
        this.players = voiceServer.getPlayerManager();
    }

    @Override
    public Optional<ServerActivation> getActivationById(@NotNull UUID id) {
        return Optional.ofNullable(activationById.get(id));
    }

    @Override
    public Optional<ServerActivation> getActivationByName(@NotNull String name) {
        return Optional.ofNullable(activationById.get(VoiceActivation.generateId(name)));
    }

    @Override
    public Collection<ServerActivation> getActivations() {
        return activationById.values();
    }

    @Override
    public Optional<ServerActivation> register(@NotNull Object addonObject,
                                              @NotNull String name,
                                              @NotNull String translation,
                                              @NotNull String icon,
                                              List<Integer> distances,
                                              int defaultDistance,
                                              boolean transitive,
                                              boolean stereoSupported,
                                              int weight) {
        Optional<AddonContainer> addon = addons.getAddon(addonObject);
        if (!addon.isPresent()) throw new IllegalArgumentException("addonObject is not an addon");

        VoiceServerActivation activation = (VoiceServerActivation) activationById.get(VoiceActivation.generateId(name));
        if (activation != null) return Optional.of(activation);

        activation = new VoiceServerActivation(
                voiceServer,
                addon.get(),
                name,
                translation,
                icon,
                distances,
                defaultDistance,
                transitive,
                stereoSupported,
                weight
        );

        ServerActivationRegisterEvent event = new ServerActivationRegisterEvent(activation);
        voiceServer.getEventBus().call(event);
        if (event.isCancelled()) return Optional.empty();

        activationById.put(activation.getId(), activation);

        voiceServer.getTcpConnectionManager()
                .broadcast(new ActivationRegisterPacket(activation));

        voiceServer.getPlayerManager().registerPermission("voice.activation." + name);

        return Optional.of(activation);
    }

    @Override
    public boolean unregister(@NotNull UUID id) {
        ServerActivation activation = activationById.get(id);
        if (activation != null) {
            ServerActivationUnregisterEvent event = new ServerActivationUnregisterEvent(activation);
            voiceServer.getEventBus().call(event);
            if (event.isCancelled()) return false;

            activationById.remove(id);

            players.getPlayers()
                    .stream()
                    .filter(VoicePlayer::hasVoiceChat)
                    .forEach((player) -> ((VoiceServerPlayer) player).removeActivationDistance(activation));

            voiceServer.getTcpConnectionManager()
                    .broadcast(new ActivationUnregisterPacket(activation.getId()));

            String activationPermission = "voice.activation." + activation.getName();
            voiceServer.getPlayerManager().unregisterPermission(activationPermission);

            return true;
        }

        return false;
    }

    @Override
    public boolean unregister(@NotNull String name) {
        return unregister(VoiceActivation.generateId(name));
    }

    @Override
    public boolean unregister(@NotNull ServerActivation activation) {
        return unregister(activation.getId());
    }

    @Override
    public void clear() {
        activationById.values().forEach(this::unregister);
        activationById.clear();
    }
}
