package su.plo.voice.server.audio.source;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.audio.line.BaseServerSourceLine;
import su.plo.voice.api.server.audio.source.ServerAudioSource;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.audio.codec.CodecInfo;
import su.plo.voice.proto.data.audio.source.SourceInfo;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
    protected final @Nullable CodecInfo decoderInfo;

    @Getter
    protected final BaseServerSourceLine line;
    @Getter
    protected @Nullable String name;
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
                                    @NotNull BaseServerSourceLine line,
                                    @Nullable CodecInfo decoderInfo,
                                    boolean stereo) {
        this.addon = addon;
        this.id = id;
        this.line = line;
        this.decoderInfo = decoderInfo;
        this.stereo = stereo;
    }

    @Override
    public int getState() {
        return state.get();
    }

    @Override
    public synchronized void setStereo(boolean stereo) {
        if (this.stereo != stereo) {
            this.stereo = stereo;
            setDirty();
            // increase source state by 10, so client can detect that
            // it was a major change and drop all packets with source state diff more than 10
            increaseSourceState(10);
        }
    }

    @Override
    public synchronized void setIconVisible(boolean visible) {
        if (this.iconVisible != visible) {
            this.iconVisible = visible;
            setDirty();
            increaseSourceState(1);
        }
    }

    @Override
    public void setName(@Nullable String name) {
        if (!Objects.equals(this.name, name)) {
            this.name = name;
            setDirty();
            increaseSourceState(1);
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

    @Override
    public boolean matchFilters(@NotNull VoicePlayer player) {
        for (Predicate<VoicePlayer> filter : filters) {
            if (!filter.test(player)) return false;
        }

        return true;
    }

    protected void increaseSourceState(int addition) {
        state.updateAndGet((operand) -> {
            int value = operand + addition;

            if (value > Byte.MAX_VALUE) {
                int remainder = value % Byte.MIN_VALUE;
                return Byte.MIN_VALUE + remainder;
            } else {
                return value;
            }
        });
    }
}
