package su.plo.voice.api.client.audio.device;

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
     * Read samples with the capacity of bufferSize
     *
     * @param bufferSize the frame size
     * @return samples or null if bufferSize > available samples
     */
    short[] read(int bufferSize);

    /**
     * Read samples with the capacity of bufferSize calculated from AudioFormat
     *
     * @return samples or null if bufferSize > available samples
     */
    short[] read();

    @Override
    default DeviceType getType() {
        return DeviceType.INPUT;
    }
}
