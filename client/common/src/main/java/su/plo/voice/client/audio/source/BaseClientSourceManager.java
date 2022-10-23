package su.plo.voice.client.audio.source;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.source.ClientAudioSource;
import su.plo.voice.api.client.audio.source.ClientSourceManager;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.client.event.audio.source.AudioSourceClosedEvent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.audio.source.*;
import su.plo.voice.proto.packets.tcp.serverbound.SourceInfoRequestPacket;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
public abstract class BaseClientSourceManager implements ClientSourceManager {

    protected final ListMultimap<UUID, ClientAudioSource<?>> sourcesByLineId = Multimaps.newListMultimap(
            Maps.newConcurrentMap(),
            CopyOnWriteArrayList::new
    );
    protected final Map<UUID, ClientAudioSource<?>> sourceById = Maps.newConcurrentMap();
    protected final Map<UUID, Long> sourceRequestById = Maps.newConcurrentMap();

    protected final PlasmoVoiceClient voiceClient;
    protected final ClientConfig config;

    @Override
    public Optional<ClientAudioSource<?>> getSourceById(@NotNull UUID sourceId, boolean request) {
        if (!voiceClient.getServerConnection().isPresent()) throw new IllegalStateException("Not connected");

        ClientAudioSource<?> source = sourceById.get(sourceId);
        if (source != null) return Optional.of(source);

        if (!request) return Optional.empty();

        // request source
        long lastRequest = sourceRequestById.getOrDefault(sourceId, 0L);
        if (System.currentTimeMillis() - lastRequest > 1_000L)
            sendSourceInfoRequest(sourceId);

        return Optional.empty();
    }

    @Override
    public Collection<ClientAudioSource<?>> getSourcesByLineId(@NotNull UUID lineId) {
        return sourcesByLineId.get(lineId);
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
    public synchronized void clear() {
        sourceById.values().forEach(ClientAudioSource::close);
        sourcesByLineId.clear();
        sourceRequestById.clear();
    }

    @Override // todo: refactor somehow pepega
    public void update(@NotNull SourceInfo sourceInfo) {
        try {
            if (sourceById.containsKey(sourceInfo.getId())) {
                ClientAudioSource<?> source = sourceById.get(sourceInfo.getId());
                if (source.isClosed() || source.getInfo() == null) {
                    sourceRequestById.remove(sourceInfo.getId());
                    return;
                }

                if (source.getInfo().getLineId() != sourceInfo.getLineId()) {
                    sourcesByLineId.remove(source.getInfo().getLineId(), source);
                    sourcesByLineId.put(sourceInfo.getLineId(), source);
                }

                if (source.getInfo() instanceof StaticSourceInfo && sourceInfo instanceof StaticSourceInfo) {
                    ((ClientAudioSource<StaticSourceInfo>) source).initialize((StaticSourceInfo) sourceInfo);
                } else if (source.getInfo() instanceof PlayerSourceInfo && sourceInfo instanceof PlayerSourceInfo) {
                    ((ClientAudioSource<PlayerSourceInfo>) source).initialize((PlayerSourceInfo) sourceInfo);
                } else if (source.getInfo() instanceof EntitySourceInfo && sourceInfo instanceof EntitySourceInfo) {
                    ((ClientAudioSource<EntitySourceInfo>) source).initialize((EntitySourceInfo) sourceInfo);
                } else if (source.getInfo() instanceof DirectSourceInfo && sourceInfo instanceof DirectSourceInfo) {
                    ((ClientAudioSource<DirectSourceInfo>) source).initialize((DirectSourceInfo) sourceInfo);
                } else {
                    throw new IllegalArgumentException("Invalid source type");
                }
                return;
            }

            if (sourceInfo instanceof PlayerSourceInfo) {
                ClientAudioSource<PlayerSourceInfo> source = createPlayerSource();
                source.initialize((PlayerSourceInfo) sourceInfo);

                sourceById.put(sourceInfo.getId(), source);
                sourcesByLineId.put(sourceInfo.getLineId(), source);
            } else if (sourceInfo instanceof EntitySourceInfo) {
                ClientAudioSource<EntitySourceInfo> source = createEntitySource();
                source.initialize((EntitySourceInfo) sourceInfo);

                sourceById.put(sourceInfo.getId(), source);
                sourcesByLineId.put(sourceInfo.getLineId(), source);
            } else if (sourceInfo instanceof StaticSourceInfo) {
                ClientAudioSource<StaticSourceInfo> source = createStaticSource();
                source.initialize((StaticSourceInfo) sourceInfo);

                sourceById.put(sourceInfo.getId(), source);
                sourcesByLineId.put(sourceInfo.getLineId(), source);
            } else if (sourceInfo instanceof DirectSourceInfo) {
                ClientAudioSource<DirectSourceInfo> source = createDirectSource();
                source.initialize((DirectSourceInfo) sourceInfo);

                sourceById.put(sourceInfo.getId(), source);
                sourcesByLineId.put(sourceInfo.getLineId(), source);
            } else {
                throw new IllegalArgumentException("Invalid source type");
            }

            sourceRequestById.remove(sourceInfo.getId());
        } catch (DeviceException e) {
            throw new IllegalStateException("Failed to initialize audio source", e);
        }
    }

    @Override
    public synchronized void sendSourceInfoRequest(@NotNull UUID sourceId) {
        ServerConnection connection = voiceClient.getServerConnection()
                .orElseThrow(() -> new IllegalStateException("Not connected"));

        if (sourceById.containsKey(sourceId)) return;

        sourceRequestById.put(sourceId, System.currentTimeMillis());
        connection.sendPacket(new SourceInfoRequestPacket(sourceId));
    }

    @EventSubscribe
    public synchronized void onAudioSourceClosed(AudioSourceClosedEvent event) {
        ClientAudioSource<?> source = event.getSource();
        voiceClient.getEventBus().unregister(voiceClient, source);

        sourceById.remove(source.getInfo().getId());
    }

    protected abstract ClientAudioSource<PlayerSourceInfo> createPlayerSource();

    protected abstract ClientAudioSource<EntitySourceInfo> createEntitySource();

    protected abstract ClientAudioSource<DirectSourceInfo> createDirectSource();

    protected abstract ClientAudioSource<StaticSourceInfo> createStaticSource();
}
