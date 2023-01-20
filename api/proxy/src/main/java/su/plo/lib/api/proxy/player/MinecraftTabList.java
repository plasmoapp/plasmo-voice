package su.plo.lib.api.proxy.player;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface MinecraftTabList {

    boolean containsEntry(@NotNull UUID playerId);
}
