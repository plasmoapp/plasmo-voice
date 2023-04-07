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
 * Manages audio capture thread
 *
 * <p>
 *     On each thread iteration, iterates all {@link ClientActivation}s ordered by {@link ClientActivation#getWeight()}
 *     and if {@link ClientActivation} is activated - encodes, encrypts and sends {@link PlayerAudioPacket}
 *     to the UDP server using {@link UdpClient}
 * </p>
 * <p>
 *     If {@link ClientActivation} supports stereo and stereo capture is enabled in config - stereo encoder
 *     will be used to encode audio data, otherwise - mono encoder
 * </p>
 * <p>
 *     By default, default encoders from {@link AudioCapture} will be used.
 *     But if {@link ClientActivation#getMonoEncoder()} or {@link ClientActivation#getStereoEncoder()}
 *     is not null, then they will be used instead
 * </p>
 */
public interface AudioCapture {

    /**
     * @return default mono encoder if {@link ServerInfo}'s {@link CaptureInfo} is not null
     */
    Optional<AudioEncoder> getDefaultMonoEncoder();

    /**
     * @return default stereo encoder if {@link ServerInfo}'s {@link CaptureInfo} is not null
     */
    Optional<AudioEncoder> getDefaultStereoEncoder();

    /**
     * @return default stereo encoder if {@link ServerInfo}'s {@link Encryption} is not null
     */
    Optional<Encryption> getEncryption();

    /**
     * @return current input device if it was initialized in {@link #initialize(ServerInfo)}
     */
    Optional<InputDevice> getDevice();

    /**
     * Initializes {@link AudioCapture} and starts a new thread that captures audio data from opened microphone.
     * After audio data capture, {@link AudioCapture} iterates all {@link ClientActivation}s by
     *
     * @param serverInfo
     */
    void initialize(@NotNull ServerInfo serverInfo);

    /**
     * Starts a new thread that reads audio data from {@link InputDevice}.
     */
    void start();

    /**
     * Stops the thread if {@link #isActive()}, otherwise doing nothing
     */
    void stop();

    /**
     * @return true if capture thread is alive
     */
    boolean isActive();

    /**
     * @return true if client muted on the server
     */
    boolean isServerMuted();
}
