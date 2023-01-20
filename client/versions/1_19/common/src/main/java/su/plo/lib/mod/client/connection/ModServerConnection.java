package su.plo.lib.mod.client.connection;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.connection.MinecraftPlayerInfo;
import su.plo.lib.api.client.connection.MinecraftServerConnection;
import su.plo.voice.proto.data.player.MinecraftGameProfile;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
                (uuid) -> new MinecraftPlayerInfo(getGameProfile(playerInfo.getProfile()))
        ));
    }

    private MinecraftGameProfile getGameProfile(@NotNull GameProfile gameProfile) {
        return new MinecraftGameProfile(
                gameProfile.getId(),
                gameProfile.getName(),
                gameProfile.getProperties().values().stream()
                        .map((property) -> new MinecraftGameProfile.Property(
                                property.getName(),
                                property.getValue(),
                                property.getSignature()
                        ))
                        .collect(Collectors.toList())
        );
    }
}
