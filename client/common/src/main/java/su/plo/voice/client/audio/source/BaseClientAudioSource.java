package su.plo.voice.client.audio.source;

import com.google.common.base.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.codec.AudioDecoder;
import su.plo.voice.api.audio.codec.CodecException;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.AlAudioDevice;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.device.DeviceType;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.client.audio.device.source.DeviceSource;
import su.plo.voice.api.client.audio.device.source.SourceGroup;
import su.plo.voice.api.client.audio.source.ClientAudioSource;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.client.event.audio.device.source.AlSourceClosedEvent;
import su.plo.voice.api.client.event.audio.source.AudioSourceClosedEvent;
import su.plo.voice.api.client.util.AudioUtil;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.api.encryption.EncryptionException;
import su.plo.voice.api.event.EventPriority;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.util.Params;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.config.entry.DoubleConfigEntry;
import su.plo.voice.proto.data.source.SourceInfo;
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket;
import su.plo.voice.proto.packets.udp.cllientbound.SourceAudioPacket;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseClientAudioSource<T extends SourceInfo> implements ClientAudioSource<T> {

    protected static final Logger LOGGER = LogManager.getLogger();

    protected final PlasmoVoiceClient voiceClient;
    protected final ClientConfig config;

    protected final float[] position = new float[3];
    protected final float[] lookAngle = new float[3];

    protected ServerInfo.VoiceInfo voiceInfo;
    protected T sourceInfo;
    protected DoubleConfigEntry sourceVolume;
    protected Encryption encryption;
    protected AudioDecoder decoder;
    protected SourceGroup sourceGroup;

    protected long lastSequenceNumber = -1L;

    protected AtomicBoolean closed = new AtomicBoolean(false);

    public BaseClientAudioSource(@NotNull PlasmoVoiceClient voiceClient, ClientConfig config) {
        this.voiceClient = voiceClient;
        this.config = config;
    }

    @Override
    public void initialize(T sourceInfo) throws DeviceException {
        Optional<ServerInfo> optServerInfo = voiceClient.getServerInfo();
        if (!optServerInfo.isPresent()) throw new IllegalStateException("Not connected");

        ServerInfo serverInfo = optServerInfo.get();
        this.voiceInfo = serverInfo.getVoiceInfo();

        int bufferSize = (serverInfo.getVoiceInfo().getSampleRate() / 1_000) * 20;

        if (Strings.emptyToNull(sourceInfo.getCodec()) != null) {
            this.decoder = voiceClient.getCodecManager().createDecoder(
                    sourceInfo.getCodec(),
                    Params.builder()
                            .set("sampleRate", serverInfo.getVoiceInfo().getSampleRate())
                            .set("bufferSize", bufferSize)
                            .build()
            );
        }

        if (serverInfo.getEncryption().isPresent())
            this.encryption = serverInfo.getEncryption().get();

        this.sourceGroup = voiceClient.getDeviceManager().createSourceGroup(DeviceType.OUTPUT);
        sourceGroup.create(Params.EMPTY);
        for (DeviceSource source : sourceGroup.getSources()) {
            if (source instanceof AlSource) {
                AlSource alSource = (AlSource) source;
                AlAudioDevice device = (AlAudioDevice) alSource.getDevice();

                device.runInContext(() -> {
                    alSource.setFloat(0x100E, 4F); // AL_MAX_GAIN
                    alSource.setInt(0xD000, 0xD004); // AL_DISTANCE_MODEL // AL_LINEAR_DISTANCE_CLAMPED

                    alSource.play();
                });
            }
        }

        this.sourceVolume = config.getVoice()
                .getVolumes()
                .getVolume("source_" + sourceInfo.getId());
        this.sourceInfo = sourceInfo;

        LOGGER.info("Source {} initialized", sourceInfo);
    }

    @Override
    public @NotNull T getInfo() {
        return sourceInfo;
    }

    @Override
    public void updateInfo(T sourceInfo) {
        // todo: update decoder
        this.sourceInfo = sourceInfo;
    }

    @Override
    public void process(@NotNull SourceAudioPacket packet) {
        if (this.lastSequenceNumber >= 0 && packet.getSequenceNumber() <= this.lastSequenceNumber) {
            LOGGER.info("Drop packet with bad order");
            return;
        }

        try {
            getPosition(position);
            getLookAngle(lookAngle);
        } catch (IllegalStateException e) {
            close();
            return;
        }

        int distance = packet.getDistance();

//        boolean isPriority = packet.getDistance() > voiceInfo.getMaxDistance();
        double volume = sourceVolume.value();
        int fadeDistance = distance / voiceInfo.getFadeDivisor(); // todo: activation fade divisor?

        // todo: occlusion

        updateSource((float) volume, fadeDistance, packet.getDistance());

        // packet compensation
        if (lastSequenceNumber >= 0 && decoder != null) { // todo: check if decoder can compensate lost packets
            int packetsToCompensate = (int) (packet.getSequenceNumber() - (lastSequenceNumber + 1));
            if (packetsToCompensate <= 4) {
                LOGGER.debug("Compensate {} packets", packetsToCompensate);

                for (int i = 0; i < packetsToCompensate; i++) {
                    try {
                        write(decoder.decode(null));
                    } catch (CodecException e) {
                        LOGGER.warn("Failed to decode source audio", e);
                        return;
                    }
                }
            }
        }

        try {
            byte[] decrypted = packet.getData();
            if (encryption != null) {
                try {
                    decrypted = encryption.decrypt(decrypted);
                } catch (EncryptionException e) {
                    LOGGER.warn("Failed to decrypt source audio", e);
                    return;
                }
            }

            if (decoder != null) {
                write(decoder.decode(decrypted));
            } else {
                write(decrypted);
            }
        } catch (CodecException e) {
            LOGGER.warn("Failed to decode source audio", e);
            return;
        }

        this.lastSequenceNumber = packet.getSequenceNumber();
    }

    @Override
    public void process(@NotNull SourceAudioEndPacket packet) {
        for (DeviceSource source : sourceGroup.getSources()) {
            source.write(null);
        }

        this.lastSequenceNumber = packet.getSequenceNumber();
        if (decoder != null) decoder.reset();
    }

    @Override
    public void close() {
        if (decoder != null && decoder.isOpen()) decoder.close();
        closed.set(true);

        sourceGroup.getSources().forEach(DeviceSource::close);
        voiceClient.getEventBus().call(new AudioSourceClosedEvent(this));
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    @EventSubscribe(priority = EventPriority.LOWEST)
    public void onSourceClosed(AlSourceClosedEvent event) {
        if (closed.get() || !sourceGroup.getSources().contains(event.getSource())) return;
        close();
    }

    private void updateSource(float volume, int fadeDistance, int maxDistance) {
        for (DeviceSource source : sourceGroup.getSources()) {
            if (source instanceof AlSource) {
                AlSource alSource = (AlSource) source;
                AlAudioDevice device = (AlAudioDevice) alSource.getDevice();

                device.runInContext(() -> {
                    alSource.setVolume(volume);
                    alSource.setFloatArray(0x1004, position); // AL_POSITION
                    alSource.setFloatArray(0x1005, lookAngle); // AL_DIRECTION
                    alSource.setFloat(0x1020, fadeDistance); // AL_REFERENCE_DISTANCE
                    alSource.setFloat(0x1023, maxDistance); // AL_MAX_DISTANCE
                    alSource.setFloat(0x1021, 0.95F); // AL_ROLLOFF_FACTOR
                });
            }
        }
    }

    private void write(short[] samples) {
        write(AudioUtil.shortsToBytes(samples));
    }

    private void write(byte[] samples) {
        for (DeviceSource source : sourceGroup.getSources()) {
            source.write(samples);
        }
    }

    protected abstract float[] getPosition(float[] position);

    protected abstract float[] getLookAngle(float[] lookAngle);
}
