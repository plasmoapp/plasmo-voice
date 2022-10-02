package su.plo.voice.server.mute.storage.file;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.mute.ServerMuteInfo;
import su.plo.voice.api.server.mute.storage.MuteStorage;
import su.plo.voice.server.mute.VoiceMuteManager;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
public final class JsonMuteStorage implements MuteStorage {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();
    private static final Type MUTE_MAP_TYPE = new TypeToken<Map<UUID, ServerMuteInfo>>() {
    }.getType();

    private final Map<UUID, ServerMuteInfo> muteByPlayerId = Maps.newConcurrentMap();

    private final ExecutorService executor;
    private final File file;

    @Override
    public void init() throws Exception {
        if (!file.exists()) return;

        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        Map<UUID, ServerMuteInfo> data = GSON.fromJson(bufferedReader, MUTE_MAP_TYPE);
        data.forEach((playerId, muteInfo) -> {
            if (VoiceMuteManager.isMuteValid(muteInfo)) muteByPlayerId.put(playerId, muteInfo);
        });

        if (data.size() != muteByPlayerId.size())
            saveAsync();
    }

    @Override
    public void close() throws Exception {
        save();
        executor.shutdown();
        muteByPlayerId.clear();
    }

    @Override
    public void putPlayerMute(@NotNull UUID playerId, @NotNull ServerMuteInfo muteInfo) {
        if (VoiceMuteManager.isMuteValid(muteInfo)) {
            muteByPlayerId.put(playerId, muteInfo);
            saveAsync();
        }
    }

    @Override
    public Optional<ServerMuteInfo> getMuteByPlayerId(@NotNull UUID playerId) {
        return Optional.ofNullable(muteByPlayerId.get(playerId));
    }

    @Override
    public Optional<ServerMuteInfo> removeMuteByPlayerId(@NotNull UUID playerId) {
        ServerMuteInfo muteInfo = muteByPlayerId.remove(playerId);
        if (muteInfo != null) saveAsync();

        return Optional.ofNullable(muteInfo);
    }

    @Override
    public Collection<ServerMuteInfo> getMutedPlayers() {
        return muteByPlayerId.values();
    }

    private void saveAsync() {
        executor.execute(() -> {
            try {
                save();
            } catch (Exception e) {
                LOGGER.error("Failed to save json mute storage: {}", e.toString());
                e.printStackTrace();
            }
        });
    }

    private void save() throws Exception {
        muteByPlayerId.values().stream()
                .filter(muteInfo -> !VoiceMuteManager.isMuteValid(muteInfo))
                .forEach(muteInfo -> muteByPlayerId.remove(muteInfo.getPlayerUUID()));

        try (Writer w = new FileWriter(file)) {
            w.write(GSON.toJson(muteByPlayerId));
        }
    }
}
