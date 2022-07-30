package su.plo.voice.api.audio.device;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.source.DeviceSource;
import su.plo.voice.api.util.Params;

// todo: doc
public interface OutputDevice<T extends DeviceSource> extends AudioDevice {

    /**
     * Create a new source
     *
     * @param params parameters
     *
     * @return the device source
     */
    T createSource(@NotNull Params params) throws DeviceException;

    @Override
    default DeviceType getType() {
        return DeviceType.OUTPUT;
    }
}
