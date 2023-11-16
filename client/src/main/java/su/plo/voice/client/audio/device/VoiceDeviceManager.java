package su.plo.voice.client.audio.device;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import su.plo.config.entry.ConfigEntry;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.*;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.client.audio.device.source.SourceGroup;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.client.audio.device.source.VoiceOutputSourceGroup;
import su.plo.voice.client.audio.filter.GainFilter;
import su.plo.voice.client.audio.filter.NoiseSuppressionFilter;
import su.plo.voice.client.audio.filter.StereoToMonoFilter;
import su.plo.voice.client.config.VoiceClientConfig;

import javax.sound.sampled.AudioFormat;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static su.plo.voice.universal.UDesktop.isMac;

@RequiredArgsConstructor
public final class VoiceDeviceManager implements DeviceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoiceDeviceManager.class);

    private final PlasmoVoiceClient voiceClient;
    private final VoiceClientConfig config;

    private final List<AudioDevice> inputDevices = new CopyOnWriteArrayList<>();
    private final List<AudioDevice> outputDevices = new CopyOnWriteArrayList<>();

    private ScheduledFuture<?> job;

    @Override
    public void add(@NotNull AudioDevice device) {
        checkNotNull(device, "device cannot be null");

        List<AudioDevice> devices = getDevicesList(device);

        if (devices == inputDevices && devices.size() > 0) {
            throw new IllegalStateException("Multiple input devices currently are not supported. Use DeviceManager::replace to replace the current input device");
        }

        if (devices.contains(device)) return;

        voiceClient.getEventBus().register(voiceClient, device);
        devices.add(device);
    }

    @Override
    public void replace(@Nullable AudioDevice oldDevice, @NotNull AudioDevice newDevice) {
        checkNotNull(newDevice, "newDevice cannot be null");

        List<AudioDevice> devices = getDevicesList(newDevice);

        if (oldDevice != null) {
            if (devices != getDevicesList(oldDevice)) {
                throw new IllegalArgumentException("Devices are not implementing the same interface");
            }

            int index = devices.indexOf(oldDevice);
            if (index < 0) {
                throw new IllegalArgumentException("oldDevice not found in device list");
            }

            devices.set(index, newDevice);
        } else {
            if (devices.size() > 0) {
                oldDevice = devices.get(0);

                devices.set(0, newDevice);

                oldDevice.close();
            } else devices.add(newDevice);
        }

        if (oldDevice != null) voiceClient.getEventBus().unregister(voiceClient, oldDevice);
        voiceClient.getEventBus().register(voiceClient, newDevice);
    }

    @Override
    public void remove(@NotNull AudioDevice device) {
        checkNotNull(device, "device cannot be null");
        getDevicesList(device).remove(device);
        voiceClient.getEventBus().unregister(voiceClient, device);
    }

    @Override
    public void clear(@Nullable DeviceType type) {
        if (type == DeviceType.INPUT) {
            inputDevices.forEach(device -> {
                device.close();
                voiceClient.getEventBus().unregister(voiceClient, device);
            });
            inputDevices.clear();
        } else if (type == DeviceType.OUTPUT) {
            outputDevices.forEach(device -> {
                device.close();
                voiceClient.getEventBus().unregister(voiceClient, device);
            });
            outputDevices.clear();
        } else {
            getDevices(null).forEach(device -> {
                device.close();
                voiceClient.getEventBus().unregister(voiceClient, device);
            });
            inputDevices.clear();
            outputDevices.clear();
        }
    }

    @Override
    public <T extends AudioDevice> Collection<T> getDevices(DeviceType type) {
        if (type == DeviceType.INPUT) {
            return (Collection<T>) inputDevices;
        } else if (type == DeviceType.OUTPUT) {
            return (Collection<T>) outputDevices;
        } else {
            ImmutableList.Builder<AudioDevice> builder = ImmutableList.builder();
            return (Collection<T>) builder.addAll(inputDevices).addAll(outputDevices).build();
        }
    }

    @Override
    public @NotNull SourceGroup createSourceGroup() {
        return new VoiceOutputSourceGroup(this);
    }

    @Override
    public @NotNull InputDevice openInputDevice(@Nullable AudioFormat format) throws DeviceException {
        // Use javax for mac by default
        if (isMac() && !config.getVoice().getUseJavaxInput().value()) {
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
    public @NotNull OutputDevice<AlSource> openOutputDevice(@Nullable AudioFormat format) throws DeviceException {
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

        return (OutputDevice<AlSource>) deviceFactory.openDevice(
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

    private void tickJob() throws DeviceException {
        DeviceFactory outputFactory = voiceClient.getDeviceFactoryManager().getDeviceFactory("AL_OUTPUT")
                .orElseThrow(() -> new DeviceException("OpenAL output device factory is not registered"));

        String inputFactoryName = config.getVoice().getUseJavaxInput().value()
                ? "JAVAX_INPUT"
                : "AL_INPUT";
        DeviceFactory inputFactory = voiceClient
                .getDeviceFactoryManager()
                .getDeviceFactory(inputFactoryName)
                .orElseThrow(() -> new IllegalStateException("OpenAL input factory is not registered"));;

        if (outputDevices.isEmpty()) {
            if (outputFactory.getDeviceNames().size() > 0) {
                try {
                    add(openOutputDevice(null));
                } catch (Exception e) {
                    LOGGER.error("Failed to open primary OpenAL output device", e);
                }

                if (!voiceClient.getAudioCapture().isActive() && !inputDevices.isEmpty()) {
                    voiceClient.getAudioCapture().start();
                }

                voiceClient.getSourceManager().clear();
            }
        } else {
            outputDevices.stream()
                    .filter(device -> !device.isOpen())
                    .forEach(device -> {
                        device.close();
                        remove(device);
                    });
        }

        if (inputDevices.isEmpty()) {
            if (inputFactory.getDeviceNames().size() > 0 && !config.getVoice().getDisableInputDevice().value()) {
                try {
                    replace(null, openInputDevice(null));
                } catch (Exception e) {
                    LOGGER.error("Failed to open input device", e);
                }

                if (!voiceClient.getAudioCapture().isActive()) {
                    voiceClient.getAudioCapture().start();
                }
            }
        } else {
            inputDevices.stream()
                    .filter(device -> !device.isOpen())
                    .forEach(device -> {
                        device.close();
                        remove(device);
                    });
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

    private List<AudioDevice> getDevicesList(AudioDevice device) {
        List<AudioDevice> devices;
        if (device instanceof InputDevice) {
            devices = inputDevices;
        } else if (device instanceof OutputDevice) {
            devices = outputDevices;
        } else {
            throw new IllegalArgumentException("device not implements InputDevice or OutputDevice");
        }

        return devices;
    }
}
