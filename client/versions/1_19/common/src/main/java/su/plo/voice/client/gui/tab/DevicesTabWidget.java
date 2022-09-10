package su.plo.voice.client.gui.tab;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.*;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.util.Params;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.GuiUtil;
import su.plo.voice.client.gui.MicrophoneTestController;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.gui.widget.ActivationThresholdWidget;
import su.plo.voice.client.gui.widget.DropDownWidget;
import su.plo.voice.client.gui.widget.ToggleButton;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public final class DevicesTabWidget extends TabWidget {

    private static final Logger LOGGER = LogManager.getLogger();

    private final PlasmoVoiceClient voiceClient;
    private final MicrophoneTestController testController;
    private final ClientConfig config;
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
        this.devices = voiceClient.getDeviceManager();
        this.deviceFactories = voiceClient.getDeviceFactoryManager();
    }

    @Override
    public void init() {
        super.init();

        addEntry(new CategoryEntry(Component.translatable("gui.plasmovoice.devices.microphone")));
        addEntry(createThresholdEntry());
        addEntry(createMicrophoneEntry());
        addEntry(createDoubleSliderWidget(
                "gui.plasmovoice.devices.microphone_volume",
                "gui.plasmovoice.devices.volume.tooltip",
                config.getVoice().getMicrophoneVolume(),
                "%"
        ));
        addEntry(createToggleEntry(
                "gui.plasmovoice.devices.noise_suppression",
                "gui.plasmovoice.devices.noise_suppression.tooltip",
                config.getVoice().getNoiseSuppression()
        ));
        addEntry(createStereoCaptureEntry());

        addEntry(new CategoryEntry(Component.translatable("gui.plasmovoice.devices.output")));
        addEntry(createOutputDeviceEntry());
        addEntry(createDoubleSliderWidget(
                "gui.plasmovoice.devices.volume",
                "gui.plasmovoice.devices.volume.tooltip",
                config.getVoice().getVolume(),
                "%"
        ));
        addEntry(createToggleEntry(
                "gui.plasmovoice.devices.compressor",
                "gui.plasmovoice.devices.compressor.tooltip",
                config.getVoice().getCompressorLimiter()
        ));
        addEntry(createToggleEntry(
                "gui.plasmovoice.devices.occlusion",
                "gui.plasmovoice.devices.occlusion.tooltip",
                config.getVoice().getSoundOcclusion()
        ));
        addEntry(createToggleEntry(
                "gui.plasmovoice.devices.directional_sources",
                "gui.plasmovoice.devices.directional_sources.tooltip",
                config.getVoice().getDirectionalSources()
        ));
        addEntry(createHrtfEntry());

        addEntry(new CategoryEntry(Component.literal("хуй")));
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
                Component.translatable("gui.plasmovoice.devices.stereo_capture"),
                toggleButton,
                config.getVoice().getStereoCapture(),
                GuiUtil.multiLineTooltip("gui.plasmovoice.devices.stereo_capture.tooltip")
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
                }
        );
    }

    private OptionEntry<DropDownWidget> createOutputDeviceEntry() {
        Optional<DeviceFactory> deviceFactory = deviceFactories.getDeviceFactory("AL_OUTPUT");
        if (deviceFactory.isEmpty()) throw new IllegalStateException("Al Output device factory not initialized");

        ImmutableList<String> outputDeviceNames = deviceFactory.get().getDeviceNames();
        Collection<AudioDevice> outputDevices = this.devices.getDevices(DeviceType.OUTPUT);
        Optional<AudioDevice> outputDevice = outputDevices.stream().findFirst();

        DropDownWidget dropdown = new DropDownWidget(
                parent,
                0,
                0,
                97,
                20,
                GuiUtil.formatDeviceName(outputDevice.orElse(null), deviceFactory.get()),
                GuiUtil.formatDeviceNames(outputDeviceNames, deviceFactory.get()),
                true,
                (index) -> {
                    String deviceName = outputDeviceNames.get(index);
                    if (Objects.equals(deviceName, deviceFactory.get().getDefaultDeviceName())) {
                        deviceName = null;
                    }

                    config.getVoice().getOutputDevice().set(Strings.nullToEmpty(deviceName));
                    config.save(true);

                    reloadOutputDevice();
                }
        );

        return new OptionEntry<>(
                Component.translatable("gui.plasmovoice.devices.output_device"),
                dropdown,
                config.getVoice().getOutputDevice(),
                (button, element) -> {
                    element.setMessage(GuiUtil.formatDeviceName((String) null, deviceFactory.get()));
                    reloadOutputDevice();
                }
        );
    }

    private OptionEntry<ToggleButton> createHrtfEntry() {
        ToggleButton toggleButton = new ToggleButton(
                0,
                0,
                97,
                20,
                config.getVoice().getHrtf(),
                (toggled) -> {
                    devices.<OutputDevice<?>>getDevices(DeviceType.OUTPUT).forEach(device -> {
                        if (device instanceof HrtfAudioDevice hrtfAudioDevice) {
                            if (toggled) hrtfAudioDevice.enableHrtf();
                            else hrtfAudioDevice.disableHrtf();
                        }
                    });
                }
        );

        return new OptionEntry<>(
                Component.translatable("gui.plasmovoice.devices.hrtf"),
                toggleButton,
                config.getVoice().getHrtf(),
                GuiUtil.multiLineTooltip("gui.plasmovoice.devices.hrtf.tooltip")
        );
    }

    private void reloadOutputDevice() {

        try {
            OutputDevice<AlSource> outputDevice = devices.openOutputDevice(null, Params.EMPTY);

            voiceClient.getDeviceManager().replace(null, outputDevice);
            testController.restart();
        } catch (Exception e) {
            LOGGER.error("Failed to open primary OpenAL output device", e);
        }
    }

    private void reloadInputDevice() {
        try {
            InputDevice device = devices.openInputDevice(null, Params.EMPTY);

            devices.replace(null, device);
        } catch (Exception e) {
            LOGGER.error("Failed to open input device", e);
        }
    }
}
