package su.plo.lib.mod.client.world;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.world.MinecraftClientWorld;
import su.plo.lib.api.entity.MinecraftPlayer;
import su.plo.lib.mod.entity.ModPlayer;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public final class ModClientWorld implements MinecraftClientWorld {

    @Getter
    private final ClientLevel level;

    private final Map<UUID, MinecraftPlayer> playerById = Maps.newConcurrentMap();

    @Override
    public Optional<MinecraftPlayer> getPlayerById(@NotNull UUID playerId) {
        Player player = level.getPlayerByUUID(playerId);
        if (player == null) {
            playerById.remove(playerId);
            return Optional.empty();
        }

        return Optional.of(playerById.computeIfAbsent(
                playerId,
                (uuid) -> new ModPlayer<>(player)
        ));
    }
}
