package su.plo.voice;

import su.plo.voice.api.audio.device.DeviceFactoryManager;
import su.plo.voice.audio.device.AlInputDeviceFactory;
import su.plo.voice.audio.device.AlOutputDeviceFactory;
import su.plo.voice.audio.device.JavaxInputDeviceFactory;

public abstract class VoiceClientMod extends VoiceClientBase {

    protected String modId = "plasmovoice";

    protected VoiceClientMod() {
        DeviceFactoryManager factoryManager = getDeviceFactoryManager();

        // OpenAL in&out
        factoryManager.registerDeviceFactory(new AlOutputDeviceFactory(this));
        factoryManager.registerDeviceFactory(new AlInputDeviceFactory(this));

        // JavaX input
        factoryManager.registerDeviceFactory(new JavaxInputDeviceFactory(this));
    }
}
