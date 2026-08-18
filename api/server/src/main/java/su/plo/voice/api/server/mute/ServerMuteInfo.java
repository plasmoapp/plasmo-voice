package su.plo.voice.api.server.mute;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Player's mute information.
 */
@AllArgsConstructor
public class ServerMuteInfo {

    private final @NotNull UUID playerUUID;
    private final @Nullable UUID mutedByPlayerUUID;
    private final long mutedAtTime;
    private final long mutedToTime;
    private final @Nullable String reason;
    private final @Nullable Boolean silent;

    /**
     * Backward-compatible constructor that leaves {@link #getSilent()} unspecified (null).
     *
     * @deprecated use {@link #ServerMuteInfo(UUID, UUID, long, long, String, Boolean)}.
     */
    @Deprecated
    public ServerMuteInfo(@NotNull UUID playerUUID,
                          @Nullable UUID mutedByPlayerUUID,
                          long mutedAtTime,
                          long mutedToTime,
                          @Nullable String reason) {
        this(playerUUID, mutedByPlayerUUID, mutedAtTime, mutedToTime, reason, null);
    }

    /**
     * Gets the UUID of the player who is muted.
     *
     * @return The UUID of the player.
     */
    public @NotNull UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Gets the UUID of the player who performed the mute action.
     *
     * @return The UUID of the muting player.
     */
    public @Nullable UUID getMutedByPlayerUUID() {
        return mutedByPlayerUUID;
    }

    /**
     * Gets the timestamp at which the player was muted.
     *
     * @return The timestamp in milliseconds when the mute action occurred.
     */
    public long getMutedAtTime() {
        return mutedAtTime;
    }

    /**
     * Gets the timestamp until which the player is muted.
     *
     * @return The timestamp in milliseconds until which the player is muted. If permanent, this value will be 0.
     */
    public long getMutedToTime() {
        return mutedToTime;
    }

    /**
     * Gets the reason for the mute.
     *
     * @return The reason for muting the player, or null if no reason was provided.
     */
    public @Nullable String getReason() {
        return reason;
    }

    /**
     * Gets whether the player was muted silently, i.e. without receiving a notification.
     *
     * @return {@code true}/{@code false} if specified at mute time,
     * or {@code null} if unspecified (e.g. mutes stored before this field existed).
     * The expiry ticker treats {@code null} by falling back to the server's
     * {@code notifications.unmuted} config value.
     */
    public @Nullable Boolean getSilent() {
        return silent;
    }
}
