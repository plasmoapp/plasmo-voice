package su.plo.voice.api.server.mute;

import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class ServerMuteInfo {

    private final UUID playerUUID;
    private final UUID mutedByPlayerUUID;
    private final long mutedAtTime;
    private final long mutedToTime;
    private final String reason;

    /**
     * Gets the player uuid
     *
     * @return the player uuid
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Gets the player uuid who muted the player
     *
     * @return the player uuid
     */
    public UUID getMutedByPlayerUUID() {
        return mutedByPlayerUUID;
    }

    /**
     * Gets the timestamp at which the player was muted
     *
     * @return the timestamp is ms
     */
    public long getMutedAtTime() {
        return mutedAtTime;
    }

    /**
     * Gets the timestamp until which the player is muted
     *
     * @return the timestamp in ms
     */
    public long getMutedToTime() {
        return mutedToTime;
    }

    /**
     * Gets the mute reason
     *
     * @return the reason
     */
    public String getReason() {
        return reason;
    }
}
