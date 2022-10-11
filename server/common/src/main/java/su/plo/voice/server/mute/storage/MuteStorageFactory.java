package su.plo.voice.server.mute.storage;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.mute.storage.MuteStorage;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.mute.storage.file.JsonMuteStorage;

import java.io.File;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
public final class MuteStorageFactory {

    private final BaseVoiceServer voiceServer;
    private final ExecutorService executor;

    public MuteStorage createStorage(@NotNull String storageType) {
        switch (storageType) {
            case "json":
                return new JsonMuteStorage(executor, new File(voiceServer.getConfigFolder(), "voice_mutes.json"));
            default:
                throw new IllegalArgumentException("Unknown storage type: " + storageType);
        }
    }
}
