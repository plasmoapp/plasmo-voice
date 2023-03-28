package su.plo.voice.client.gui.settings.tab;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.mod.client.gui.GuiUtil;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.*;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.util.Params;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.gui.settings.MicrophoneTestController;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.gui.settings.widget.ActivationThresholdWidget;
import su.plo.voice.client.gui.settings.widget.DropDownWidget;
import su.plo.voice.client.gui.settings.widget.ToggleButton;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public final class DevicesTabWidget extends TabWidget {

    private static final Logger LOGGER = LogManager.getLogger();

    private final MicrophoneTestController testController;
    private final DeviceManager devices;
    private final DeviceFactoryManager deviceFactories;

    private ActivationThresholdWidget threshold;

    public DevicesTabWidget(VoiceSettingsScreen parent,
                            PlasmoVoiceClient voiceClient,
                            VoiceClientConfig config,
                            MicrophoneTestController testController) {
        super(parent, voiceClient, config);

        this.testController = testController;
        this.devices = voiceClient.getDeviceManager();
        this.deviceFactories = voiceClient.getDeviceFactoryManager();
    }

    @Override
    public void init() {
        super.init();

        addEntry(new CategoryEntry(MinecraftTextComponent.translatable("gui.plasmovoice.devices.microphone")));
        addEntry(createThresholdEntry());
        addEntry(createMicrophoneEntry());
        addEntry(createVolumeSlider(
                MinecraftTextComponent.translatable("gui.plasmovoice.devices.microphone_volume"),
                MinecraftTextComponent.translatable("gui.plasmovoice.devices.volume.tooltip"),
                config.getVoice().getMicrophoneVolume(),
                "%"
        ));
        addEntry(createToggleEntry(
                MinecraftTextComponent.translatable("gui.plasmovoice.devices.noise_suppression"),
                MinecraftTextComponent.translatable("gui.plasmovoice.devices.noise_suppression.tooltip"),
                config.getVoice().getNoiseSuppression()
        ));
        addEntry(createStereoCaptureEntry());

        addEntry(new CategoryEntry(MinecraftTextComponent.translatable("gui.plasmovoice.devices.output")));
        addEntry(createOutputDeviceEntry());
        addEntry(createVolumeSlider(
                MinecraftTextComponent.translatable("gui.plasmovoice.devices.volume"),
                MinecraftTextComponent.translatable("gui.plasmovoice.devices.volume.tooltip"),
                config.getVoice().getVolume(),
                "%"
        ));
//        addEntry(createToggleEntry(
//                "gui.plasmovoice.devices.compressor",
//                "gui.plasmovoice.devices.compressor.tooltip",
//                config.getVoice().getCompressorLimiter()
//        ));
        addEntry(createToggleEntry(
                MinecraftTextComponent.translatable("gui.plasmovoice.devices.occlusion"),
                MinecraftTextComponent.translatable("gui.plasmovoice.devices.occlusion.tooltip"),
                config.getVoice().getSoundOcclusion()
        ));
        addEntry(createToggleEntry(
                MinecraftTextComponent.translatable("gui.plasmovoice.devices.directional_sources"),
                MinecraftTextComponent.translatable("gui.plasmovoice.devices.directional_sources.tooltip"),
                config.getVoice().getDirectionalSources()
        ));
        addEntry(createHrtfEntry());
    }

    private ButtonOptionEntry<ActivationThresholdWidget> createThresholdEntry() {
        if (threshold != null) voiceClient.getEventBus().unregister(voiceClient, threshold);
        this.threshold = new ActivationThresholdWidget(
                parent,
                config.getVoice().getActivationThreshold(),
                devices,
                testController,
                0,
                0,
                ELEMENT_WIDTH - 24,
                20
        );
        voiceClient.getEventBus().register(voiceClient, threshold);

        return new ButtonOptionEntry<>(
                MinecraftTextComponent.translatable("gui.plasmovoice.devices.activation_threshold"),
                threshold,
                threshold.getButtons(),
                config.getVoice().getActivationThreshold(),
                MinecraftTextComponent.translatable("gui.plasmovoice.devices.activation_threshold.tooltip"),
                null
        );
    }

    private OptionEntry<DropDownWidget> createMicrophoneEntry() {
        Optional<DeviceFactory> deviceFactory;

        if (config.getVoice().getUseJavaxInput().value()) {
            deviceFactory = deviceFactories.getDeviceFactory("JAVAX_INPUT");
            if (!deviceFactory.isPresent())
                throw new IllegalStateException("Javax Input device factory not initialized");
        } else {
            deviceFactory = deviceFactories.getDeviceFactory("AL_INPUT");
            if (!deviceFactory.isPresent()) throw new IllegalStateException("Al Input device factory not initialized");
        }

        ImmutableList<String> inputDeviceNames = deviceFactory.get().getDeviceNames();
        Collection<AudioDevice> inputDevices = this.devices.getDevices(DeviceType.INPUT);
        Optional<AudioDevice> inputDevice = inputDevices.stream().findFirst();

        DropDownWidget dropdown = new DropDownWidget(
                parent,
                0,
                0,
                ELEMENT_WIDTH,
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
                MinecraftTextComponent.translatable("gui.plasmovoice.devices.microphone"),
                dropdown,
                config.getVoice().getInputDevice(),
                (button, element) -> {
                    element.setText(GuiUtil.formatDeviceName((String) null, deviceFactory.get()));
                    reloadInputDevice();
                }
        );
    }

    private OptionEntry<ToggleButton> createStereoCaptureEntry() {
        Runnable onUpdate = () -> {
            reloadInputDevice();
            testController.restart();
        };

        ToggleButton toggleButton = new ToggleButton(
                config.getVoice().getStereoCapture(),
                0,
                0,
                ELEMENT_WIDTH,
                20,
                (toggled) -> onUpdate.run()
        );

        return new OptionEntry<>(
                MinecraftTextComponent.translatable("gui.plasmovoice.devices.stereo_capture"),
                toggleButton,
                config.getVoice().getStereoCapture(),
                MinecraftTextComponent.translatable("gui.plasmovoice.devices.stereo_capture.tooltip"),
                (button, element) -> onUpdate.run()
        );
    }

    private OptionEntry<DropDownWidget> createOutputDeviceEntry() {
        Optional<DeviceFactory> deviceFactory = deviceFactories.getDeviceFactory("AL_OUTPUT");
        if (!deviceFactory.isPresent()) throw new IllegalStateException("Al Output device factory not initialized");

        ImmutableList<String> outputDeviceNames = deviceFactory.get().getDeviceNames();
        Collection<AudioDevice> outputDevices = this.devices.getDevices(DeviceType.OUTPUT);
        Optional<AudioDevice> outputDevice = outputDevices.stream().findFirst();

        DropDownWidget dropdown = new DropDownWidget(
                parent,
                0,
                0,
                ELEMENT_WIDTH,
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
                MinecraftTextComponent.translatable("gui.plasmovoice.devices.output_device"),
                dropdown,
                config.getVoice().getOutputDevice(),
                (button, element) -> {
                    element.setText(GuiUtil.formatDeviceName((String) null, deviceFactory.get()));
                    reloadOutputDevice();
                }
        );
    }

    private OptionEntry<ToggleButton> createHrtfEntry() {
        Consumer<Boolean> onUpdate = (toggled) -> {
            devices.<OutputDevice<?>>getDevices(DeviceType.OUTPUT).forEach(device -> {
                if (device instanceof HrtfAudioDevice) {
                    try {
                        device.reload();
                    } catch (DeviceException e) {
                        LogManager.getLogger().warn("Failed to reload device: {}", e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        };

        ToggleButton toggleButton = new ToggleButton(
                config.getVoice().getHrtf(),
                0,
                0,
                ELEMENT_WIDTH,
                20,
                onUpdate::accept
        );

        return new OptionEntry<>(
                MinecraftTextComponent.translatable("gui.plasmovoice.devices.hrtf"),
                toggleButton,
                config.getVoice().getHrtf(),
                MinecraftTextComponent.translatable("gui.plasmovoice.devices.hrtf.tooltip"),
                (button, element) -> onUpdate.accept(config.getVoice().getHrtf().value())
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
