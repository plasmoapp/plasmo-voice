package su.plo.lib.client.connection;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.profile.MinecraftGameProfile;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public final class ModServerConnection implements MinecraftServerConnection {

    @Getter
    private final ClientPacketListener connection;

    private final Map<UUID, MinecraftPlayerInfo> playerInfoById = Maps.newConcurrentMap();

    @Override
    public Optional<MinecraftPlayerInfo> getPlayerInfo(@NotNull UUID playerId) {
        PlayerInfo playerInfo = connection.getPlayerInfo(playerId);
        if (playerInfo == null) {
            playerInfoById.remove(playerId);
            return Optional.empty();
        }

        return Optional.of(playerInfoById.computeIfAbsent(
                playerId,
                (uuid) -> new MinecraftPlayerInfo(
                        new MinecraftGameProfile(playerInfo.getProfile().getId(), playerInfo.getProfile().getName())
                )
        ));
    }
}
