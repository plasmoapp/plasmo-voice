package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.audio.source.AudioSource;
import su.plo.voice.api.server.audio.line.BaseServerSourceLine;
import su.plo.voice.api.server.audio.provider.AudioFrameProvider;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.audio.source.SourceInfo;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Base interface for server audio sources.
 *
 * <p>
 *     <b>Source states</b>
 *     <br/>
 *     Some methods mark the source as "dirty" and increment its state.
 *     If a source is marked as "dirty", it will broadcast its updated information to the players before sending the next UDP packet.
 *     <br/><br/>
 *     If players go out of range and then return to the source's range,
 *     and if the state in the UDP packet differs from their source state,
 *     the players will request source information themselves.
 *     <br/><br/>
 *     If the source state is increased by more than 10 (e.g. in {@link #setStereo(boolean)}),
 *     there may be delays in receiving certain UDP packets.
 *     This could result in packet drops and potential audio distortion.
 * </p>
 */
public interface ServerAudioSource<S extends SourceInfo> extends AudioSource<S> {

    /**
     * Gets the addon associated with this audio source.
     *
     * @return The addon.
     */
    @NotNull AddonContainer getAddon();

    /**
     * Gets the unique identifier of this audio source.
     *
     * @return The audio source unique identifier.
     */
    @NotNull UUID getId();

    /**
     * Gets the server source line to which this audio source belongs.
     *
     * @return The source line.
     */
    @NotNull BaseServerSourceLine getLine();

    /**
     * Gets the state of this audio source.
     *
     * @return The state.
     */
    int getState();

    /**
     * Sets whether the icon for this audio source is visible in the world.
     *
     * <p>
     *     This method sets marks as "dirty" and increases its state by 10.
     * </p>
     *
     * @param visible {@code true} if the icon should be visible, {@code false} otherwise.
     */
    void setIconVisible(boolean visible);

    /**
     * Sets the source mode to stereo.
     *
     * <p>
     *     This method sets marks as "dirty" and increases its state by 10.
     * </p>
     *
     * @param stereo Whether the source is stereo.
     */
    void setStereo(boolean stereo);

    /**
     * Sets the source name that will be visible in the overlay.
     *
     * <p>
     *     This method sets marks as "dirty" and increases its state by 1.
     * </p>
     *
     * @param name The source name.
     */
    void setName(@Nullable String name);

    /**
     * Marks the source as "dirty".
     */
    void setDirty();

    /**
     * Checks if the icon for this audio source is visible in the world.
     *
     * @return {@code true} if the icon is visible, {@code false} otherwise.
     */
    boolean isIconVisible();

    /**
     * Adds a new player filter to the source.
     *
     * @param filter Return {@code true} if you want to send source packets to the player.
     */
    <P extends VoicePlayer> void addFilter(@NotNull Predicate<? super P> filter);

    /**
     * Removes a player filter from the source.
     *
     * @param filter The player predicate to remove.
     */
    <P extends VoicePlayer> void removeFilter(@NotNull Predicate<? super P> filter);

    /**
     * Gets the collection of player filters associated with this source.
     *
     * @return The collection of player filters.
     */
    @NotNull Collection<Predicate<? super VoicePlayer>> getFilters();

    /**
     * Clears all filters from the source.
     */
    void clearFilters();

    /**
     * Removes the source from its associated source line.
     */
    default void remove() {
        getLine().removeSource(this);
    }

    /**
     * Checks if a player matches all filters associated with this source.
     *
     * @param player The player to check.
     * @return {@code true} if the player matches all filters, {@code false} otherwise.
     * @see ServerAudioSource#addFilter(Predicate)
     * @see ServerAudioSource#removeFilter(Predicate)
     * @see ServerAudioSource#getFilters()
     */
    <P extends VoicePlayer> boolean matchFilters(@NotNull P player);

    /**
     * Checks if a player does not match any of the filters associated with this source.
     *
     * @param player The player to check.
     * @return {@code true} if the player does not match any filter, {@code false} otherwise.
     * @see ServerAudioSource#addFilter(Predicate)
     * @see ServerAudioSource#removeFilter(Predicate)
     * @see ServerAudioSource#getFilters()
     */
    default <P extends VoicePlayer> boolean notMatchFilters(@NotNull P player) {
        return !matchFilters(player);
    }
}
