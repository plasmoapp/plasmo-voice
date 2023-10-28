package su.plo.voice.api.client.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.audio.source.SelfSourceInfo;

/**
 * Represents information about the source from which the current player is speaking using an activation.
 */
public interface ClientSelfSourceInfo {

    /**
     * Gets the self source info.
     *
     * @return The self source info.
     */
    @NotNull SelfSourceInfo getSelfSourceInfo();

    /**
     * Gets the current sequence number.
     *
     * @return The sequence number.
     */
    long getSequenceNumber();

    /**
     * Gets the current distance of the source.
     *
     * @return The distance.
     */
    short getDistance();

    /**
     * Gets the timestamp of the last update.
     *
     * @return The timestamp of the last update.
     */
    long getLastUpdate();
}
