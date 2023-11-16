package su.plo.voice.client.gui.settings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.source.LoopbackSource;
import su.plo.voice.api.client.event.audio.capture.AudioCaptureEvent;
import su.plo.voice.api.client.event.audio.capture.AudioCaptureProcessedEvent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.util.AudioUtil;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.event.gui.MicrophoneTestStartedEvent;
import su.plo.voice.client.event.gui.MicrophoneTestStoppedEvent;

@RequiredArgsConstructor
public final class MicrophoneTestController {

    private final PlasmoVoiceClient voiceClient;
    private final VoiceClientConfig config;

    private double highestDB = -127.0D;
    private long lastUpdate = 0L;

    @Getter
    private double microphoneValue = 0D;
    @Getter
    private double microphoneDB = 0D;

    private LoopbackSource source;

    public void tick(double delta) {
        this.microphoneValue = Math.max(microphoneValue - (0.04D * delta), 0.0D);
    }

    public void restart() {
        if (source == null) return;
        source.close();

        try {
            initializeLoopbackSource();
        } catch (DeviceException e) {
            BaseVoice.LOGGER.error("Failed to initialize source for mic test", e);
            this.source = null;
        }
    }

    public void start() {
        if (source != null) return;

        try {
            initializeLoopbackSource();
        } catch (DeviceException e) {
            BaseVoice.LOGGER.error("Failed to initialize source for mic test", e);
            this.source = null;
            return;
        }

        voiceClient.getEventBus().fire(new MicrophoneTestStartedEvent(this));
    }

    public void stop() {
        if (source != null) source.close();
        this.source = null;

        voiceClient.getEventBus().fire(new MicrophoneTestStoppedEvent(this));
    }

    public boolean isActive() {
        return source != null;
    }

    @EventSubscribe
    public void onAudioCapture(@NotNull AudioCaptureEvent event) {
        if (source != null) {
            event.setFlushActivations(true);
        }
    }

    @EventSubscribe
    public void onAudioCaptureProcessed(@NotNull AudioCaptureProcessedEvent event) {
        short[] samples = event.getProcessed().getSamples(isStereoSupported());

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
        }

        if (source != null) {
            source.write(samples);
        }
    }

    private void initializeLoopbackSource() throws DeviceException {
        this.source = voiceClient.getSourceManager().createLoopbackSource(true);
        try {
            source.initialize(isStereoSupported());
        } catch (DeviceException e) {
            this.source = null;
            throw e;
        }
    }

    private boolean isStereoSupported() {
        return config.getVoice().getStereoCapture().value() && !config.getAdvanced().getStereoSourcesToMono().value();
    }
}
