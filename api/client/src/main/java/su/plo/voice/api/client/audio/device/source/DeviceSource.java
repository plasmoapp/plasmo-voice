package su.plo.voice.api.client.audio.device.source;

// todo: doc
public interface DeviceSource {

    void write(byte[] samples);

    void close();

    boolean isClosed();
}
