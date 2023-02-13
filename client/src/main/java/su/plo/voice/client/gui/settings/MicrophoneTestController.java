package su.plo.voice.client.gui.settings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.source.LoopbackSource;
import su.plo.voice.api.client.event.audio.capture.AudioCaptureEvent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.util.AudioUtil;
import su.plo.voice.client.audio.filter.NoiseSuppressionFilter;
import su.plo.voice.client.audio.filter.StereoToMonoFilter;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.event.gui.MicrophoneTestStartedEvent;
import su.plo.voice.client.event.gui.MicrophoneTestStoppedEvent;

@RequiredArgsConstructor
public final class MicrophoneTestController {

    private static final Logger LOGGER = LogManager.getLogger();

    private final PlasmoVoiceClient voiceClient;
    private final ClientConfig config;

    private double highestDB = -127.0D;
    private long lastUpdate = 0L;

    @Getter
    private double microphoneValue = 0D;
    @Getter
    private double microphoneDB = 0D;

    private LoopbackSource source;

    public void restart() {
        if (source == null) return;
        source.close();

        try {
            initializeLoopbackSource();
        } catch (DeviceException e) {
            LOGGER.error("Failed to initialize source for mic test", e);
            this.source = null;
        }
    }

    public void start() {
        try {
            initializeLoopbackSource();
        } catch (DeviceException e) {
            LOGGER.error("Failed to initialize source for mic test", e);
            this.source = null;
            return;
        }

        voiceClient.getEventBus().call(new MicrophoneTestStartedEvent(this));
    }

    public void stop() {
        if (source != null) source.close();
        this.source = null;

        voiceClient.getEventBus().call(new MicrophoneTestStoppedEvent(this));
    }

    @EventSubscribe
    public void onAudioCapture(@NotNull AudioCaptureEvent event) {
        short[] samples = new short[event.getSamples().length];
        System.arraycopy(event.getSamples(), 0, samples, 0, event.getSamples().length);
        samples = event.getDevice().processFilters(
                samples,
                (filter) -> isStereo() &&
                        ((filter instanceof StereoToMonoFilter) || (filter instanceof NoiseSuppressionFilter))
        );

        this.microphoneDB = AudioUtil.calculateHighestAudioLevel(samples);
        if (microphoneDB > highestDB) {
            this.highestDB = microphoneDB;
            this.lastUpdate = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - lastUpdate > 1000L) {
            this.highestDB = microphoneDB;
        }
        double value = 1 - (microphoneDB / -60);

        if (microphoneDB > -60 && value > microphoneValue) {
            this.microphoneValue = AudioUtil.audioLevelToDoubleRange(microphoneDB);
        } else {
            // todo: move to "delta" tick
            this.microphoneValue = Math.max(microphoneValue - 0.02D, 0.0F);
        }

        if (source != null) {
            event.setSendEnd(true);
            source.write(samples);
        }
    }

    private void initializeLoopbackSource() throws DeviceException {
        this.source = voiceClient.getSourceManager().createLoopbackSource(true);
        try {
            source.initialize(isStereo());
        } catch (DeviceException e) {
            this.source = null;
            throw e;
        }
    }

    private boolean isStereo() {
        return config.getVoice().getStereoCapture().value() && !config.getAdvanced().getStereoSourcesToMono().value();
    }
}
