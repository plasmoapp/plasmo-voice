package su.plo.voice.api.audio.source;

import java.util.concurrent.CompletableFuture;

// todo: doc
public interface DeviceSource {

    void write(byte[] samples);

    CompletableFuture<Void> close();
}
