package su.plo.voice.api.server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.slib.api.server.McServerLib;
import su.plo.voice.api.addon.AddonsLoader;
import su.plo.voice.api.addon.ServerAddonsLoader;
import su.plo.voice.api.audio.codec.AudioDecoder;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.audio.line.ServerSourceLineManager;
import su.plo.voice.api.server.config.ServerConfig;
import su.plo.voice.api.server.connection.TcpServerPacketManager;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.mute.MuteManager;
import su.plo.voice.api.server.player.VoicePlayerManager;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.api.server.player.VoiceServerPlayerManager;
import su.plo.voice.api.server.socket.UdpServer;

import java.util.Optional;

/**
 * The Plasmo Voice Server API.
 */
public interface PlasmoVoiceServer extends PlasmoBaseVoiceServer {

    /**
     * Gets the server's addons loader.
     *
     * <p>Use this method to get the addons loader for loading server addons from Spigot/Forge/Fabric.</p>
     *
     * @return The addons loader.
     */
    static AddonsLoader getAddonsLoader() {
        return ServerAddonsLoader.INSTANCE;
    }

    /**
     * Gets the {@link McServerLib}.
     *
     * @return The {@link McServerLib}.
     */
    @NotNull McServerLib getMinecraftServer();

    /**
     * Gets the {@link VoiceServerPlayerManager}.
     *
     * <p>
     *     This manager can be used to get voice players.
     * </p>
     *
     * @return The {@link VoiceServerPlayerManager}.
     */
    @NotNull VoiceServerPlayerManager getPlayerManager();

    /**
     * Gets the {@link ServerSourceLineManager}.
     *
     * <p>
     *     Source lines are used to create audio sources.
     *     To create audio source, you need to create source line using {@link ServerSourceLineManager#createBuilder}
     *     and then you can create audio sources using your {@link ServerSourceLine}.
     * </p>
     *
     * @return The {@link ServerSourceLineManager}.
     */
    @NotNull ServerSourceLineManager getSourceLineManager();

    /**
     * Gets the {@link MuteManager}.
     *
     * <p>
     *     This manager is used to mute or unmute voice for the players.
     * </p>
     *
     * @return The {@link MuteManager}.
     */
    @NotNull MuteManager getMuteManager();

    /**
     * Gets the {@link TcpServerPacketManager}.
     *
     * @return The {@link TcpServerPacketManager}.
     */
    @NotNull TcpServerPacketManager getTcpPacketManager();

    /**
     * Gets the {@link UdpServerConnectionManager}.
     *
     * @return The {@link UdpServerConnectionManager}.
     */
    @NotNull UdpServerConnectionManager getUdpConnectionManager();

    /**
     * Gets the {@link UdpServer}.
     *
     * @return The {@link UdpServer}.
     */
    Optional<UdpServer> getUdpServer();

    /**
     * Gets the {@link ServerConfig}.
     *
     * @return The {@link ServerConfig} or null if server is not initialized yet.
     */
    @Nullable ServerConfig getConfig();

    /**
     * Gets the default encryption instance.
     * <br/>
     * AES/CBC/PKCS5Padding is used by default.
     *
     * @return The {@link Encryption} instance.
     */
    @NotNull Encryption getDefaultEncryption();

    /**
     * Creates a new opus encoder using params created from {@link ServerConfig}.
     *
     * @param stereo {@code true} if the encoder should be initialized in stereo mode.
     * @return {@link AudioEncoder} instance.
     */
    @NotNull AudioEncoder createOpusEncoder(boolean stereo);

    /**
     * Creates a new opus decoder using params created from {@link ServerConfig}.
     *
     * @param stereo {@code true if the decoder should be initialized in stereo mode.
     * @return {@link AudioDecoder} instance.
     */
    @NotNull AudioDecoder createOpusDecoder(boolean stereo);
}
