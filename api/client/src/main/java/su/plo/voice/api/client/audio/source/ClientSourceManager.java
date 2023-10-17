package su.plo.voice.api.client.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.source.AudioSourceManager;
import su.plo.voice.proto.data.audio.source.EntitySourceInfo;
import su.plo.voice.proto.data.audio.source.PlayerSourceInfo;
import su.plo.voice.proto.data.audio.source.SelfSourceInfo;
import su.plo.voice.proto.data.audio.source.SourceInfo;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages client audio sources.
 */
public interface ClientSourceManager extends AudioSourceManager<ClientAudioSource<?>> {

    /**
     * Creates a loopback source.
     *
     * @param relative {@code true} if the loopback source is relative, {@code false} otherwise.
     * @return the created loopback source.
     */
    @NotNull LoopbackSource createLoopbackSource(boolean relative);

    /**
     * Retrieves a client audio source by its unique identifier.
     *
     * @param sourceId     The unique identifier of the source.
     * @param request      {@code true} to send a request if the source doesn't exist, {@code false} otherwise.
     * @return An optional containing the client audio source if found, or empty if not found.
     */
    Optional<ClientAudioSource<?>> getSourceById(@NotNull UUID sourceId, boolean request);

    /**
     * Retrieves all client audio sources associated with a specified line identifier.
     *
     * @param lineId The unique identifier of the source line.
     * @return A collection of client audio sources associated with the specified source line.
     */
    Collection<ClientAudioSource<?>> getSourcesByLineId(@NotNull UUID lineId);

    /**
     * Retrieves all client audio sources associated with a specific entity.
     *
     * @param entityId The id of the entity.
     * @return A collection of client audio sources associated with the specified entity.
     */
    Collection<ClientAudioSource<EntitySourceInfo>> getEntitySources(int entityId);

    /**
     * Retrieves all client audio sources associated with a specific player.
     *
     * @param playerId The unique identifier of the player.
     * @return A collection of client audio sources associated with the specified player.
     */
    Collection<ClientAudioSource<PlayerSourceInfo>> getPlayerSources(@NotNull UUID playerId);

    /**
     * Creates or updates a client audio source based on the provided source information.
     *
     * @param sourceInfo The source information to create or update the source.
     */
    void createOrUpdateSource(@NotNull SourceInfo sourceInfo);

    /**
     * Sends a source information request for the specified source identifier.
     *
     * @param sourceId        The unique identifier of the source.
     * @param requestIfExist  {@code true} to send the request if the source already exists, {@code false} otherwise.
     */
    void sendSourceInfoRequest(@NotNull UUID sourceId, boolean requestIfExist);

    /**
     * Sends a source information request for the specified source identifier.
     *
     * @param sourceId The unique identifier of the source.
     */
    default void sendSourceInfoRequest(@NotNull UUID sourceId) {
        sendSourceInfoRequest(sourceId, false);
    }

    /**
     * Updates the client self source information.
     *
     * @param selfSourceInfo The self source information to update.
     *
     * @see ClientSelfSourceInfo
     */
    void updateSelfSourceInfo(@NotNull SelfSourceInfo selfSourceInfo);

    /**
     * Retrieves the client self source information for the specified source identifier.
     *
     * @param sourceId The unique identifier of the source.
     * @return An optional containing the client self source information if found, or empty if not found.
     */
    Optional<ClientSelfSourceInfo> getSelfSourceInfo(@NotNull UUID sourceId);

    /**
     * Retrieves a collection of {@link ClientSelfSourceInfo}.
     *
     * @return A collection of {@link ClientSelfSourceInfo}.
     */
    Collection<? extends ClientSelfSourceInfo> getAllSelfSourceInfos();
}
