package su.plo.voice.proxy.audio.source;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.proxy.PlasmoVoiceProxy;
import su.plo.voice.api.proxy.audio.source.ProxySourceManager;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.audio.source.ServerAudioSource;
import su.plo.voice.api.server.audio.source.ServerDirectSource;
import su.plo.voice.proto.data.audio.codec.CodecInfo;
import su.plo.voice.server.audio.source.VoiceServerDirectSource;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class VoiceProxySourceManager implements ProxySourceManager {

    private final PlasmoVoiceProxy voiceProxy;

    private final Map<UUID, ServerAudioSource<?>> sourceById = Maps.newConcurrentMap();

    @Override
    public Optional<ServerAudioSource<?>> getSourceById(@NotNull UUID sourceId) {
        return Optional.ofNullable(sourceById.get(sourceId));
    }

    public Collection<ServerAudioSource<?>> getSources() {
        return sourceById.values();
    }

    @Override
    public void clear() {
        // todo: create & send source removed packet

        sourceById.clear();
    }

    @Override
    public @NotNull ServerDirectSource createDirectSource(@NotNull Object addonObject,
                                                          @NotNull ServerSourceLine line,
                                                          @Nullable CodecInfo decoderInfo,
                                                          boolean stereo) {
        Optional<AddonContainer> addon = voiceProxy.getAddonManager().getAddon(addonObject);
        if (!addon.isPresent()) throw new IllegalArgumentException("addonObject is not an addon");

        ServerDirectSource source = new VoiceServerDirectSource(
                voiceProxy,
                voiceProxy.getUdpConnectionManager(),
                addon.get(),
                line,
                decoderInfo,
                stereo
        );
        sourceById.put(source.getId(), source);

        return source;
    }

    @Override
    public void remove(@NotNull UUID sourceId) {
        sourceById.remove(sourceId);
    }

    @Override
    public void remove(@NotNull ServerAudioSource<?> source) {
        remove(source.getId());
    }

//    @Override
//    public @NotNull UUID registerCustomSource(@NotNull ServerAudioSource source) {
//        UUID sourceId = UUID.randomUUID();
//        sourceById.put(sourceId, source);
//
//        return sourceId;
//    }

//    @EventSubscribe
//    public void onVoiceShutdown(VoiceServerShutdownEvent event) {
//        sourceById.clear();
//    }
}
