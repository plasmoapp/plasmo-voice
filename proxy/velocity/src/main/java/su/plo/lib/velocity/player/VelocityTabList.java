package su.plo.lib.velocity.player;

import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.proxy.player.MinecraftTabList;

import java.util.UUID;

@RequiredArgsConstructor
public final class VelocityTabList implements MinecraftTabList {

    private final Player player;

    @Override
    public boolean containsEntry(@NotNull UUID playerId) {
        return player.getTabList().containsEntry(playerId);
    }
}
