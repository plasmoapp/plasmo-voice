package su.plo.voice.client.gui.tab;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.AudioCapture;
import su.plo.voice.api.client.audio.device.*;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.GuiUtil;
import su.plo.voice.client.gui.MicrophoneTestController;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.gui.widget.ActivationThresholdWidget;
import su.plo.voice.client.gui.widget.DropDownWidget;
import su.plo.voice.client.gui.widget.SliderWidget;
import su.plo.voice.client.gui.widget.ToggleButton;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public final class DevicesTabWidget extends TabWidget {

    private static final Logger LOGGER = LogManager.getLogger();

    private final PlasmoVoiceClient voiceClient;
    private final MicrophoneTestController testController;
    private final ClientConfig config;
    private final AudioCapture capture;
    private final DeviceManager devices;
    private final DeviceFactoryManager deviceFactories;

    private ActivationThresholdWidget threshold;

    public DevicesTabWidget(Minecraft minecraft,
                            VoiceSettingsScreen parent,
                            MicrophoneTestController testController,
                            PlasmoVoiceClient voiceClient,
                            ClientConfig config) {
        super(minecraft, parent);

        this.testController = testController;
        this.voiceClient = voiceClient;
        this.config = config;
        this.capture = voiceClient.getAudioCapture();
        this.devices = voiceClient.getDeviceManager();
        this.deviceFactories = voiceClient.getDeviceFactoryManager();
    }

    @Override
    public void init() {
        super.init();

        addEntry(new CategoryEntry(Component.translatable("gui.plasmovoice.devices.microphone")));
        addEntry(createThresholdEntry());
        addEntry(createMicrophoneEntry());
        addEntry(createMicrophoneVolumeEntry());
        addEntry(createToggleEntry(
                "gui.plasmovoice.devices.noise_suppression",
                config.getVoice().getNoiseSuppression()
        ));

        addEntry(new CategoryEntry(Component.literal("хуй")));
        addEntry(createStereoCaptureEntry());
//        addEntry(createStereoToMonoSources());

    }

    @Override
    public void removed() {
        super.removed();
    }

    private OptionEntry<ToggleButton> createStereoCaptureEntry() {
        ToggleButton toggleButton = new ToggleButton(
                0,
                0,
                97,
                20,
                config.getVoice().getStereoCapture(),
                (toggled) -> {
                    reloadInputDevice();
                    testController.restart();
                }
        );

        return new OptionEntry<>(
                Component.literal("Stereo Capture"),
                toggleButton,
                config.getVoice().getStereoCapture()
        );
    }

    private OptionEntry<ToggleButton> createStereoToMonoSources() {
        ToggleButton toggleButton = new ToggleButton(
                0,
                0,
                97,
                20,
                config.getVoice().getStereoToMonoSources()
        );

        return new OptionEntry<>(
                Component.literal("Stereo To Mono"),
                toggleButton,
                config.getVoice().getStereoToMonoSources()
        );
    }

    private OptionEntry<ActivationThresholdWidget> createThresholdEntry() {
        if (threshold != null) voiceClient.getEventBus().unregister(voiceClient, threshold);
        this.threshold = new ActivationThresholdWidget(
                minecraft,
                parent,
                testController,
                config,
                devices,
                0,
                0,
                97,
                true
        );
        voiceClient.getEventBus().register(voiceClient, threshold);

        return new OptionEntry<>(
                Component.translatable("gui.plasmovoice.devices.activation_threshold"),
                threshold,
                config.getVoice().getActivationThreshold(),
                GuiUtil.multiLineTooltip("gui.plasmovoice.devices.activation_threshold.tooltip")
        );
    }

    private OptionEntry<DropDownWidget> createMicrophoneEntry() {
        Optional<DeviceFactory> deviceFactory;

        if (config.getVoice().getUseJavaxInput().value()) {
            deviceFactory = deviceFactories.getDeviceFactory("JAVAX_INPUT");
            if (deviceFactory.isEmpty()) throw new IllegalStateException("Javax Input device factory not initialized");
        } else {
            deviceFactory = deviceFactories.getDeviceFactory("AL_INPUT");
            if (deviceFactory.isEmpty()) throw new IllegalStateException("Al Input device factory not initialized");
        }

        ImmutableList<String> inputDeviceNames = deviceFactory.get().getDeviceNames();
        Collection<AudioDevice> inputDevices = this.devices.getDevices(DeviceType.INPUT);
        Optional<AudioDevice> inputDevice = inputDevices.stream().findFirst();

        DropDownWidget dropdown = new DropDownWidget(
                parent,
                0,
                0,
                97,
                20,
                GuiUtil.formatDeviceName(inputDevice.orElse(null), deviceFactory.get()),
                GuiUtil.formatDeviceNames(inputDeviceNames, deviceFactory.get()),
                true,
                (index) -> {
                    String deviceName = inputDeviceNames.get(index);
                    if (Objects.equals(deviceName, deviceFactory.get().getDefaultDeviceName())) {
                        deviceName = null;
                    }

                    config.getVoice().getInputDevice().set(Strings.nullToEmpty(deviceName));
                    config.save(true);

                    reloadInputDevice();
                }
        );

        return new OptionEntry<>(
                Component.translatable("gui.plasmovoice.devices.microphone"),
                dropdown,
                config.getVoice().getInputDevice(),
                (button, element) -> {
                    element.setMessage(GuiUtil.formatDeviceName((String) null, deviceFactory.get()));
                    reloadInputDevice();
                });
    }

    private OptionEntry<SliderWidget> createMicrophoneVolumeEntry() {
        SliderWidget volumeSlider = new SliderWidget(
                0,
                0,
                97,
                config.getVoice().getMicrophoneVolume()
        );

        return new OptionEntry<>(
                Component.translatable("gui.plasmovoice.devices.microphone_volume"),
                volumeSlider,
                config.getVoice().getMicrophoneVolume()
        );
    }

    private void reloadInputDevice() {
        try {
            InputDevice device = capture.openInputDevice(null);

            devices.replace(null, device);
        } catch (Exception e) {
            LOGGER.error("Failed to open input device", e);
        }
    }
}
