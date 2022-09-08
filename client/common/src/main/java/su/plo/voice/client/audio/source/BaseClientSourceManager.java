package su.plo.voice.client.audio.source;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.source.ClientAudioSource;
import su.plo.voice.api.client.audio.source.ClientSourceManager;
import su.plo.voice.api.client.event.audio.source.AudioSourceClosedEvent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.source.*;
import su.plo.voice.proto.packets.tcp.serverbound.SourceInfoRequestPacket;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class BaseClientSourceManager implements ClientSourceManager {

    protected final Map<UUID, ClientAudioSource<?>> sourceById = Maps.newConcurrentMap();
    protected final Map<UUID, Long> sourceRequests = Maps.newConcurrentMap();

    protected final PlasmoVoiceClient voiceClient;
    protected final ClientConfig config;

    @Override
    public Optional<ClientAudioSource<?>> getSourceById(@NotNull UUID sourceId, boolean request) {
        if (!voiceClient.getServerConnection().isPresent()) throw new IllegalStateException("Not connected");

        ClientAudioSource<?> source = sourceById.get(sourceId);
        if (source != null) return Optional.of(source);

        if (!request) return Optional.empty();

        // request source
        long lastRequest = sourceRequests.getOrDefault(sourceId, 0L);
        if (System.currentTimeMillis() - lastRequest > 1_000L)
            sendSourceInfoRequest(sourceId);

        return Optional.empty();
    }

    @Override
    public Optional<ClientAudioSource<?>> getSourceById(@NotNull UUID sourceId) {
        return getSourceById(sourceId, true);
    }

    @Override
    public Collection<ClientAudioSource<?>> getSources() {
        return sourceById.values();
    }

    @Override
    public void update(@NotNull SourceInfo sourceInfo) {
        if (sourceById.containsKey(sourceInfo.getId())) {
            ClientAudioSource<?> source = sourceById.get(sourceInfo.getId());

            if (source.getInfo() instanceof StaticSourceInfo && sourceInfo instanceof StaticSourceInfo) {
                ((ClientAudioSource<StaticSourceInfo>) source).updateInfo((StaticSourceInfo) sourceInfo);
            } else {
                throw new IllegalArgumentException("Invalid source type");
            }
            return;
        }

        if (sourceInfo instanceof PlayerSourceInfo) {
            ClientAudioSource<PlayerSourceInfo> source = createPlayerSource();
            try {
                source.initialize((PlayerSourceInfo) sourceInfo);
            } catch (DeviceException e) {
                throw new IllegalStateException("Failed to initialize audio source", e);
            }

            sourceById.put(sourceInfo.getId(), source);
        } else if (sourceInfo instanceof EntitySourceInfo) {
            ClientAudioSource<EntitySourceInfo> source = createEntitySource();
            try {
                source.initialize((EntitySourceInfo) sourceInfo);
            } catch (DeviceException e) {
                throw new IllegalStateException("Failed to initialize audio source", e);
            }

            sourceById.put(sourceInfo.getId(), source);
        } else if (sourceInfo instanceof StaticSourceInfo) {
            ClientAudioSource<StaticSourceInfo> source = createStaticSource();
            try {
                source.initialize((StaticSourceInfo) sourceInfo);
            } catch (DeviceException e) {
                throw new IllegalStateException("Failed to initialize audio source", e);
            }

            sourceById.put(sourceInfo.getId(), source);
        } else if (sourceInfo instanceof DirectSourceInfo) {
            ClientAudioSource<DirectSourceInfo> source = createDirectSource();
            try {
                source.initialize((DirectSourceInfo) sourceInfo);
            } catch (DeviceException e) {
                throw new IllegalStateException("Failed to initialize audio source", e);
            }

            sourceById.put(sourceInfo.getId(), source);
        } else {
            throw new IllegalArgumentException("Invalid source type");
        }

        sourceRequests.remove(sourceInfo.getId());
    }

    @Override
    public void sendSourceInfoRequest(@NotNull UUID sourceId) {
        if (!voiceClient.getServerConnection().isPresent()) throw new IllegalStateException("Not connected");

        sourceRequests.put(sourceId, System.currentTimeMillis());
        voiceClient.getServerConnection().get().sendPacket(
                new SourceInfoRequestPacket(sourceId)
        );
    }

    @EventSubscribe
    public void onAudioSourceClosed(AudioSourceClosedEvent event) {
        ClientAudioSource<?> source = event.getSource();
        voiceClient.getEventBus().unregister(voiceClient, source);

        sourceById.remove(source.getInfo().getId());
    }

    protected abstract ClientAudioSource<PlayerSourceInfo> createPlayerSource();

    protected abstract ClientAudioSource<EntitySourceInfo> createEntitySource();

    protected abstract ClientAudioSource<DirectSourceInfo> createDirectSource();

    protected abstract ClientAudioSource<StaticSourceInfo> createStaticSource();
}
