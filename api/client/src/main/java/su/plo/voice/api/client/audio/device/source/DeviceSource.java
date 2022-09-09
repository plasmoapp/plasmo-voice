package su.plo.voice.api.client.audio.device.source;

import su.plo.voice.api.client.audio.device.AudioDevice;

// todo: doc
public interface DeviceSource {

    AudioDevice getDevice();

    void write(byte[] samples);

    void close();

    boolean isClosed();
}
