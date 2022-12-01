package su.plo.voice.client.audio.source;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.voice.api.audio.codec.AudioDecoder;
import su.plo.voice.api.audio.codec.CodecException;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.AlAudioDevice;
import su.plo.voice.api.client.audio.device.AlListenerDevice;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.device.DeviceType;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.client.audio.device.source.DeviceSource;
import su.plo.voice.api.client.audio.device.source.SourceGroup;
import su.plo.voice.api.client.audio.line.ClientSourceLine;
import su.plo.voice.api.client.audio.source.ClientAudioSource;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.client.event.audio.device.source.AlSourceClosedEvent;
import su.plo.voice.api.client.event.audio.device.source.AlStreamSourceStoppedEvent;
import su.plo.voice.api.client.event.audio.source.AudioSourceClosedEvent;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.api.encryption.EncryptionException;
import su.plo.voice.api.event.EventPriority;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.util.AudioUtil;
import su.plo.voice.api.util.Params;
import su.plo.voice.client.audio.codec.AudioDecoderPlc;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.audio.source.SourceInfo;
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket;
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseClientAudioSource<T extends SourceInfo> implements ClientAudioSource<T> {

    private static final float[] ZERO_VECTOR = new float[]{0F, 0F, 0F};
    protected static final Logger LOGGER = LogManager.getLogger();

    protected final PlasmoVoiceClient voiceClient;
    protected final ClientConfig config;
    protected final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    protected final float[] playerPosition = new float[3];
    protected final float[] position = new float[3];
    protected final float[] lookAngle = new float[3];

    protected ServerInfo.VoiceInfo voiceInfo;
    protected T sourceInfo;
    protected DoubleConfigEntry lineVolume;
    protected DoubleConfigEntry sourceVolume;
    protected Encryption encryption;
    protected AudioDecoder decoder;
    protected SourceGroup sourceGroup;

    protected ScheduledFuture<?> endRequest;

    protected Map<UUID, Long> lastSequenceNumbers = Maps.newHashMap();
    protected long lastActivation = 0L;
    protected double lastOcclusion = -1D;
    protected AtomicBoolean closed = new AtomicBoolean(false);
    protected AtomicBoolean resetted = new AtomicBoolean(false);
    protected AtomicBoolean activated = new AtomicBoolean(false);

    public BaseClientAudioSource(@NotNull PlasmoVoiceClient voiceClient, ClientConfig config) {
        this.voiceClient = voiceClient;
        this.config = config;
    }

    @Override
    public void initialize(T sourceInfo) throws DeviceException {
        ServerInfo serverInfo = voiceClient.getServerInfo()
                .orElseThrow(() -> new IllegalStateException("Not connected"));

        this.voiceInfo = serverInfo.getVoiceInfo();

        boolean stereoChanged = this.sourceInfo != null && isStereo(sourceInfo) != isStereo(this.sourceInfo);

        // initialize sources
        if (this.sourceInfo == null || (stereoChanged && sourceGroup != null)) {
            if (sourceGroup == null) {
                this.sourceGroup = voiceClient.getDeviceManager().createSourceGroup(DeviceType.OUTPUT);
            } else {
                SourceGroup oldSourceGroup = this.sourceGroup;
                this.sourceGroup = voiceClient.getDeviceManager().createSourceGroup(DeviceType.OUTPUT);
                oldSourceGroup.clear();
            }

            sourceGroup.create(isStereo(sourceInfo), Params.EMPTY);
            for (DeviceSource source : sourceGroup.getSources()) {
                if (source instanceof AlSource) {
                    AlSource alSource = (AlSource) source;
                    AlAudioDevice device = (AlAudioDevice) alSource.getDevice();

                    device.runInContext(() -> {
                        alSource.setFloat(0x100E, 4F); // AL_MAX_GAIN
                        alSource.setInt(0xD000, 0xD003); // AL_DISTANCE_MODEL // AL_LINEAR_DISTANCE

                        alSource.play();
                    });
                }
            }
            LOGGER.info("Initialize device sources");
        }

        // initialize decoder
        if (this.sourceInfo == null || (sourceInfo.isStereo() != this.sourceInfo.isStereo())) {
            AudioDecoder decoder = null;
            if (Strings.emptyToNull(sourceInfo.getCodec()) != null) {
                decoder = voiceClient.getCodecManager().createDecoder(
                        sourceInfo.getCodec(),
                        voiceInfo.getCapture().getSampleRate(),
                        sourceInfo.isStereo(),
                        serverInfo.getVoiceInfo().getBufferSize(),
                        serverInfo.getVoiceInfo().getCapture().getMtuSize(),
                        Params.EMPTY
                );
            }

            if (this.decoder != null) this.decoder.close();
            this.decoder = decoder;
            LOGGER.info("Initialize decoder");
        }

        // initialize encryption
        if (serverInfo.getEncryption().isPresent())
            this.encryption = serverInfo.getEncryption().get();

        // initialize volumes
        if (lineVolume == null || sourceVolume == null) {
            ClientSourceLine sourceLine = voiceClient.getSourceLineManager().getLineById(sourceInfo.getLineId())
                    .orElseThrow(() -> new IllegalStateException("Source line not found"));

            this.lineVolume = config.getVoice()
                    .getVolumes()
                    .getVolume(sourceLine.getName());

            this.sourceVolume = config.getVoice()
                    .getVolumes()
                    .getVolume("source_" + sourceInfo.getId().toString());

            LOGGER.info("Source {} initialized", sourceInfo);
        }

        this.sourceInfo = sourceInfo;
    }

    @Override
    public @NotNull T getInfo() {
        return sourceInfo;
    }

    @Override
    public void process(@NotNull SourceAudioPacket packet) {
        if (isClosed()) return;

        executor.execute(() -> processAudioPacket(packet));
    }

    @Override
    public void process(@NotNull SourceAudioEndPacket packet) {
        if (isClosed()) return;

        executor.execute(() -> processAudioEndPacket(packet));
        if (endRequest != null) endRequest.cancel(false);
        this.endRequest = executor.schedule(
                this::reset,
                100L,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public synchronized void close() {
        activated.set(false);
        closed.set(true);
        if (!executor.isShutdown()) executor.shutdown();

        if (decoder != null && decoder.isOpen()) {
            decoder.close();
            this.decoder = null;
        }

        sourceGroup.clear();
        this.sourceGroup = null;

        voiceClient.getEventBus().call(new AudioSourceClosedEvent(this));
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public boolean isActivated() {
        if (activated.get()) {
            if (System.currentTimeMillis() - lastActivation > 500L) {
                LOGGER.warn("Voice end packet was not received. Resetting audio source");
                reset();
                return false;
            }

            return true;
        }

        return false;
    }

    @EventSubscribe(priority = EventPriority.LOWEST)
    public void onSourceClosed(@NotNull AlSourceClosedEvent event) {
        if (closed.get() || !sourceGroup.getSources().contains(event.getSource())) return;
        close();
    }

    @EventSubscribe(priority = EventPriority.LOWEST)
    public void onSourceStopped(@NotNull AlStreamSourceStoppedEvent event) {
        if (closed.get() || !sourceGroup.getSources().contains(event.getSource())) return;
        reset();
    }

    private void processAudioPacket(@NotNull SourceAudioPacket packet) {
        if (sourceInfo == null || packet.getSourceState() != sourceInfo.getState()) {
            LOGGER.info("Drop packet with bad source state");
            return;
        }

        long lastSequenceNumber = lastSequenceNumbers.getOrDefault(sourceInfo.getLineId(), -1L);

        if (lastSequenceNumber >= 0 && packet.getSequenceNumber() <= lastSequenceNumber) {
            if (lastSequenceNumber - packet.getSequenceNumber() < 10L) {
                LOGGER.info("Drop packet with bad order");
                return;
            }

            lastSequenceNumbers.remove(sourceInfo.getLineId());
        }

        if (endRequest != null) {
            if (endRequest.getDelay(TimeUnit.MILLISECONDS) > 40L) {
                endRequest.cancel(false);
                this.endRequest = null;
                return;
            }
        }

        try {
            getPlayerPosition(playerPosition);
            getPosition(position);
            getLookAngle(lookAngle);
        } catch (IllegalStateException e) {
            close();
            return;
        }

        int distance = packet.getDistance();

        double volume = config.getVoice().getVolume().value() * sourceVolume.value() * lineVolume.value();

        if (config.getVoice().getSoundOcclusion().value()) {
            // todo: disable occlusion via client addon?
            double occlusion = getOccludedPercent(position);
            if (lastOcclusion >= 0) {
                if (occlusion > lastOcclusion) {
                    lastOcclusion = Math.max(lastOcclusion + 0.05, 0.0D);
                } else {
                    lastOcclusion = Math.max(lastOcclusion - 0.05, occlusion);
                }

                occlusion = lastOcclusion;
            }

            volume *= (float) (1D - occlusion);
            if (lastOcclusion == -1D) {
                lastOcclusion = occlusion;
            }
        }

        if (isStereo(sourceInfo)) {
            int sourceDistance = Math.min(getSourceDistance(position), distance);

            float distanceGain = (1F - (float) sourceDistance / (float) distance);
            volume *= distanceGain;
        }

        updateSource((float) volume, packet.getDistance());

        // packet compensation
        if (lastSequenceNumber >= 0) {
            int packetsToCompensate = (int) (packet.getSequenceNumber() - (lastSequenceNumber + 1));
            if (packetsToCompensate <= 4) {
                LOGGER.debug("Compensate {} packets", packetsToCompensate);

                for (int i = 0; i < packetsToCompensate; i++) {
                    if (decoder != null && decoder instanceof AudioDecoderPlc && !sourceInfo.isStereo()) {
                        try {
                            write(((AudioDecoderPlc) decoder).decodePLC());
                        } catch (CodecException e) {
                            LOGGER.warn("Failed to decode source audio", e);
                            return;
                        }
                    } else {
                        write(new short[0]);
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
                short[] decoded = decoder.decode(decrypted);
                if (sourceInfo.isStereo() && config.getAdvanced().getStereoSourcesToMono().value()) {
                    decoded = AudioUtil.convertToMonoShorts(decoded);
                }

                write(decoded);
            } else {
                write(decrypted);
            }
        } catch (CodecException e) {
            LOGGER.warn("Failed to decode source audio", e);
            return;
        }

        lastSequenceNumbers.put(sourceInfo.getLineId(), packet.getSequenceNumber());
        this.lastActivation = System.currentTimeMillis();
        activated.set(true);
        resetted.set(false);
    }

    private void processAudioEndPacket(@NotNull SourceAudioEndPacket packet) {
        if (!activated.get()) return;
        lastSequenceNumbers.put(sourceInfo.getLineId(), packet.getSequenceNumber());
    }

    private void reset() {
        if (!resetted.compareAndSet(false, true)) return;
        if (decoder != null) decoder.reset();
        activated.set(false);
    }

    private void updateSource(float volume, int maxDistance) {
        for (DeviceSource source : sourceGroup.getSources()) {
            if (source instanceof AlSource) {
                AlSource alSource = (AlSource) source;
                AlAudioDevice device = (AlAudioDevice) alSource.getDevice();

                if (device instanceof AlListenerDevice) {
                    ((AlListenerDevice) device).getListener().update();
                }

                device.runInContext(() -> {
                    alSource.setVolume(volume);
                    alSource.setFloatArray(0x1004, position); // AL_POSITION
                    alSource.setFloat(0x1020, 0); // AL_REFERENCE_DISTANCE
                    alSource.setFloat(0x1023, maxDistance); // AL_MAX_DISTANCE

                    if (config.getVoice().getDirectionalSources().value()) {
                        alSource.setFloatArray(0x1005, lookAngle); // AL_DIRECTION

                        alSource.setFloat(0x1022, 0F); // AL_CONE_OUTER_GAIN
                        alSource.setFloat(
                                0x1001, // AL_CONE_INNER_ANGLE
                                90
                        );

                        alSource.setFloat(
                                0x1002, // AL_CONE_OUTER_ANGLE
                                180
                        );
                    } else {
                        alSource.setFloatArray(0x1005, ZERO_VECTOR); // AL_DIRECTION
                    }
                });
            }
        }
    }

    private void write(short[] samples) {
        for (DeviceSource source : sourceGroup.getSources()) {
            samples = source.getDevice().processFilters(samples);
            source.write(AudioUtil.shortsToBytes(samples));
        }
    }

    private void write(byte[] samples) {
        write(AudioUtil.bytesToShorts(samples));
    }

    private int getSourceDistance(float[] position) {
        double xDiff = playerPosition[0] - position[0];
        double yDiff = playerPosition[1] - position[1];
        double zDiff = playerPosition[2] - position[2];

        return (int) Math.sqrt((xDiff * xDiff) + (yDiff * yDiff) + (zDiff * zDiff));
    }

    private boolean isStereo(@NotNull SourceInfo sourceInfo) {
        return sourceInfo.isStereo() && !config.getAdvanced().getStereoSourcesToMono().value();
    }

    private int getSourceAngle() {
        if (sourceInfo.getAngle() > 0) return sourceInfo.getAngle();

        return config.getAdvanced().getDirectionalSourcesAngle().value();
    }

    protected abstract double getOccludedPercent(float[] position);

    protected abstract float[] getPlayerPosition(float[] position);

    protected abstract float[] getPosition(float[] position);

    protected abstract float[] getLookAngle(float[] lookAngle);
}
