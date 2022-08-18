package su.plo.voice.client.audio.source;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.source.ClientAudioSource;
import su.plo.voice.api.client.audio.source.ClientSourceManager;
import su.plo.voice.api.client.event.audio.source.AudioSourceClosedEvent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.source.EntitySourceInfo;
import su.plo.voice.proto.data.source.PlayerSourceInfo;
import su.plo.voice.proto.data.source.SourceInfo;
import su.plo.voice.proto.packets.tcp.serverbound.SourceInfoRequestPacket;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@RequiredArgsConstructor
public abstract class BaseClientSourceManager implements ClientSourceManager {

    protected final Map<UUID, ClientAudioSource<?>> sourceById = Maps.newConcurrentMap();
    protected final Set<UUID> sourceRequests = new CopyOnWriteArraySet<>();

    protected final PlasmoVoiceClient voiceClient;
    protected final ClientConfig config;

    @Override
    public Optional<ClientAudioSource<?>> getSourceById(@NotNull UUID sourceId) {
        if (!voiceClient.getServerConnection().isPresent()) throw new IllegalStateException("Not connected");

        ClientAudioSource<?> source = sourceById.get(sourceId);
        if (source != null) return Optional.of(source);

        // request source
        if (!sourceRequests.contains(sourceId)) {
            sourceRequests.add(sourceId);

            voiceClient.getServerConnection().get().sendPacket(
                    new SourceInfoRequestPacket(sourceId)
            );
        }

        return Optional.empty();
    }

    @Override
    public Collection<ClientAudioSource<?>> getSources(SourceInfo.@Nullable Type type) {
        return sourceById.values();
    }

    @Override
    public void create(@NotNull SourceInfo sourceInfo) {
        if (sourceById.containsKey(sourceInfo.getId()))
            throw new IllegalStateException("Source with the same id is already exist");

        if (sourceInfo instanceof PlayerSourceInfo) {
            ClientAudioSource<PlayerSourceInfo> source = createPlayerSource();
            try {
                source.initialize((PlayerSourceInfo) sourceInfo);
            } catch (DeviceException e) {
                throw new IllegalStateException("Failed to initialize audio source", e);
            }

            sourceById.put(sourceInfo.getId(), source);
        } else {
            throw new IllegalArgumentException("Invalid source type");
        }

        sourceRequests.remove(sourceInfo.getId());
    }

    @EventSubscribe
    public void onAudioSourceClosed(AudioSourceClosedEvent event) {
        ClientAudioSource<?> source = event.getSource();
        voiceClient.getEventBus().unregister(voiceClient, source);

        sourceById.remove(source.getInfo().getId());
    }

    protected abstract ClientAudioSource<PlayerSourceInfo> createPlayerSource();

    protected abstract ClientAudioSource<EntitySourceInfo> createEntitySource();
}
