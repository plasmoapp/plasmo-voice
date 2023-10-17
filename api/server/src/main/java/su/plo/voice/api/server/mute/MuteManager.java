package su.plo.voice.api.server.mute;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.mute.storage.MuteStorage;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages voice players mutes.
 */
public interface MuteManager {

    /**
     * Mutes a player.
     *
     * @param playerId     The UUID of the player to be muted.
     * @param mutedById    The UUID of the player who mutes the target player.
     * @param duration     The duration of the mute in durationTime.
     * @param durationUnit The unit of duration (e.g., seconds, minutes, hours).
     *                     If null and duration is 0, the mute will be permanent.
     * @param reason       The reason for the mute.
     * @param silent       If {@code true}, the player won't receive a notification about the mute.
     * @return An {@link Optional} containing {@link ServerMuteInfo} if the player was muted successfully, or empty otherwise.
     */
    Optional<ServerMuteInfo> mute(
            @NotNull UUID playerId,
            @Nullable UUID mutedById,
            long duration,
            @Nullable MuteDurationUnit durationUnit,
            @Nullable String reason,
            boolean silent
    );

    /**
     * Unmutes a player.
     *
     * @param playerId The UUID of the player to be unmuted.
     * @param silent   If {@code true}, the player won't receive a notification about being unmuted.
     * @return An {@link Optional} containing {@link ServerMuteInfo} if the player was previously muted and is now unmuted, or empty otherwise.
     */
    Optional<ServerMuteInfo> unmute(@NotNull UUID playerId, boolean silent);

    /**
     * Gets the mute information for a player.
     *
     * @param playerId The UUID of the player.
     * @return An {@link Optional} containing {@link ServerMuteInfo} if the player is muted, or empty otherwise.
     */
    Optional<ServerMuteInfo> getMute(@NotNull UUID playerId);

    /**
     * Gets the mute storage implementation used for storing mute information.
     *
     * @return The {@link MuteStorage} used for storing mute information.
     */
    @NotNull MuteStorage getMuteStorage();

    /**
     * Sets the mute storage implementation to be used for storing mute information.
     *
     * @param muteStorage The {@link MuteStorage} implementation to be set.
     */
    void setMuteStorage(@NotNull MuteStorage muteStorage);
}
