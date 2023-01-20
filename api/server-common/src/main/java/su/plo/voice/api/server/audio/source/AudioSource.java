package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.audio.source.SourceInfo;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;

public interface AudioSource<S extends SourceInfo, P extends VoicePlayer<?>> extends su.plo.voice.api.audio.source.AudioSource<S> {

    @NotNull AddonContainer getAddon();

    @NotNull UUID getId();

    @NotNull ServerSourceLine getLine();

    void setLine(@NotNull ServerSourceLine line);

    int getState();

    void setAngle(int angle);

    void setIconVisible(boolean visible);

    void setStereo(boolean stereo);

    /**
     * Marks source as dirty.
     * On next received packet, source will send SourceInfoPacket to all listeners
     */
    void setDirty();

    boolean isIconVisible();

    void addFilter(Predicate<P> filter);

    void removeFilter(Predicate<P> filter);

    @NotNull Collection<Predicate<P>> getFilters();

    void clearFilters();
}
