package su.plo.voice.api.client.audio.device;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.client.audio.device.source.DeviceSource;
import su.plo.voice.api.util.Params;

import javax.sound.sampled.AudioFormat;

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

    /**
     * Reloads the device
     *
     * @param format audio format
     *               if null current audio format will be used
     * @param params device params
     *               will use current audio params and overwrite it with yours
     *
     * @throws DeviceException if the device cannot be reloaded
     */
    void reload(@Nullable AudioFormat format, @NotNull Params params) throws DeviceException;

    @Override
    default DeviceType getType() {
        return DeviceType.OUTPUT;
    }
}
