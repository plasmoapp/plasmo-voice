package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.audio.source.AudioSource;
import su.plo.voice.api.server.audio.line.BaseServerSourceLine;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.audio.source.SourceInfo;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;

public interface ServerAudioSource<S extends SourceInfo> extends AudioSource<S> {

    @NotNull AddonContainer getAddon();

    @NotNull UUID getId();

    @NotNull BaseServerSourceLine getLine();

    int getState();

    void setAngle(int angle);

    void setIconVisible(boolean visible);

    void setStereo(boolean stereo);

    /**
     * Sets the source name that will be visible in overlay
     */
    void setName(@Nullable String name);

    /**
     * Marks source as dirty.
     * On next received packet, source will send SourceInfoPacket to all listeners
     */
    void setDirty();

    boolean isIconVisible();

    void addFilter(Predicate<VoicePlayer> filter);

    void removeFilter(Predicate<VoicePlayer> filter);

    @NotNull Collection<Predicate<VoicePlayer>> getFilters();

    void clearFilters();

    /**
     * Removes source from {@link #getLine()}
     */
    default void remove() {
        getLine().removeSource(this);
    }

    /**
     * @param player player to check
     * @return true if player matching all filters
     * @see ServerAudioSource#addFilter(Predicate)
     * @see ServerAudioSource#removeFilter(Predicate)
     * @see ServerAudioSource#getFilters()
     */
    boolean matchFilters(@NotNull VoicePlayer player);

    /**
     * @param player player to check
     * @return true if player not matching any filter
     * @see ServerAudioSource#addFilter(Predicate)
     * @see ServerAudioSource#removeFilter(Predicate)
     * @see ServerAudioSource#getFilters()
     */
    default boolean notMatchFilters(@NotNull VoicePlayer player) {
        return !matchFilters(player);
    }
}
