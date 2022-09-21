package su.plo.voice.client.audio.line;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.line.ClientSourceLine;
import su.plo.voice.api.client.audio.line.ClientSourceLineManager;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.config.entry.DoubleConfigEntry;
import su.plo.voice.proto.data.audio.line.SourceLine;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;

import java.util.*;

@RequiredArgsConstructor
public final class VoiceClientSourceLineManager implements ClientSourceLineManager {

    private final ClientConfig config;

    private final Map<UUID, ClientSourceLine> lineById = Maps.newConcurrentMap();

    @Override
    public Optional<ClientSourceLine> getLineById(@NotNull UUID id) {
        return Optional.ofNullable(lineById.get(id));
    }

    @Override
    public Optional<ClientSourceLine> getLineByName(@NotNull String name) {
        return Optional.ofNullable(lineById.get(VoiceSourceLine.generateId(name)));
    }

    @Override
    public Collection<ClientSourceLine> getLines() {
        return lineById.values();
    }

    @Override
    public boolean unregister(@NotNull UUID id) {
        return lineById.remove(id) != null;
    }

    @Override
    public boolean unregister(@NotNull String name) {
        return unregister(VoiceSourceLine.generateId(name));
    }

    @Override
    public boolean unregister(@NotNull ClientSourceLine line) {
        return unregister(line.getId());
    }

    @Override
    public @NotNull ClientSourceLine register(@NotNull ClientSourceLine line) {
        return lineById.put(line.getId(), line);
    }

    @Override
    public @NotNull Collection<ClientSourceLine> register(@NotNull Collection<SourceLine> lines) {
        List<ClientSourceLine> registered = Lists.newArrayList();

        for (SourceLine line : lines) {
            DoubleConfigEntry volumeEntry = config.getVoice().getVolumes().getVolume(line.getName());
            registered.add(register(new VoiceClientSourceLine(volumeEntry, line)));
        }

        return registered;
    }
}
