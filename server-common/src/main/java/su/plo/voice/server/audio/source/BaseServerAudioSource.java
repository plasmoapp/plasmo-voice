package su.plo.voice.server.audio.source;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.audio.source.ServerAudioSource;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.audio.source.SourceInfo;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public abstract class BaseServerAudioSource<S extends SourceInfo>
        implements ServerAudioSource<S> {

    @Getter
    protected final AddonContainer addon;
    @Getter
    protected final UUID id;
    protected final String codec;

    @Getter
    protected @NotNull ServerSourceLine line;
    @Getter
    protected boolean iconVisible = true;
    @Setter
    protected int angle;
    protected boolean stereo;

    protected final AtomicBoolean dirty = new AtomicBoolean(true);
    protected final AtomicInteger state = new AtomicInteger(1);

    protected final List<Predicate<VoicePlayer>> filters = new CopyOnWriteArrayList<>();

    protected BaseServerAudioSource(@NotNull AddonContainer addon,
                                 @NotNull UUID id,
                                 @NotNull ServerSourceLine line,
                                 @Nullable String codec,
                                 boolean stereo) {
        this.addon = addon;
        this.id = id;
        this.line = line;
        this.codec = codec;
        this.stereo = stereo;
    }

    @Override
    public int getState() {
        return state.get();
    }

    @Override
    public synchronized void setLine(@NotNull ServerSourceLine line) {
        if (!this.line.equals(line)) {
            this.line = line;
            setDirty();
            increaseSourceState();
        }
    }

    @Override
    public synchronized void setStereo(boolean stereo) {
        if (this.stereo != stereo) {
            this.stereo = stereo;
            setDirty();
            increaseSourceState();
        }
    }

    @Override
    public synchronized void setIconVisible(boolean visible) {
        if (this.iconVisible != visible) {
            this.iconVisible = visible;
            setDirty();
            increaseSourceState();
        }
    }

    @Override
    public void addFilter(Predicate<VoicePlayer> filter) {
        if (filters.contains(filter)) throw new IllegalArgumentException("Filter already exist");
        filters.add(filter);
    }

    @Override
    public void removeFilter(Predicate<VoicePlayer> filter) {
        filters.remove(filter);
    }

    @Override
    public @NotNull Collection<Predicate<VoicePlayer>> getFilters() {
        return filters;
    }

    @Override
    public void clearFilters() {
        filters.clear();
    }

    @Override
    public void setDirty() {
        dirty.set(true);
    }

    protected boolean testPlayer(@NotNull VoicePlayer player) {
        for (Predicate<VoicePlayer> filter : filters) {
            if (!filter.test(player)) return false;
        }

        return true;
    }

    protected void increaseSourceState() {
        state.updateAndGet((operand) -> {
            int value = operand + 1;
            return value > Byte.MAX_VALUE ? Byte.MIN_VALUE : value;
        });
    }
}
