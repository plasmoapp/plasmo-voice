package su.plo.voice.client.audio.device;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.ConfigEntry;
import su.plo.slib.api.logging.McLogger;
import su.plo.slib.api.logging.McLoggerFactory;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.*;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.client.audio.filter.GainFilter;
import su.plo.voice.client.audio.filter.NoiseSuppressionFilter;
import su.plo.voice.client.audio.filter.StereoToMonoFilter;
import su.plo.voice.client.config.VoiceClientConfig;

import javax.sound.sampled.AudioFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public final class VoiceDeviceManager implements DeviceManager {

    private static final McLogger LOGGER = McLoggerFactory.createLogger("VoiceDeviceManager");

    private final PlasmoVoiceClient voiceClient;
    private final VoiceClientConfig config;

    private @Nullable AlContextOutputDevice outputDevice = null;
    private @Nullable InputDevice inputDevice = null;

    private ScheduledFuture<?> job;

    private String failedOutputDevices = "";
    private String failedInputDevices = "";

    @Override
    public @NotNull Optional<AlContextOutputDevice> getOutputDevice() {
        return Optional.ofNullable(outputDevice);
    }

    @Override
    public void setOutputDevice(@Nullable AlContextOutputDevice device) {
        if (outputDevice != null) {
            voiceClient.getEventBus().unregister(voiceClient, outputDevice);
        }

        this.outputDevice = device;

        if (device != null) {
            voiceClient.getEventBus().register(voiceClient, device);
        }
    }

    @Override
    public @NotNull Optional<InputDevice> getInputDevice() {
        return Optional.ofNullable(inputDevice);
    }

    @Override
    public void setInputDevice(@Nullable InputDevice device) {
        if (inputDevice != null) {
            voiceClient.getEventBus().unregister(voiceClient, inputDevice);
        }

        this.inputDevice = device;

        if (device != null) {
            voiceClient.getEventBus().register(voiceClient, device);
        }
    }

    @Override
    public @NotNull InputDevice openInputDevice(@Nullable AudioFormat format) throws DeviceException {
        // Use javax for mac by default
        if (Minecraft.ON_OSX && !config.getVoice().getUseJavaxInput().value()) {
            config.getVoice().getUseJavaxInput().set(true);
            config.save(true);
        }

        // todo: javax stereo
        if (config.getVoice().getUseJavaxInput().value() && config.getVoice().getStereoCapture().value()) {
            config.getVoice().getStereoCapture().set(false);
            config.getVoice().getStereoCapture().setDisabled(true);
            config.save(true);
        }

        if (format == null) {
            if (!voiceClient.getServerInfo().isPresent()) throw new IllegalStateException("Not connected");

            ServerInfo serverInfo = voiceClient.getServerInfo().get();
            format = serverInfo.getVoiceInfo().createFormat(
                    config.getVoice().getStereoCapture().value()
            );
        }

        InputDevice device;
        if (config.getVoice().getUseJavaxInput().value()) {
            device = openJavaxInputDevice(format);
        } else {
            try {
                device = openAlInputDevice(format);
            } catch (Exception e) {
                BaseVoice.LOGGER.error("Failed to open OpenAL input device, falling back to Javax input device", e);

                device = openJavaxInputDevice(format);
            }
        }

        // apply default filters
        device.addFilter(new StereoToMonoFilter(config.getVoice().getStereoCapture()));
        device.addFilter(new GainFilter(config.getVoice().getMicrophoneVolume()));
        device.addFilter(new NoiseSuppressionFilter((int) format.getSampleRate(), config.getVoice().getNoiseSuppression()));

        return device;
    }

    @Override
    public @NotNull AlContextOutputDevice openOutputDevice(@Nullable AudioFormat format) throws DeviceException {
        DeviceFactory deviceFactory = voiceClient.getDeviceFactoryManager().getDeviceFactory("AL_OUTPUT")
                .orElseThrow(() -> new DeviceException("OpenAL output device factory is not initialized"));

        if (format == null) {
            if (!voiceClient.getServerInfo().isPresent()) throw new IllegalStateException("Not connected");

            ServerInfo serverInfo = voiceClient.getServerInfo().get();
            format = serverInfo.getVoiceInfo().createFormat(false);
        }

        String deviceName = getDeviceName(deviceFactory, config.getVoice().getOutputDevice());

//        device.addFilter(new CompressorFilter(
//                (int) format.getSampleRate(),
//                config.getVoice().getCompressorLimiter(),
//                config.getAdvanced().getCompressorThreshold()
//        ));
//        device.addFilter(new LimiterFilter(
//                (int) format.getSampleRate(),
//                config.getVoice().getCompressorLimiter(),
//                config.getAdvanced().getLimiterThreshold()
//        ));

        return (AlContextOutputDevice) deviceFactory.openDevice(
                format,
                deviceName
        );
    }

    public void startJob() {
        this.job = voiceClient.getBackgroundExecutor().scheduleAtFixedRate(
                () -> {
                    try {
                        tickJob();
                    } catch (DeviceException e) {
                        e.printStackTrace();
                    }
                },
                0L,
                1L,
                TimeUnit.SECONDS
        );
    }

    public void stopJob() {
        if (job != null) job.cancel(false);
    }

    private synchronized void tickJob() throws DeviceException {
        DeviceFactory outputFactory = voiceClient.getDeviceFactoryManager().getDeviceFactory("AL_OUTPUT")
                .orElseThrow(() -> new DeviceException("OpenAL output device factory is not registered"));

        String inputFactoryName = config.getVoice().getUseJavaxInput().value()
                ? "JAVAX_INPUT"
                : "AL_INPUT";
        DeviceFactory inputFactory = voiceClient
                .getDeviceFactoryManager()
                .getDeviceFactory(inputFactoryName)
                .orElseThrow(() -> new IllegalStateException("OpenAL input factory is not registered"));;

        if (outputDevice == null) {
            List<String> deviceNames = outputFactory.getDeviceNames();
            String deviceNamesString = String.join("\n", deviceNames);

            if (deviceNames.size() > 0 && !deviceNamesString.equals(failedOutputDevices)) {
                try {
                    setOutputDevice(openOutputDevice(null));
                    failedOutputDevices = "";
                } catch (Exception e) {
                    LOGGER.error("Failed to open primary OpenAL output device", e);
                    failedOutputDevices = deviceNamesString;
                }

                if (!voiceClient.getAudioCapture().isActive() && inputDevice != null) {
                    voiceClient.getAudioCapture().start();
                }

                voiceClient.getSourceManager().clear();
            }
        } else if (!outputDevice.isOpen()) {
            outputDevice.close();
            setOutputDevice(null);
        }

        if (inputDevice == null) {
            List<String> deviceNames = inputFactory.getDeviceNames();
            String deviceNamesString = String.join("\n", deviceNames);

            if (deviceNames.size() > 0 && !deviceNamesString.equals(failedInputDevices) && !config.getVoice().getDisableInputDevice().value()) {
                try {
                    setInputDevice(openInputDevice(null));
                    failedInputDevices = "";
                } catch (Exception e) {
                    LOGGER.error("Failed to open input device", e);
                    failedInputDevices = deviceNamesString;
                }

                if (!voiceClient.getAudioCapture().isActive()) {
                    voiceClient.getAudioCapture().start();
                }
            }
        } else if (!inputDevice.isOpen()) {
            inputDevice.close();
            setInputDevice(null);
        }
    }

    private InputDevice openAlInputDevice(@NotNull AudioFormat format) throws Exception {
        DeviceFactory deviceFactory = voiceClient.getDeviceFactoryManager().getDeviceFactory("AL_INPUT")
                .orElseThrow(() -> new IllegalStateException("OpenAL input factory is not registered"));

        String deviceName = getDeviceName(deviceFactory, config.getVoice().getInputDevice());

        return (InputDevice) deviceFactory.openDevice(
                format,
                deviceName
        );
    }

    private InputDevice openJavaxInputDevice(@NotNull AudioFormat format) throws DeviceException {
        DeviceFactory deviceFactory = voiceClient.getDeviceFactoryManager().getDeviceFactory("JAVAX_INPUT")
                .orElseThrow(() -> new IllegalStateException("Javax input factory is not registered"));

        String deviceName = getDeviceName(deviceFactory, config.getVoice().getInputDevice());

        return (InputDevice) deviceFactory.openDevice(
                format,
                deviceName
        );
    }

    private @NotNull String getDeviceName(DeviceFactory deviceFactory, ConfigEntry<String> configEntry) {
        String deviceName = configEntry.value();
        if (!Strings.isNullOrEmpty(deviceName) && !deviceFactory.getDeviceNames().contains(deviceName)) {
            deviceName = deviceFactory.getDefaultDeviceName();
            configEntry.set("");
        } else if (Strings.isNullOrEmpty(deviceName)) {
            deviceName = deviceFactory.getDefaultDeviceName();
        }

        return deviceName;
    }
}
