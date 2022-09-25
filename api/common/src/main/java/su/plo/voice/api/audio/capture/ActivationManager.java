package su.plo.voice.api.audio.capture;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.audio.capture.Activation;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ActivationManager<T extends Activation> {

    Optional<T> getActivationById(@NotNull UUID id);

    Optional<T> getActivationByName(@NotNull String name);

    Collection<T> getActivations();

    boolean unregister(@NotNull UUID id);

    boolean unregister(@NotNull String name);

    boolean unregister(@NotNull T activation);

    void clear();
}
