package su.plo.voice.api.server.mute;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface MuteManager {

    /**
     * Mute the player
     *
     * @param playerId     Player UUID
     * @param mutedById    Player UUID of the player who mutes the player
     * @param duration     Duration of the mute in durationTime
     * @param durationUnit DurationUnit, can be null, if duration is 0
     *                     mute will be permanent
     * @param reason       Reason for the mute
     * @param silent       If true, the player won't see the message about the mute
     * @return {@link ServerMuteInfo} if player was muted successfully
     */
    Optional<ServerMuteInfo> mute(@NotNull UUID playerId,
                                  @Nullable UUID mutedById,
                                  long duration,
                                  @Nullable MuteDurationUnit durationUnit,
                                  @Nullable String reason,
                                  boolean silent);

    /**
     * Unmute the player
     *
     * @param playerId Player UUID
     * @param silent   if true, player won't see the message that they are no longer muted
     * @return {@link ServerMuteInfo} if player was muted and now is no longer muted
     */
    Optional<ServerMuteInfo> unmute(@NotNull UUID playerId, boolean silent);

    /**
     * Gets the player mute info
     *
     * @param playerId Player UUID
     *
     * @return {@link ServerMuteInfo} is player is muted
     */
    Optional<ServerMuteInfo> getMute(@NotNull UUID playerId);

    /**
     * @return collection of the muted players
     */
    Collection<ServerMuteInfo> getMutedPlayers();
}
