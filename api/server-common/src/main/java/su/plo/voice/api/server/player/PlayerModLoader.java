package su.plo.voice.api.server.player;

/**
 * Player's mod loader
 * <br/>
 * FORGE is detected by fml:handshake channel,
 * otherwise FABRIC will be returned
 */
public enum PlayerModLoader {

    FABRIC,
    FORGE
}
