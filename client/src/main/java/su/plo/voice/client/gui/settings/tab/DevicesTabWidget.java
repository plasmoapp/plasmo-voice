package su.plo.voice.client.gui.settings.tab;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.gui.GuiUtil;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.*;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.client.event.audio.device.DeviceClosedEvent;
import su.plo.voice.api.client.event.audio.device.DeviceOpenEvent;
import su.plo.voice.api.event.EventSubscribe;
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

        addEntry(new CategoryEntry(McTextComponent.translatable("gui.plasmovoice.devices.microphone")));
        addEntry(createThresholdEntry());
        addEntry(createMicrophoneEntry());
        addEntry(createVolumeSlider(
                McTextComponent.translatable("gui.plasmovoice.devices.microphone_volume"),
                McTextComponent.translatable("gui.plasmovoice.devices.volume.tooltip"),
                config.getVoice().getMicrophoneVolume(),
                "%"
        ));
        addEntry(createToggleEntry(
                McTextComponent.translatable("gui.plasmovoice.devices.noise_suppression"),
                McTextComponent.translatable("gui.plasmovoice.devices.noise_suppression.tooltip"),
                config.getVoice().getNoiseSuppression()
        ));
        addEntry(createStereoCaptureEntry());

        addEntry(new CategoryEntry(McTextComponent.translatable("gui.plasmovoice.devices.output")));
        addEntry(createOutputDeviceEntry());
        addEntry(createVolumeSlider(
                McTextComponent.translatable("gui.plasmovoice.devices.volume"),
                McTextComponent.translatable("gui.plasmovoice.devices.volume.tooltip"),
                config.getVoice().getVolume(),
                "%"
        ));
//        addEntry(createToggleEntry(
//                "gui.plasmovoice.devices.compressor",
//                "gui.plasmovoice.devices.compressor.tooltip",
//                config.getVoice().getCompressorLimiter()
//        ));
        addEntry(createToggleEntry(
                McTextComponent.translatable("gui.plasmovoice.devices.occlusion"),
                McTextComponent.translatable("gui.plasmovoice.devices.occlusion.tooltip"),
                config.getVoice().getSoundOcclusion()
        ));
        addEntry(createToggleEntry(
                McTextComponent.translatable("gui.plasmovoice.devices.directional_sources"),
                McTextComponent.translatable("gui.plasmovoice.devices.directional_sources.tooltip"),
                config.getVoice().getDirectionalSources()
        ));
        addEntry(createHrtfEntry());
    }

    @EventSubscribe
    public void onDeviceOpen(@NotNull DeviceOpenEvent event) {
        Minecraft.getInstance().execute(this::init);
    }

    @EventSubscribe
    public void onDeviceClose(@NotNull DeviceClosedEvent event) {
        Minecraft.getInstance().execute(this::init);
    }

    private ButtonOptionEntry<ActivationThresholdWidget> createThresholdEntry() {
        if (threshold != null) voiceClient.getEventBus().unregister(voiceClient, threshold);
        this.threshold = new ActivationThresholdWidget(
                parent,
                config.getVoice().getActivationThreshold(),
                voiceClient.getAudioCapture(),
                voiceClient.getDeviceManager(),
                testController,
                0,
                0,
                ELEMENT_WIDTH - 24,
                20
        );
        voiceClient.getEventBus().register(voiceClient, threshold);

        return new ButtonOptionEntry<>(
                McTextComponent.translatable("gui.plasmovoice.devices.activation_threshold"),
                threshold,
                threshold.getButtons(),
                config.getVoice().getActivationThreshold(),
                McTextComponent.translatable("gui.plasmovoice.devices.activation_threshold.tooltip"),
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
        Optional<AudioDevice> inputDevice = Optional.empty();
        if (!config.getVoice().getDisableInputDevice().value()) {
            inputDevice = inputDevices.stream().findFirst();
        }

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

        dropdown.setActive(!inputDeviceNames.isEmpty() && !config.getVoice().getDisableInputDevice().value());

        return new OptionEntry<>(
                McTextComponent.translatable("gui.plasmovoice.devices.microphone"),
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
                McTextComponent.translatable("gui.plasmovoice.devices.stereo_capture"),
                toggleButton,
                config.getVoice().getStereoCapture(),
                McTextComponent.translatable("gui.plasmovoice.devices.stereo_capture.tooltip"),
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

        dropdown.setActive(!outputDeviceNames.isEmpty());

        return new OptionEntry<>(
                McTextComponent.translatable("gui.plasmovoice.devices.output_device"),
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
                        BaseVoice.LOGGER.warn("Failed to reload device: {}", e.getMessage());
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
                McTextComponent.translatable("gui.plasmovoice.devices.hrtf"),
                toggleButton,
                config.getVoice().getHrtf(),
                McTextComponent.translatable("gui.plasmovoice.devices.hrtf.tooltip"),
                (button, element) -> onUpdate.accept(config.getVoice().getHrtf().value())
        );
    }

    private void reloadOutputDevice() {
        try {
            devices.replace(null, DeviceType.OUTPUT, (oldDevice) -> {
                if (oldDevice != null) oldDevice.close();

                return devices.openOutputDevice(null);
            });
            testController.restart();
            Minecraft.getInstance().execute(this::init);
        } catch (Exception e) {
            BaseVoice.LOGGER.error("Failed to open primary OpenAL output device", e);
        }
    }

    private void reloadInputDevice() {
        if (config.getVoice().getDisableInputDevice().value()) return;

        try {
            devices.replace(null, DeviceType.INPUT, (oldDevice) -> {
                if (oldDevice != null) oldDevice.close();

                return devices.openInputDevice(null);
            });
            Minecraft.getInstance().execute(this::init);
        } catch (Exception e) {
            BaseVoice.LOGGER.error("Failed to open input device", e);
        }
    }
}
