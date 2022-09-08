package su.plo.voice.api.client.audio.device;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.device.source.DeviceSource;
import su.plo.voice.api.util.Params;

// todo: doc
public interface OutputDevice<T extends DeviceSource> extends AudioDevice {

    /**
     * Create a new source
     *
     * @param stereo if true source will be stereo
     * @param params parameters
     *
     * @return the device source
     */
    T createSource(boolean stereo, @NotNull Params params) throws DeviceException;

    @Override
    default DeviceType getType() {
        return DeviceType.OUTPUT;
    }
}
