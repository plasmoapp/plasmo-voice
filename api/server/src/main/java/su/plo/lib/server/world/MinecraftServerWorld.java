package su.plo.lib.server.world;

public interface MinecraftServerWorld {

    /**
     * Gets the backed world object
     */
    <T> T getInstance();
}
