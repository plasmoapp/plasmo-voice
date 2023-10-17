package su.plo.voice.api.audio.capture;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.audio.capture.Activation;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages activations.
 */
public interface ActivationManager<T extends Activation> {

    /**
     * Retrieves an activation by its unique identifier.
     *
     * @param id The unique ID of the activation to retrieve.
     * @return An optional containing the activation if found, or an empty optional if not found.
     */
    Optional<T> getActivationById(@NotNull UUID id);

    /**
     * Retrieves an activation by its name.
     *
     * @param name The name of the activation to retrieve.
     * @return An optional containing the activation if found, or an empty optional if not found.
     */
    Optional<T> getActivationByName(@NotNull String name);

    /**
     * Retrieves a collection of all activations managed by this manager.
     *
     * @return A collection of activations.
     */
    Collection<T> getActivations();

    /**
     * Unregisters an activation specified by its unique identifier.
     *
     * @param id The unique ID of the activation to unregister.
     * @return {@code true} if the activation was successfully unregistered, {@code false} if the activation was not found.
     */
    boolean unregister(@NotNull UUID id);

    /**
     * Unregisters an activation specified by its name.
     *
     * @param name The name of the activation to unregister.
     * @return {@code true} if the activation was successfully unregistered, {@code false} if the activation was not found.
     */
    default boolean unregister(@NotNull String name) {
        return unregister(VoiceActivation.generateId(name));
    }

    /**
     * Unregisters a given activation from the manager.
     *
     * @param activation The activation to unregister.
     * @return {@code true} if the activation was successfully unregistered, {@code false} if the activation was not found.
     */
    default boolean unregister(@NotNull T activation) {
        return unregister(activation.getId());
    }

    /**
     * Clears all activations from the manager.
     */
    void clear();
}
