package su.plo.voice.api.server.audio.capture;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// todo: doc
public interface ActivationManager {

    @NotNull ServerActivation getProximityActivation();

    Optional<ServerActivation> getActivationById(@NotNull UUID id);

    Optional<ServerActivation> getActivationByName(@NotNull String name);

    Collection<ServerActivation> getActivations();

    @NotNull ServerActivation register(@NotNull String name,
                                       @NotNull String translation,
                                       @NotNull String hudIconLocation,
                                       @NotNull String sourceIconLocation,
                                       List<Integer> distances,
                                       int defaultDistance,
                                       boolean transitive,
                                       int weight);

    boolean unregister(@NotNull UUID id);

    boolean unregister(@NotNull String name);

    boolean unregister(@NotNull ServerActivation activation);
}
