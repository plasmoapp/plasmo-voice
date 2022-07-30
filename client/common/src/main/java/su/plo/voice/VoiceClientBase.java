package su.plo.voice;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import su.plo.voice.api.PlasmoVoiceClient;
import su.plo.voice.api.audio.device.DeviceFactoryManager;
import su.plo.voice.api.audio.device.DeviceManager;
import su.plo.voice.api.event.VoiceClientInitializedEvent;
import su.plo.voice.api.event.VoiceClientShutdownEvent;
import su.plo.voice.audio.device.VoiceDeviceFactoryManager;
import su.plo.voice.audio.device.VoiceDeviceManager;

public abstract class VoiceClientBase extends VoiceBase implements PlasmoVoiceClient {

    protected final Logger logger = LogManager.getLogger(PlasmoVoiceClient.class);

    @Getter
    private final DeviceFactoryManager deviceFactoryManager = new VoiceDeviceFactoryManager();
    @Getter
    private final DeviceManager deviceManager = new VoiceDeviceManager();

    protected void onInitialize() {

        getEventBus().call(new VoiceClientInitializedEvent(this));
    }

    protected void onShutdown() {
        logger.info("Shutting down");

        getEventBus().call(new VoiceClientShutdownEvent(this));
    }
}
