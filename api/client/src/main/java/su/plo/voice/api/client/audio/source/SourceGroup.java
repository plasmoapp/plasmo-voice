package su.plo.voice.api.client.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.util.Params;

import java.util.Collection;

// todo: doc
public interface SourceGroup {

    void create(@NotNull Params params) throws DeviceException;

    void clear();

    Collection<DeviceSource> getSources();
}
