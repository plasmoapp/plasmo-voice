package su.plo.voice.client.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.source.DeviceSource;
import su.plo.voice.api.client.audio.source.SourceGroup;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class VoiceSourceGroup implements SourceGroup {

    private final List<DeviceSource> sources = new CopyOnWriteArrayList<>();

    @Override
    public void add(@NotNull DeviceSource source) {
        sources.add(source);
    }

    @Override
    public void remove(@NotNull DeviceSource source) {
        sources.remove(source);
    }

    @Override
    public void clear() {
        sources.clear();
    }

    @Override
    public Collection<DeviceSource> getSources() {
        return sources;
    }
}
