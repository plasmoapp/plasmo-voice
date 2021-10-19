package su.plo.voice.client.sound.capture;

public interface CaptureDevice {
    /**
     * Open capture device
     * @throws IllegalStateException if cannot open capture device
     */
    void open() throws IllegalStateException;

    /**
     * Start capture device
     */
    void start();

    /**
     * Stop capture device
     */
    void stop();

    /**
     * Close capture device
     */
    void close();

    /**
     * Capture samples with capacity of frameSize
     * @param frameSize
     * @return samples or null if frameSize > available samples
     */
    byte[] read(int frameSize);

    /**
     * @return true if capture device is open
     */
    boolean isOpen();
}
