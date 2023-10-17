package su.plo.voice.api.client.audio.capture;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.capture.ActivationManager;
import su.plo.voice.proto.data.audio.capture.Activation;

import java.util.Collection;
import java.util.Optional;

/**
 * Manages client activations.
 */
public interface ClientActivationManager extends ActivationManager<ClientActivation> {

    /**
     * Gets the parent client activation.
     *
     * @return An optional containing the parent client activation, if available; otherwise, an empty optional.
     */
    Optional<ClientActivation> getParentActivation();

    /**
     * Registers a client activation and returns the registered instance.
     *
     * @param activation The client activation to register.
     * @return The registered client activation.
     */
    @NotNull ClientActivation register(@NotNull ClientActivation activation);

    /**
     * Registers a collection of client activations and returns the registered instances.
     *
     * @param activations The collection of client activations to register.
     * @return A collection of registered client activations.
     */
    @NotNull Collection<ClientActivation> register(@NotNull Collection<Activation> activations);
}
