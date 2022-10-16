package su.plo.lib.api.server.world;

public interface MinecraftServerWorld {

    /**
     * Gets the backed world object
     */
    <T> T getInstance();
}
