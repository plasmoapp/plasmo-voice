package su.plo.voice.api.server;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.MinecraftCommonServerLib;
import su.plo.voice.api.PlasmoVoice;
import su.plo.voice.api.server.audio.capture.ServerActivationManager;
import su.plo.voice.api.server.audio.line.ServerSourceLineManager;
import su.plo.voice.api.server.audio.source.BaseServerSourceManager;
import su.plo.voice.api.server.config.ServerLanguages;
import su.plo.voice.api.server.connection.UdpConnectionManager;
import su.plo.voice.api.server.player.VoicePlayerManager;

/**
 * Server common Plasmo Voice API
 * <br/>
 * Common interface for proxy and server
 */
public interface PlasmoCommonVoiceServer extends PlasmoVoice {

    /**
     * Gets the {@link MinecraftCommonServerLib}
     *
     * @return {@link MinecraftCommonServerLib}
     */
    @NotNull MinecraftCommonServerLib getMinecraftServer();

    /**
     * Gets the {@link VoicePlayerManager}
     *
     * This manager can be used to get voice players
     *
     * @return {@link VoicePlayerManager}
     */
    @NotNull VoicePlayerManager<?> getPlayerManager();

    /**
     * Gets the {@link UdpConnectionManager}
     *
     * This manager can be used to broadcast or manage udp connections
     *
     * @return {@link UdpConnectionManager}
     */
    @NotNull UdpConnectionManager<?, ?> getUdpConnectionManager();

    /**
     * Gets the {@link ServerSourceLineManager}
     *
     * @return {@link ServerSourceLineManager}
     */
    @NotNull ServerSourceLineManager getSourceLineManager();

    /**
     * Gets the {@link ServerActivationManager}
     *
     * @return {@link ServerActivationManager}
     */
    @NotNull ServerActivationManager getActivationManager();

    /**
     * Gets the {@link BaseServerSourceManager}
     *
     * @return {@link BaseServerSourceManager}
     */
    @NotNull BaseServerSourceManager getSourceManager();

    /**
     * Get the {@link ServerLanguages}
     *
     * @return {@link ServerLanguages}
     */
    @NotNull ServerLanguages getLanguages();
}
