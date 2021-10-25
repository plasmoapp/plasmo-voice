package su.plo.voice.client.gui.tabs;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.MicIconPositionScreen;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.gui.widgets.*;
import su.plo.voice.client.sound.openal.CustomSoundEngine;
import su.plo.voice.client.utils.TextUtils;

import java.util.List;
import java.util.Objects;

public class GeneralTabWidget extends TabWidget {
    public GeneralTabWidget(Minecraft client, VoiceSettingsScreen parent) {
//        super(minecraftClient, width, height, top, bottom, itemHeight);
        super(client, parent);

        this.addEntry(new CategoryEntry(new TranslatableComponent("gui.plasmo_voice.general.audio"), 24));
        this.addEntry(new OptionEntry(
                new TranslatableComponent("gui.plasmo_voice.general.output_device"),
                new DropDownWidget(parent, 0, 0, 97, 20,
                        TextUtils.formatAlDeviceName(CustomSoundEngine.getCurrentDevice()),
                        TextUtils.formatAlDeviceNames(CustomSoundEngine.getDevices()),
                        true,
                        i -> {
                            String device = CustomSoundEngine.getDevices().get(i);
                            if (Objects.equals(device, CustomSoundEngine.getDefaultDevice())) {
                                device = null;
                            }

                            VoiceClient.getClientConfig().speaker.set(device);
                            VoiceClient.getClientConfig().save();

                            // restart sound engine
                            VoiceClient.getSoundEngine().restart();
                        }),
                VoiceClient.getClientConfig().speaker,
                (button, element) -> {
                    element.setMessage(TextUtils.formatAlDeviceName(CustomSoundEngine.getCurrentDevice()));
                })
        );
        this.addEntry(new OptionEntry(
                new TranslatableComponent("gui.plasmo_voice.general.voice_chat_volume"),
                new VoiceVolumeSlider(0, 0, 97, VoiceClient.getClientConfig().voiceVolume),
                VoiceClient.getClientConfig().voiceVolume,
                TextUtils.multiLine("gui.plasmo_voice.general.voice_chat_volume.tooltip", 2),
                (button, element) -> {
                    ((VoiceVolumeSlider) element).updateValue();
                })
        );
        if (VoiceClient.getClientConfig().showPriorityVolume.get()) {
            this.addEntry(new OptionEntry(
                    new TranslatableComponent("gui.plasmo_voice.general.voice_chat_volume.priority"),
                    new VoiceVolumeSlider(0, 0, 97, VoiceClient.getClientConfig().priorityVolume),
                    VoiceClient.getClientConfig().priorityVolume,
                    TextUtils.multiLine("gui.plasmo_voice.general.voice_chat_volume.tooltip", 2),
                    (button, element) -> {
                        ((VoiceVolumeSlider) element).updateValue();
                    })
            );
        }
        ToggleButton occlusion = new ToggleButton(0, 0, 97, 20, VoiceClient.getClientConfig().occlusion, toggled ->
                VoiceClient.getClientConfig().occlusion.set(toggled)
        );
        occlusion.active = !VoiceClient.getSoundEngine().isSoundPhysics();
        this.addEntry(new OptionEntry(
                new TranslatableComponent("gui.plasmo_voice.general.occlusion"),
                occlusion,
                VoiceClient.getClientConfig().occlusion,
                TextUtils.multiLine("gui.plasmo_voice.general.occlusion.tooltip", 6),
                (button, element) -> {
                    ((ToggleButton) element).updateValue();
                })
        );

        this.addEntry(new CategoryEntry(new TranslatableComponent("gui.plasmo_voice.general.microphone")));
        this.addEntry(new OptionEntry(
                new TranslatableComponent("gui.plasmo_voice.general.microphone"),
                new DropDownWidget(parent, 0, 0, 97, 20,
                        TextUtils.formatAlDeviceName(CustomSoundEngine.getCurrentCaptureDevice()),
                        TextUtils.formatAlDeviceNames(CustomSoundEngine.getCaptureDevices()),
                        true,
                        i -> {
                            String microphone = CustomSoundEngine.getCaptureDevices().get(i);
                            if (Objects.equals(microphone, CustomSoundEngine.getDefaultCaptureDevice())) {
                                microphone = null;
                            }

                            VoiceClient.getClientConfig().microphone.set(microphone);
                            VoiceClient.getClientConfig().save();

                            // restart mic thread
                            VoiceClient.recorder.start();
                        }),
                VoiceClient.getClientConfig().microphone,
                (button, element) -> {
                    element.setMessage(TextUtils.formatAlDeviceName(CustomSoundEngine.getCurrentCaptureDevice()));

                    // restart mic thread
                    VoiceClient.recorder.start();
                })
        );
        this.addEntry(new OptionEntry(
                new TranslatableComponent("gui.plasmo_voice.general.microphone.volume"),
                new MicrophoneVolumeSlider(0, 0, 97),
                VoiceClient.getClientConfig().microphoneAmplification,
                TextUtils.multiLine("gui.plasmo_voice.general.voice_chat_volume.tooltip", 2),
                (button, element) -> {
                    ((MicrophoneVolumeSlider) element).updateValue();
                })
        );
        this.addEntry(new OptionEntry(
                new TranslatableComponent("gui.plasmo_voice.general.voice_distance"),
                new DistanceSlider(0, 0, 97),
                VoiceClient.getClientConfig().getCurrentServerConfig().distance,
                TextUtils.multiLine("gui.plasmo_voice.general.voice_distance.tooltip", 2),
                (button, element) -> {
                    VoiceClient.getServerConfig().setDistance(VoiceClient.getClientConfig().getCurrentServerConfig().distance.get().shortValue());
                    ((DistanceSlider) element).updateValue();
                })
        );
        this.addEntry(new OptionEntry(
                new TranslatableComponent("gui.plasmo_voice.general.priority_distance"),
                new NumberTextFieldWidget(client.font, 0, 0, 97, 20,
                        String.valueOf(VoiceClient.getClientConfig().getCurrentServerConfig().priorityDistance.get()),
                        VoiceClient.getServerConfig().getMaxDistance(),
                        VoiceClient.getServerConfig().getMaxPriorityDistance(),
                        Math.min(VoiceClient.getServerConfig().getMaxPriorityDistance(), VoiceClient.getServerConfig().getMaxDistance() * 2),
                        distance -> {
                            ClientConfig.ServerConfig serverConfig;
                            if (VoiceClient.getClientConfig().getServers()
                                    .containsKey(VoiceClient.getServerConfig().getIp())) {
                                serverConfig = VoiceClient.getClientConfig().getServers()
                                        .get(VoiceClient.getServerConfig().getIp());
                            } else {
                                serverConfig = new ClientConfig.ServerConfig();
                                serverConfig.distance.setDefault((int) VoiceClient.getServerConfig().getDefaultDistance());
                                VoiceClient.getClientConfig().getServers().put(VoiceClient.getServerConfig().getIp(), serverConfig);
                            }

                            if (!Objects.equals(distance, serverConfig.priorityDistance.get())) {
                                serverConfig.priorityDistance.set(distance);
                                VoiceClient.getServerConfig().setPriorityDistance(distance.shortValue());
                            }
                        }),
                VoiceClient.getClientConfig().getCurrentServerConfig().priorityDistance,
                TextUtils.multiLine("gui.plasmo_voice.general.priority_distance.tooltip", 5),
                (button, element) -> {
                    VoiceClient.getServerConfig().setPriorityDistance(VoiceClient.getClientConfig().getCurrentServerConfig().priorityDistance.get().shortValue());
                    ((NumberTextFieldWidget) element).setValue(String.valueOf(VoiceClient.getClientConfig().getCurrentServerConfig().priorityDistance.get()));
                })
        );

        MicrophoneThresholdWidget activationThreshold = new MicrophoneThresholdWidget(0, 0, 97, true, parent);
        String[] activations = new String[]{"gui.plasmo_voice.general.activation.ptt", "gui.plasmo_voice.general.activation.voice"};
        Button voiceActivation = new Button(0, 0, 97, 20, onOff(VoiceClient.getClientConfig().voiceActivation.get()
                        && !VoiceClient.getServerConfig().isVoiceActivationDisabled(),
                activations),
                button -> {
                    VoiceClient.getClientConfig().voiceActivation.invert();
                    boolean enableVoiceActivation = VoiceClient.getClientConfig().voiceActivation.get()
                            && !VoiceClient.getServerConfig().isVoiceActivationDisabled();
                    button.setMessage(onOff(enableVoiceActivation, activations));
                    activationThreshold.active = enableVoiceActivation;
                });

        voiceActivation.active = !VoiceClient.getServerConfig().isVoiceActivationDisabled();
        activationThreshold.active = VoiceClient.getClientConfig().voiceActivation.get();

        this.addEntry(new CategoryEntry(new TranslatableComponent("gui.plasmo_voice.general.activation")));
        this.addEntry(new OptionEntry(
                new TranslatableComponent("gui.plasmo_voice.general.activation.type"),
                voiceActivation,
                VoiceClient.getClientConfig().voiceActivation,
                TextUtils.multiLine("gui.plasmo_voice.general.activation.type.tooltip", 5),
                (button, element) -> {
                    activationThreshold.active = false;
                    element.setMessage(onOff(VoiceClient.getClientConfig().voiceActivation.get()
                                    && !VoiceClient.getServerConfig().isVoiceActivationDisabled(),
                            activations));
                })
        );
        this.addEntry(new OptionEntry(
                new TranslatableComponent("gui.plasmo_voice.general.activation.threshold"),
                activationThreshold,
                VoiceClient.getClientConfig().voiceActivationThreshold,
                TextUtils.multiLine("gui.plasmo_voice.general.activation.threshold.tooltip", 8),
                (button, element) -> {
                    ((MicrophoneThresholdWidget) element).updateValue();
                })
        );

        List<Component> icons = ImmutableList.of(
                new TranslatableComponent("gui.plasmo_voice.general.icons.hud"),
                new TranslatableComponent("gui.plasmo_voice.general.icons.always"),
                new TranslatableComponent("gui.plasmo_voice.general.icons.hidden")
        );

        this.addEntry(new CategoryEntry(new TranslatableComponent("gui.plasmo_voice.general.icons")));
        this.addEntry(new OptionEntry(
                new TranslatableComponent("gui.plasmo_voice.general.icons.show"),
                new DropDownWidget(parent, 0, 0, 97, 20,
                        icons.get(VoiceClient.getClientConfig().showIcons.get()),
                        icons,
                        false,
                        i -> {
                            VoiceClient.getClientConfig().showIcons.set(i);
                        }),
                VoiceClient.getClientConfig().showIcons,
                TextUtils.multiLine("gui.plasmo_voice.general.icons.tooltip", 7),
                (button, element) -> {
                    element.setMessage(icons.get(VoiceClient.getClientConfig().showIcons.get()));
                })
        );
        this.addEntry(new OptionEntry(
                new TranslatableComponent("gui.plasmo_voice.general.icons.position"),
                new Button(0, 0, 97, 20, VoiceClient.getClientConfig().micIconPosition.get().translate(),
                        button -> {
                            client.setScreen(new MicIconPositionScreen(parent));
                        }),
                VoiceClient.getClientConfig().micIconPosition,
                (button, element) -> {
                    element.setMessage(VoiceClient.getClientConfig().micIconPosition.get().translate());
                })
        );
    }

    private TranslatableComponent onOff(boolean on, String[] variants) {
        return new TranslatableComponent(variants[on ? 1 : 0]);
    }

    private TranslatableComponent onOff(int index, String[] variants) {
        return new TranslatableComponent(variants[index]);
    }
}
