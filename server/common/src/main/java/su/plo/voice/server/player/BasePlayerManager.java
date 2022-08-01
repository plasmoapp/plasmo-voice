package su.plo.voice.server.player;

import com.google.common.collect.Maps;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.player.PlayerManager;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.server.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class BasePlayerManager implements PlayerManager {

    protected final Map<UUID, VoicePlayer> playerById = Maps.newConcurrentMap();

    @Override
    public Optional<VoicePlayer> getPlayer(UUID playerId) {
        return Optional.ofNullable(playerById.get(playerId));
    }

    @EventSubscribe
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerById.remove(event.getPlayerId());
    }
}
