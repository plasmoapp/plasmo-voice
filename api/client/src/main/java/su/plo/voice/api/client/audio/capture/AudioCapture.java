package su.plo.voice.api.client.audio.capture;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.client.audio.device.InputDevice;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.client.socket.UdpClient;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.proto.data.audio.capture.CaptureInfo;
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket;

import java.util.Optional;

/**
 * Manages audio capture thread.
 *
 * <p>
 *     On each thread iteration, iterates all {@link ClientActivation}s ordered by {@link ClientActivation#getWeight()}
 *     and if {@link ClientActivation} is activated - encodes, encrypts and sends {@link PlayerAudioPacket}
 *     to the UDP server using {@link UdpClient}.
 * </p>
 * <p>
 *     If {@link ClientActivation} supports stereo and stereo capture is enabled in config - stereo encoder
 *     will be used to encode audio data, otherwise - mono encoder.
 * </p>
 * <p>
 *     By default, the default encoders from {@link AudioCapture} will be used.
 *     However if {@link ClientActivation#getMonoEncoder()} or {@link ClientActivation#getStereoEncoder()}
 *     is not null, then they will be used instead.
 * </p>
 */
public interface AudioCapture {

    /**
     * Gets the default mono encoder if {@link ServerInfo}'s {@link CaptureInfo} is not null.
     *
     * @return An optional containing the default mono encoder, if available; otherwise, an empty optional.
     */
    Optional<AudioEncoder> getDefaultMonoEncoder();

    /**
     * Gets the default stereo encoder if {@link ServerInfo}'s {@link CaptureInfo} is not null.
     *
     * @return An optional containing the default stereo encoder, if available; otherwise, an empty optional.
     */
    Optional<AudioEncoder> getDefaultStereoEncoder();

    /**
     * Gets the default encryption method if {@link ServerInfo}'s {@link Encryption} is not null.
     *
     * @return An optional containing the default encryption method, if available; otherwise, an empty optional.
     */
    Optional<Encryption> getEncryption();

    /**
     * Gets the current input device if it was initialized in {@link #initialize(ServerInfo)}.
     *
     * @return An optional containing the current input device, if available; otherwise, an empty optional.
     */
    Optional<InputDevice> getDevice();

    /**
     * Initializes {@link AudioCapture} and starts a new thread that captures audio data from the opened input device.
     * After audio data capture, {@link AudioCapture} iterates all {@link ClientActivation}s.
     *
     * @param serverInfo The server information to initialize audio capture.
     */
    void initialize(@NotNull ServerInfo serverInfo);

    /**
     * Starts a new thread that reads audio data from the {@link InputDevice}.
     */
    void start();

    /**
     * Stops the thread if {@link #isActive()} is {@code true}; otherwise, it does nothing.
     */
    void stop();

    /**
     * Checks if the capture thread is active.
     *
     * @return {@code true} if the capture thread is alive; otherwise, {@code false}.
     */
    boolean isActive();

    /**
     * Checks if the client is muted on the server.
     *
     * @return {@code true} if the client is muted on the server; otherwise, {@code false}.
     */
    boolean isServerMuted();
}
