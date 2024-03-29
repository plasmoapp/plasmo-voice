package su.plo.voice.api.server;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.voice.api.addon.AddonsLoader;
import su.plo.voice.api.addon.ServerAddonsLoader;
import su.plo.voice.api.audio.codec.AudioDecoder;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.api.server.audio.line.ServerSourceLineManager;
import su.plo.voice.api.server.config.ServerConfig;
import su.plo.voice.api.server.connection.TcpServerConnectionManager;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.mute.MuteManager;
import su.plo.voice.api.server.player.VoicePlayerManager;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.api.server.socket.UdpServer;

import java.util.Optional;

/**
 * The Plasmo Client Server API
 */
public interface PlasmoVoiceServer extends PlasmoBaseVoiceServer {

    /**
     * Gets the server's addons loaders
     *
     * <p>Use this method to get the addons loader for loading server/proxy addons from Spigot/Forge/Fabric</p>
     *
     * @return the addons loader
     */
    static AddonsLoader getAddonsLoader() {
        return ServerAddonsLoader.INSTANCE;
    }

    /**
     * Gets the {@link MinecraftServerLib}
     *
     * @return {@link MinecraftServerLib}
     */
    @NotNull MinecraftServerLib getMinecraftServer();

    /**
     * Gets the {@link VoicePlayerManager}
     * <p>
     * This manager can be used to get voice players
     *
     * @return {@link VoicePlayerManager}
     */
    @NotNull VoicePlayerManager<VoiceServerPlayer> getPlayerManager();

    /**
     * Gets the {@link ServerSourceLineManager}
     *
     * @return {@link ServerSourceLineManager}
     */
    @NotNull ServerSourceLineManager getSourceLineManager();

    /**
     * Gets the {@link MuteManager}
     * <p>
     * This manager can be used to mute or unmute players voice
     *
     * @return {@link MuteManager}
     */
    @NotNull MuteManager getMuteManager();

    /**
     * Gets the {@link TcpServerConnectionManager}
     * <p>
     * This manager can be used to broadcast to tcp connections
     *
     * @return {@link TcpServerConnectionManager}
     */
    @NotNull TcpServerConnectionManager getTcpConnectionManager();

    /**
     * Gets the {@link UdpServerConnectionManager}
     * <p>
     * This manager can be used to broadcast or manage udp connections
     *
     * @return {@link UdpServerConnectionManager}
     */
    @NotNull UdpServerConnectionManager getUdpConnectionManager();

    /**
     * Gets the {@link UdpServer}
     *
     * @return {@link UdpServer}
     */
    Optional<UdpServer> getUdpServer();

    /**
     * Gets the {@link ServerConfig}
     *
     * @return {@link ServerConfig}
     */
    ServerConfig getConfig();

    /**
     * Gets a default encryption
     * <br/>
     * AES/CBC/PKCS5Padding used by default
     * <br/>
     * Can be changed if server is behind the proxy,
     * so don't store reference to this in addons
     *
     * @return {@link Encryption} instance
     */
    @NotNull Encryption getDefaultEncryption();

    /**
     * Creates a new opus encoder
     * <br/>
     * params will be created from {@link ServerConfig}
     */
    @NotNull AudioEncoder createOpusEncoder(boolean stereo);

    /**
     * Creates a new opus decoder
     * <br/>
     * params will be created from {@link ServerConfig}
     */
    @NotNull AudioDecoder createOpusDecoder(boolean stereo);
}
