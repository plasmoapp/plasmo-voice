package su.plo.voice.client.audio.line;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.line.ClientSourceLine;
import su.plo.voice.api.client.audio.line.ClientSourceLineManager;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.config.entry.DoubleConfigEntry;
import su.plo.voice.proto.data.audio.line.SourceLine;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class VoiceClientSourceLineManager implements ClientSourceLineManager {

    private final ClientConfig config;

    private final List<ClientSourceLine> lines = new CopyOnWriteArrayList<>();
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
        return lines;
    }

    @Override
    public boolean unregister(@NotNull UUID id) {
        ClientSourceLine line = lineById.remove(id);
        if (line != null) return lines.remove(line);
        return false;
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
    public void clear() {
        lineById.clear();
    }

    @Override
    public @NotNull ClientSourceLine register(@NotNull ClientSourceLine line) {
        unregister(line.getId());

        int index;
        for (index = 0; index < lines.size(); index++) {
            ClientSourceLine act = lines.get(index);
            if (line.getWeight() >= act.getWeight()) break;
        }

        lines.add(index, line);
        lineById.put(line.getId(), line);

        return line;
    }

    @Override
    public @NotNull ClientSourceLine register(@NotNull SourceLine line) {
        unregister(line.getId());

        DoubleConfigEntry volumeEntry = config.getVoice().getVolumes().getVolume(line.getName());
        ClientSourceLine clientLine = new VoiceClientSourceLine(volumeEntry, line);

        return register(clientLine);
    }

    @Override
    public @NotNull Collection<ClientSourceLine> register(@NotNull Collection<SourceLine> lines) {
        return lines.stream()
                .map(this::register)
                .collect(Collectors.toList());
    }
}
