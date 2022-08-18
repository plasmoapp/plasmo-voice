package su.plo.voice.client.audio.source;

import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.source.ClientAudioSource;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.source.EntitySourceInfo;
import su.plo.voice.proto.data.source.PlayerSourceInfo;

public final class ModClientSourceManager extends BaseClientSourceManager {

    public ModClientSourceManager(PlasmoVoiceClient voiceClient, ClientConfig config) {
        super(voiceClient, config);
    }

    @Override
    protected ClientAudioSource<PlayerSourceInfo> createPlayerSource() {
        ClientAudioSource<PlayerSourceInfo> source = new ModClientPlayerSource(voiceClient, config);
        voiceClient.getEventBus().register(voiceClient, source);

        return source;
    }

    @Override
    protected ClientAudioSource<EntitySourceInfo> createEntitySource() {
        ClientAudioSource<EntitySourceInfo> source = new ModClientEntitySource(voiceClient, config);
        voiceClient.getEventBus().register(voiceClient, source);

        return source;
    }
}
