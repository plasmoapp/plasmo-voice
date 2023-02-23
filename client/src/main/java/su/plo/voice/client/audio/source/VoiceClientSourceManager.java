package su.plo.voice.client.audio.source;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.source.ClientAudioSource;
import su.plo.voice.api.client.audio.source.ClientSelfSourceInfo;
import su.plo.voice.api.client.audio.source.ClientSourceManager;
import su.plo.voice.api.client.audio.source.LoopbackSource;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.client.event.audio.source.AudioSourceClosedEvent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.audio.source.*;
import su.plo.voice.proto.packets.tcp.serverbound.SourceInfoRequestPacket;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public final class VoiceClientSourceManager implements ClientSourceManager {

    private static final long TIMEOUT_MS = 25_000L;

    private final ListMultimap<UUID, ClientAudioSource<?>> sourcesByLineId = Multimaps.newListMultimap(
            Maps.newConcurrentMap(),
            CopyOnWriteArrayList::new
    );
    private final Map<UUID, ClientAudioSource<?>> sourceById = Maps.newConcurrentMap();
    private final Map<UUID, Long> sourceRequestById = Maps.newConcurrentMap();

    private final Map<UUID, VoiceClientSelfSourceInfo> selfSourceInfoById = Maps.newConcurrentMap();

    private final BaseVoiceClient voiceClient;
    private final ClientConfig config;

    public VoiceClientSourceManager(@NotNull BaseVoiceClient voiceClient,
                                    @NotNull ClientConfig config) {
        this.voiceClient = voiceClient;
        this.config = config;

        voiceClient.getBackgroundExecutor().scheduleAtFixedRate(
                this::tickSelfSourceInfo,
                0L, 5L, TimeUnit.SECONDS
        );
    }

    @Override
    public @NotNull LoopbackSource createLoopbackSource(boolean relative) {
        return new ClientLoopbackSource(voiceClient, config, relative);
    }

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
                if (source.isClosed()) {
                    sourceRequestById.remove(sourceInfo.getId());
                    return;
                }

                if (source.getInfo().getLineId() != sourceInfo.getLineId()) {
                    sourcesByLineId.remove(source.getInfo().getLineId(), source);
                    sourcesByLineId.put(sourceInfo.getLineId(), source);
                }

                if (source.getInfo() instanceof StaticSourceInfo && sourceInfo instanceof StaticSourceInfo) {
                    ((ClientAudioSource<StaticSourceInfo>) source).update((StaticSourceInfo) sourceInfo);
                } else if (source.getInfo() instanceof PlayerSourceInfo && sourceInfo instanceof PlayerSourceInfo) {
                    ((ClientAudioSource<PlayerSourceInfo>) source).update((PlayerSourceInfo) sourceInfo);
                } else if (source.getInfo() instanceof EntitySourceInfo && sourceInfo instanceof EntitySourceInfo) {
                    ((ClientAudioSource<EntitySourceInfo>) source).update((EntitySourceInfo) sourceInfo);
                } else if (source.getInfo() instanceof DirectSourceInfo && sourceInfo instanceof DirectSourceInfo) {
                    ((ClientAudioSource<DirectSourceInfo>) source).update((DirectSourceInfo) sourceInfo);
                } else {
                    throw new IllegalArgumentException("Invalid source type");
                }
                return;
            }

            if (sourceInfo instanceof PlayerSourceInfo) {
                ClientAudioSource<PlayerSourceInfo> source = createPlayerSource((PlayerSourceInfo) sourceInfo);

                sourceById.put(sourceInfo.getId(), source);
                sourcesByLineId.put(sourceInfo.getLineId(), source);
            } else if (sourceInfo instanceof EntitySourceInfo) {
                ClientAudioSource<EntitySourceInfo> source = createEntitySource((EntitySourceInfo) sourceInfo);

                sourceById.put(sourceInfo.getId(), source);
                sourcesByLineId.put(sourceInfo.getLineId(), source);
            } else if (sourceInfo instanceof StaticSourceInfo) {
                ClientAudioSource<StaticSourceInfo> source = createStaticSource((StaticSourceInfo) sourceInfo);

                sourceById.put(sourceInfo.getId(), source);
                sourcesByLineId.put(sourceInfo.getLineId(), source);
            } else if (sourceInfo instanceof DirectSourceInfo) {
                ClientAudioSource<DirectSourceInfo> source = createDirectSource((DirectSourceInfo) sourceInfo);

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
    public synchronized void sendSourceInfoRequest(@NotNull UUID sourceId, boolean requestIfExist) {
        if (!requestIfExist && sourceById.containsKey(sourceId)) return;

        ServerConnection connection = voiceClient.getServerConnection()
                .orElseThrow(() -> new IllegalStateException("Not connected"));

        sourceRequestById.put(sourceId, System.currentTimeMillis());
        connection.sendPacket(new SourceInfoRequestPacket(sourceId));
    }

    @Override
    public void updateSelfSourceInfo(@NotNull SelfSourceInfo selfSourceInfo) {
        selfSourceInfoById.computeIfAbsent(
                selfSourceInfo.getSourceInfo().getId(),
                (sourceId) -> new VoiceClientSelfSourceInfo()
        ).setSelfSourceInfo(selfSourceInfo);

        if (getSourceById(selfSourceInfo.getSourceInfo().getId(), false).isPresent()) {
            update(selfSourceInfo.getSourceInfo());
        }
    }

    @Override
    public Optional<ClientSelfSourceInfo> getSelfSourceInfo(@NotNull UUID sourceId) {
        return Optional.ofNullable(selfSourceInfoById.get(sourceId));
    }

    @Override
    public Collection<? extends ClientSelfSourceInfo> getSelfSourceInfos() {
        return selfSourceInfoById.values();
    }

    @EventSubscribe
    public synchronized void onAudioSourceClosed(AudioSourceClosedEvent event) {
        ClientAudioSource<?> source = event.getSource();
        voiceClient.getEventBus().unregister(voiceClient, source);

        sourceById.remove(source.getInfo().getId());
    }

    private void tickSelfSourceInfo() {
        selfSourceInfoById.values()
                .stream()
                .filter((selfSourceInfo) -> System.currentTimeMillis() - selfSourceInfo.getLastUpdate() > TIMEOUT_MS)
                .map((selfSourceInfo) -> selfSourceInfo.getSelfSourceInfo().getSourceInfo().getId())
                .forEach(selfSourceInfoById::remove);
    }

    private ClientAudioSource<PlayerSourceInfo> createPlayerSource(@NotNull PlayerSourceInfo sourceInfo) {
        ClientAudioSource<PlayerSourceInfo> source = new ClientPlayerSource(voiceClient, config, sourceInfo);
        voiceClient.getEventBus().register(voiceClient, source);
        return source;
    }

    private ClientAudioSource<EntitySourceInfo> createEntitySource(@NotNull EntitySourceInfo sourceInfo) {
        ClientAudioSource<EntitySourceInfo> source = new ClientEntitySource(voiceClient, config, sourceInfo);
        voiceClient.getEventBus().register(voiceClient, source);
        return source;
    }

    private ClientAudioSource<DirectSourceInfo> createDirectSource(@NotNull DirectSourceInfo sourceInfo) {
        ClientAudioSource<DirectSourceInfo> source = new ClientDirectSource(voiceClient, config, sourceInfo);
        voiceClient.getEventBus().register(voiceClient, source);
        return source;
    }

    private ClientAudioSource<StaticSourceInfo> createStaticSource(@NotNull StaticSourceInfo sourceInfo) {
        ClientAudioSource<StaticSourceInfo> source = new ClientStaticSource(voiceClient, config, sourceInfo);
        voiceClient.getEventBus().register(voiceClient, source);
        return source;
    }
}
