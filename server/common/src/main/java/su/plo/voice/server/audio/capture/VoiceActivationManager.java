package su.plo.voice.server.audio.capture;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.audio.capture.ActivationManager;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.proto.data.capture.Activation;
import su.plo.voice.proto.data.capture.VoiceActivation;
import su.plo.voice.server.config.ServerConfig;

import java.util.*;

public final class VoiceActivationManager implements ActivationManager {

    private final ServerActivation proximityActivation;
    private final Map<UUID, ServerActivation> activationById = Maps.newConcurrentMap();

    public VoiceActivationManager(ServerConfig.Voice voiceConfig) {
        this.proximityActivation = new VoiceServerActivation(
                "proximity",
                "gui.plasmo_voice.proximity",
                voiceConfig.getDistances(),
                voiceConfig.getDefaultDistance(),
                true,
                Activation.Order.NORMAL
        );
    }

    @Override
    public @NotNull ServerActivation getProximityActivation() {
        return proximityActivation;
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
    public @NotNull ServerActivation register(@NotNull String name, List<Integer> distances, int defaultDistance, boolean transitive, Activation.Order order) {
        return activationById.computeIfAbsent(
                VoiceActivation.generateId(name),
                (id) -> new VoiceServerActivation(
                        "proximity",
                        "gui.plasmo_voice.proximity",
                        distances,
                        defaultDistance,
                        transitive,
                        Activation.Order.NORMAL
                )
        );
    }

    @Override
    public boolean unregister(@NotNull UUID id) {
        return activationById.remove(id) != null;
    }

    @Override
    public boolean unregister(@NotNull String name) {
        return activationById.remove(VoiceActivation.generateId(name)) != null;
    }

    @Override
    public boolean unregister(@NotNull ServerActivation activation) {
        return activationById.remove(activation.getId()) != null;
    }
}
