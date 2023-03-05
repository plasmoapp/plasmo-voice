package su.plo.lib.api.server.world;

import org.jetbrains.annotations.NotNull;

public interface MinecraftServerWorld {

    /**
     * @return world key
     */
    @NotNull String getKey();

    /**
     * Gets the server's implementation instance
     * <ul>
     * <li>{@code org.bukkit.World} for bukkit</li>
     * <li>{@code net.minecraft.server.level.ServerLevel} for mods (fabric/forge)</li>
     * </ul>
     *
     * @return server's implementation object
     */
    <T> T getInstance();
}
