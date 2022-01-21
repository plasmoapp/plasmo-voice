package su.plo.voice;

import org.jetbrains.annotations.ApiStatus;
import su.plo.voice.data.ServerMutedEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PlasmoVoiceAPI {
    /**
     * Mute the player
     *
     * @param player       Player UUID
     * @param duration     Duration of the mute in durationTime
     * @param durationUnit DurationUnit, can be null, if duration is 0
     * @param reason       Reason for the mute
     * @param silent       If true, the player won't see the message about the mute
     */
    void mute(UUID player, long duration, @Nullable DurationUnit durationUnit, @Nullable String reason, boolean silent);

    /**
     * Unmute the player
     *
     * @param player Player UUID
     * @param silent if true, player won't see the message that they are no longer muted
     * @return returns true, if the player is no longer muted. false, if the player wasn't muted, to begin with
     */
    boolean unmute(UUID player, boolean silent);

    /**
     * Check if the player is muted
     *
     * @param player Player UUID
     * @return true if true, false if false 5Head
     */
    boolean isMuted(UUID player);

    /**
     * Map of the muted players
     *
     * @return Map (key - Player UUID, value - ServerMutedEntity)
     */
    Map<UUID, ServerMutedEntity> getMutedMap();

    /**
     * @param player Online player UUID
     * @return true, if the player has the mod installed
     */
    boolean hasVoiceChat(UUID player);

    /**
     * Returns ModLoader of the player
     *
     * @param player UUID игрока онлайн
     * @return fabric/forge or null, if the player doesn't have the mod installed
     */
    @Nullable
    String getPlayerModLoader(UUID player);

    /**
     * @return List of UUIDs of the players with the mod installed
     */
    List<UUID> getPlayers();

    /**
     * Set player voice distances
     */
    @ApiStatus.Experimental
    void setVoiceDistances(UUID playerId, List<Integer> distances, Integer defaultDistance, Integer fadeDivisor);

    enum DurationUnit {
        SECONDS,
        MINUTES,
        HOURS,
        DAYS,
        WEEKS,
        TIMESTAMP;

        public long multiply(long duration) {
            switch (this) {
                case MINUTES:
                    return duration * 60;
                case HOURS:
                    return duration * 3600;
                case DAYS:
                    return duration * 86400;
                case WEEKS:
                    return duration * 604800;
                default:
                    return duration;
            }
        }

        public String format(long duration) {
            switch (this) {
                case MINUTES:
                    return String.format(PlasmoVoice.getInstance().getMessage("mute_durations.minutes"), duration);
                case HOURS:
                    return String.format(PlasmoVoice.getInstance().getMessage("mute_durations.hours"), duration);
                case DAYS:
                    return String.format(PlasmoVoice.getInstance().getMessage("mute_durations.days"), duration);
                case WEEKS:
                    return String.format(PlasmoVoice.getInstance().getMessage("mute_durations.weeks"), duration);
                default:
                    return String.format(PlasmoVoice.getInstance().getMessage("mute_durations.seconds"), duration);
            }
        }
    }
}
