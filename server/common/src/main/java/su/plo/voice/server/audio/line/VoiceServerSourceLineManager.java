package su.plo.voice.server.audio.line;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.addon.AddonManager;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.line.ServerPlayersSourceLine;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.audio.line.ServerSourceLineManager;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class VoiceServerSourceLineManager implements ServerSourceLineManager {

    private final PlasmoVoiceServer voiceServer;
    private final AddonManager addons;

    private final Map<UUID, ServerSourceLine> lineById = Maps.newConcurrentMap();

    public VoiceServerSourceLineManager(@NotNull PlasmoVoiceServer voiceServer) {
        this.voiceServer = voiceServer;
        this.addons = voiceServer.getAddonManager();

        register(
                voiceServer,
                VoiceSourceLine.PROXIMITY_NAME,
                "key.plasmovoice.proximity",
                "plasmovoice:textures/icons/speaker.png",
                1
        );
    }

    @Override
    public Optional<ServerSourceLine> getLineById(@NotNull UUID id) {
        return Optional.ofNullable(lineById.get(id));
    }

    @Override
    public Optional<ServerSourceLine> getLineByName(@NotNull String name) {
        return Optional.ofNullable(lineById.get(VoiceSourceLine.generateId(name + "_line")));
    }

    @Override
    public Collection<ServerSourceLine> getLines() {
        return lineById.values();
    }

    @Override
    public @NotNull ServerSourceLine register(@NotNull Object addonObject,
                                              @NotNull String name,
                                              @NotNull String translation,
                                              @NotNull String icon,
                                              int weight) {
        Optional<AddonContainer> addon = addons.getAddon(addonObject);
        if (!addon.isPresent()) throw new IllegalArgumentException("addonObject is not an addon");

        return lineById.computeIfAbsent(
                VoiceSourceLine.generateId(name),
                (id) -> new VoiceServerSourceLine(
                        voiceServer,
                        addon.get(),
                        name,
                        translation,
                        icon,
                        weight
                )
        );
    }

    @Override
    public @NotNull ServerPlayersSourceLine registerPlayers(@NotNull Object addonObject,
                                                            @NotNull String name,
                                                            @NotNull String translation,
                                                            @NotNull String icon,
                                                            int weight) {
        Optional<AddonContainer> addon = addons.getAddon(addonObject);
        if (!addon.isPresent()) throw new IllegalArgumentException("addonObject is not an addon");

        return (ServerPlayersSourceLine) lineById.computeIfAbsent(
                VoiceSourceLine.generateId(name),
                (id) -> new VoiceServerPlayersSourceLine(
                        voiceServer,
                        addon.get(),
                        name,
                        translation,
                        icon,
                        weight
                )
        );
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
    public boolean unregister(@NotNull ServerSourceLine line) {
        return unregister(line.getId());
    }
}
