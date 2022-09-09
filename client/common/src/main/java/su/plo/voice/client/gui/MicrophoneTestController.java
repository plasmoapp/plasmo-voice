package su.plo.voice.client.gui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.AlAudioDevice;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.device.DeviceType;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.client.audio.device.source.DeviceSource;
import su.plo.voice.api.client.audio.device.source.SourceGroup;
import su.plo.voice.api.client.event.audio.capture.AudioCaptureEvent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.util.AudioUtil;
import su.plo.voice.api.util.Params;
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

    private CaptureSource source;

    public void restart() {
        if (source == null) return;
        source.close();

        this.source = new CaptureSource();
        try {
            source.initialize();
        } catch (DeviceException e) {
            LOGGER.error("Failed to initialize source for mic test", e);
            this.source = null;
        }
    }

    public void start() {
        this.source = new CaptureSource();
        try {
            source.initialize();
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
    public void onAudioCapture(AudioCaptureEvent event) {
        // todo: compressor?
//        if (VoiceClient.getClientConfig().compressor.get()) {
//            buffer = compressor.compress(buffer);
//        }

        short[] samples = event.getSamples();

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

        if (source != null) source.write(samples);
//        if (source != null) {
//            byte[] finalBuffer = buffer;
//            VoiceClient.getSoundEngine().runInContext(() -> {
//                source.setVolume(VoiceClient.getClientConfig().voiceVolume.get().floatValue());
//                source.write(finalBuffer);
//            });
//        }
    }

    private class CaptureSource {

        private SourceGroup sourceGroup;

        public void initialize() throws DeviceException {
            this.sourceGroup = voiceClient.getDeviceManager().createSourceGroup(DeviceType.OUTPUT);
            sourceGroup.create(false, Params.EMPTY);

            for (DeviceSource source : sourceGroup.getSources()) {
                if (source instanceof AlSource) {
                    AlSource alSource = (AlSource) source;
                    AlAudioDevice device = (AlAudioDevice) alSource.getDevice();

                    device.runInContext(() -> {
                        alSource.setFloat(0x100E, 4F); // AL_MAX_GAIN
                        alSource.setInt(0x202, 1); // AL_SOURCE_RELATIVE

                        alSource.play();
                    });
                }
            }
        }

        public void close() {
            if (sourceGroup != null) sourceGroup.getSources().forEach(DeviceSource::close);
        }

        public void write(short[] samples) {
            setVolume(config.getVoice().getVolume().value().floatValue());

            write(AudioUtil.shortsToBytes(samples));
        }

        private void setVolume(float volume) {
            for (DeviceSource source : sourceGroup.getSources()) {
                if (source instanceof AlSource) {
                    AlSource alSource = (AlSource) source;
                    AlAudioDevice device = (AlAudioDevice) alSource.getDevice();

                    device.runInContext(() -> alSource.setVolume(volume));
                }
            }
        }

        private void write(byte[] samples) {
            for (DeviceSource source : sourceGroup.getSources()) {
                source.write(samples);
            }
        }
    }
}
