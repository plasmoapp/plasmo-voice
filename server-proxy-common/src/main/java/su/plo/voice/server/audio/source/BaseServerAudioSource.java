package su.plo.voice.server.audio.source;

import lombok.Getter;
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
    protected boolean stereo;

    protected final AtomicBoolean dirty = new AtomicBoolean(true);
    protected final AtomicInteger state = new AtomicInteger(1);

    protected final List<Predicate<? super VoicePlayer>> filters = new CopyOnWriteArrayList<>();

    protected BaseServerAudioSource(
            @NotNull AddonContainer addon,
            @NotNull UUID id,
            @NotNull BaseServerSourceLine line,
            @Nullable CodecInfo decoderInfo,
            boolean stereo
    ) {
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
    public <P extends VoicePlayer> void addFilter(@NotNull Predicate<? super P> filter) {
        if (filters.contains(filter)) throw new IllegalArgumentException("Filter already exist");
        filters.add((Predicate<? super VoicePlayer>) filter);
    }

    @Override
    public <P extends VoicePlayer> void removeFilter(@NotNull Predicate<? super P> filter) {
        filters.remove(filter);
    }

    @Override
    public @NotNull Collection<Predicate<? super VoicePlayer>> getFilters() {
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
    public <P extends VoicePlayer> boolean matchFilters(@NotNull P player) {
        // Not sure about this because this check cannot be disabled,
        // but I think it shouldn't be disabled anyway.
        if (player.isVoiceDisabled()) return false;

        for (Predicate<? super VoicePlayer> filter : filters) {
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
