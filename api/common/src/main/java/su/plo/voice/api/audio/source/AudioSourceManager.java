package su.plo.voice.api.audio.source;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages audio sources.
 *
 * @param <T> The type of audio source to manage.
 */
public interface AudioSourceManager<T extends AudioSource<?>> {

    /**
     * Retrieves an audio source by its unique identifier.
     *
     * @param sourceId The unique identifier of the audio source.
     * @return An optional containing the audio source if found, or an empty optional if not found.
     */
    Optional<T> getSourceById(@NotNull UUID sourceId);

    /**
     * Retrieves a collection of all registered audio sources.
     *
     * @return A collection of registered audio sources.
     */
    Collection<T> getSources();

    /**
     * Clears the manager, closing and removing all registered audio sources.
     */
    void clear();
}
