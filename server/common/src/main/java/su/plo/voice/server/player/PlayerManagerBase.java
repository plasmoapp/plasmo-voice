package su.plo.voice.server.player;

import com.google.common.collect.Maps;
import su.plo.voice.api.server.player.PlayerManager;
import su.plo.voice.api.server.player.VoicePlayer;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class PlayerManagerBase implements PlayerManager {
    protected final Map<UUID, VoicePlayer> playerByUUID = Maps.newConcurrentMap();

    @Override
    public Optional<VoicePlayer> getPlayer(UUID uniqueId) {
        return Optional.ofNullable(playerByUUID.get(uniqueId));
    }
}
