package su.plo.voice.api.server;

import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.McLib;
import su.plo.voice.api.PlasmoVoice;
import su.plo.voice.api.audio.codec.AudioDecoder;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.api.server.audio.capture.ServerActivationManager;
import su.plo.voice.api.server.audio.line.BaseServerSourceLine;
import su.plo.voice.api.server.audio.line.BaseServerSourceLineManager;
import su.plo.voice.api.server.connection.UdpConnectionManager;
import su.plo.voice.api.server.language.ServerLanguages;
import su.plo.voice.api.server.player.VoicePlayerManager;

/**
 * Represents a base API for proxy and server.
 */
public interface PlasmoBaseVoiceServer extends PlasmoVoice {

    /**
     * Gets the server languages.
     *
     * @return The server languages.
     */
    @NotNull ServerLanguages getLanguages();

    /**
     * Gets the {@link McLib}.
     *
     * @return The {@link McLib}.
     */
    @NotNull McLib getMinecraftServer();

    /**
     * Gets the {@link VoicePlayerManager}.
     *
     * <p>
     *     This manager can be used to get voice players.
     * </p>
     *
     * @return The {@link VoicePlayerManager}.
     */
    @NotNull VoicePlayerManager<?> getPlayerManager();

    /**
     * Gets the {@link UdpConnectionManager}.
     *
     * @return The {@link UdpConnectionManager}.
     */
    @NotNull UdpConnectionManager<?, ?> getUdpConnectionManager();

    /**
     * Gets the {@link BaseServerSourceLineManager}.
     *
     * <p>
     *     Source lines are used to create audio sources.
     *     To create audio source, you need to create source line using {@link BaseServerSourceLineManager#createBuilder}
     *     and then you can create audio sources using your {@link BaseServerSourceLine}.
     * </p>
     *
     * @return {@link BaseServerSourceLineManager}.
     */
    @NotNull BaseServerSourceLineManager<?> getSourceLineManager();

    /**
     * Gets the {@link ServerActivationManager}.
     *
     * @return The {@link ServerActivationManager}.
     */
    @NotNull ServerActivationManager getActivationManager();

    /**
     * Gets the default encryption instance.
     * <br/>
     * AES/CBC/PKCS5Padding is used by default.
     *
     * @return The {@link Encryption} instance.
     */
    @NotNull Encryption getDefaultEncryption();

    /**
     * Creates a new opus encoder using default params.
     *
     * @param stereo {@code true} if the encoder should be initialized in stereo mode.
     * @return {@link AudioEncoder} instance.
     */
    @NotNull AudioEncoder createOpusEncoder(boolean stereo);

    /**
     Creates a new opus encoder using default params.
     *
     * @param stereo {@code true} if the decoder should be initialized in stereo mode.
     * @return {@link AudioDecoder } instance.
     */
    @NotNull AudioDecoder createOpusDecoder(boolean stereo);
}
