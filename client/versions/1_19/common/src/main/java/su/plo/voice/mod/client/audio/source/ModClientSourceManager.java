package su.plo.voice.mod.client.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.voice.api.client.audio.source.ClientAudioSource;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.client.audio.source.BaseClientSourceManager;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.audio.source.DirectSourceInfo;
import su.plo.voice.proto.data.audio.source.EntitySourceInfo;
import su.plo.voice.proto.data.audio.source.PlayerSourceInfo;
import su.plo.voice.proto.data.audio.source.StaticSourceInfo;

public final class ModClientSourceManager extends BaseClientSourceManager {

    public ModClientSourceManager(@NotNull MinecraftClientLib minecraft,
                                  @NotNull BaseVoiceClient voiceClient,
                                  @NotNull ClientConfig config) {
        super(minecraft, voiceClient, config);
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

    @Override
    protected ClientAudioSource<DirectSourceInfo> createDirectSource() {
        ClientAudioSource<DirectSourceInfo> source = new ModClientDirectSource(voiceClient, config);
        voiceClient.getEventBus().register(voiceClient, source);

        return source;
    }

    @Override
    protected ClientAudioSource<StaticSourceInfo> createStaticSource() {
        ClientAudioSource<StaticSourceInfo> source = new ModClientStaticSource(voiceClient, config);
        voiceClient.getEventBus().register(voiceClient, source);

        return source;
    }
}
