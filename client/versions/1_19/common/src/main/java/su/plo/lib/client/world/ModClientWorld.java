package su.plo.lib.client.world;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.entity.MinecraftPlayer;
import su.plo.lib.entity.ModPlayer;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public final class ModClientWorld implements MinecraftClientWorld {

    private final Minecraft minecraft = Minecraft.getInstance();

    @Getter
    private final ClientLevel level;

    private final Map<UUID, MinecraftPlayer> playerById = Maps.newConcurrentMap();

    @Override
    public Optional<MinecraftPlayer> getPlayerById(@NotNull UUID playerId) {
        Player player = level.getPlayerByUUID(playerId);
        if (player == null) return Optional.empty();

        return Optional.of(playerById.computeIfAbsent(
                playerId,
                (uuid) -> new ModPlayer<>(player)
        ));
    }
}
