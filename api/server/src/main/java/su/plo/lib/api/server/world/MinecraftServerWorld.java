package su.plo.lib.api.server.world;

import org.jetbrains.annotations.NotNull;

public interface MinecraftServerWorld {

    /**
     * Get the world key
     */
    @NotNull String getKey();

    /**
     * Gets the backed world object
     */
    <T> T getInstance();
}
