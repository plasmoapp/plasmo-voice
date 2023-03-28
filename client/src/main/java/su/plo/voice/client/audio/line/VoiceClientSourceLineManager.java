package su.plo.voice.client.audio.line;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.lib.api.MathLib;
import su.plo.voice.api.client.audio.line.ClientSourceLine;
import su.plo.voice.api.client.audio.line.ClientSourceLineManager;
import su.plo.voice.api.client.config.overlay.OverlaySourceState;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.render.voice.VoiceIconUtil;
import su.plo.voice.proto.data.audio.line.SourceLine;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class VoiceClientSourceLineManager implements ClientSourceLineManager {

    private final VoiceClientConfig config;

    private final List<ClientSourceLine> lines = new CopyOnWriteArrayList<>() {

        @Override
        public boolean add(ClientSourceLine sourceLine) {
            int index;
            for (index = 0; index < this.size(); index++) {
                ClientSourceLine lineToCompare = this.get(index);
                if (sourceLine.getWeight() > lineToCompare.getWeight()) break;
                if (sourceLine.getWeight() == lineToCompare.getWeight()) {
                    if (sourceLine.getName().compareToIgnoreCase(lineToCompare.getName()) == -1) break;
                }
            }

            super.add(index, sourceLine);
            return true;
        }
    };
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
        lines.clear();
        lineById.clear();
    }

    @Override
    public @NotNull ClientSourceLine register(@NotNull ClientSourceLine line) {
        unregister(line.getId());

        lines.add(line);
        lineById.put(line.getId(), line);

        return line;
    }

    @Override
    public @NotNull ClientSourceLine register(@NotNull SourceLine line) {
        unregister(line.getId());

        EnumConfigEntry<OverlaySourceState> stateEntry = config.getOverlay().getSourceStates().getState(line);
        if (line.hasPlayers()) {
            if (stateEntry.value().isProximityOnly()) {
                stateEntry.set(OverlaySourceState.WHEN_TALKING);
            }
            stateEntry.setDefault(OverlaySourceState.WHEN_TALKING);
        } else {
            if (!stateEntry.value().isProximityOnly()) {
                stateEntry.set(OverlaySourceState.OFF);
            }
            stateEntry.setDefault(OverlaySourceState.OFF);
        }

        String icon = VoiceIconUtil.INSTANCE.getIcon(
                line.getIcon(),
                new ResourceLocation("plasmovoice:textures/addons/source_lines/" + line.getName())
        );

        DoubleConfigEntry volumeEntry = config.getVoice()
                .getVolumes()
                .getVolume(line.getName());
        volumeEntry.setDefault(MathLib.clamp(line.getDefaultVolume(), 0D, 1D));
        ClientSourceLine clientLine = new VoiceClientSourceLine(volumeEntry, line, icon);

        return register(clientLine);
    }

    @Override
    public @NotNull Collection<ClientSourceLine> register(@NotNull Collection<SourceLine> lines) {
        return lines.stream()
                .map(this::register)
                .collect(Collectors.toList());
    }
}
