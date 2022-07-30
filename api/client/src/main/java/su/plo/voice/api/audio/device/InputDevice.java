package su.plo.voice.api.audio.device;

// todo: doc
public interface InputDevice extends AudioDevice {

    /**
     * Start the device
     */
    void start();

    /**
     * Stop the device
     */
    void stop();

    /**
     * @return available samples
     */
    int available();

    /**
     * Read samples with the capacity of frameSize
     * @param frameSize the frame size
     * @return samples or null if frameSize > available samples
     */
    byte[] read(int frameSize);

    @Override
    default DeviceType getType() {
        return DeviceType.INPUT;
    }
}
