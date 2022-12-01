package su.plo.voice.client.gui.settings.tab;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.IntConfigEntry;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.lib.api.client.gui.components.Button;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.api.client.audio.capture.ClientActivationManager;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.client.event.audio.capture.ClientActivationRegisteredEvent;
import su.plo.voice.api.client.event.audio.capture.ClientActivationUnregisteredEvent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.audio.capture.VoiceClientActivation;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.config.capture.ConfigClientActivation;
import su.plo.voice.client.config.keybind.KeyBindingConfigEntry;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.gui.settings.widget.CircularButton;
import su.plo.voice.client.gui.settings.widget.DistanceSliderWidget;
import su.plo.voice.client.gui.settings.widget.HotKeyWidget;
import su.plo.voice.client.gui.settings.widget.NumberTextFieldWidget;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ActivationTabWidget extends AbstractHotKeysTabWidget {

    private static final List<MinecraftTextComponent> TYPES = ImmutableList.of(
            MinecraftTextComponent.translatable("gui.plasmovoice.activation.type_ptt"),
            MinecraftTextComponent.translatable("gui.plasmovoice.activation.type_voice"),
            MinecraftTextComponent.translatable("gui.plasmovoice.activation.type_inherit")
    );
    private static final List<MinecraftTextComponent> NO_INHERIT_TYPES = ImmutableList.of(
            MinecraftTextComponent.translatable("gui.plasmovoice.activation.type_ptt"),
            MinecraftTextComponent.translatable("gui.plasmovoice.activation.type_voice")
    );

    private final ClientActivationManager activations;

    public ActivationTabWidget(@NotNull MinecraftClientLib minecraft,
                               @NotNull VoiceSettingsScreen parent,
                               @NotNull PlasmoVoiceClient voiceClient,
                               @NotNull ClientConfig config) {
        super(minecraft, parent, voiceClient, config);

        this.activations = voiceClient.getActivationManager();
    }

    @Override
    public void init() {
        super.init();

        activations.getParentActivation().ifPresent(activation ->
                createActivation(activation, false)
        );

        List<ClientActivation> activations = Lists.newArrayList(this.activations.getActivations());
        Collections.reverse(activations);
        activations.stream()
                .filter(activation -> !activation.getId().equals(VoiceActivation.PROXIMITY_ID))
                .forEach(activation -> createActivation(activation, true));
    }

    @EventSubscribe
    public void onActivationRegister(@NotNull ClientActivationRegisteredEvent event) {
        init();
    }

    @EventSubscribe
    public void onActivationUnregister(@NotNull ClientActivationUnregisteredEvent event) {
        init();
    }

    private void createActivation(ClientActivation activation, boolean canInherit) {
        Optional<ServerInfo> serverInfo = voiceClient.getServerInfo();
        if (!serverInfo.isPresent()) throw new IllegalStateException("Not connected");

        Optional<ClientConfig.Server> serverConfig = config.getServers().getById(serverInfo.get().getServerId());
        if (!serverConfig.isPresent()) throw new IllegalStateException("Not connected");

        Optional<ConfigClientActivation> activationConfig = config.getActivations().getActivation(activation.getId());
        if (!activationConfig.isPresent()) throw new IllegalStateException("Activation config is empty");

        Optional<IntConfigEntry> activationDistance = serverConfig.get().getActivationDistance(activation.getId());
        if (!activationDistance.isPresent()) throw new IllegalStateException("Activation distance config is empty");

        addEntry(new CategoryEntry(MinecraftTextComponent.translatable(activation.getTranslation())));
        addEntry(createActivationType(activation, activationConfig.get(), canInherit));
        addEntry(createActivationButton((VoiceClientActivation) activation));
        if (activation.getDistances().size() > 0)
            createDistance(activation, activationConfig.get(), activationDistance.get());
    }

    private OptionEntry<CircularButton> createActivationType(ClientActivation activation,
                                                             ConfigClientActivation activationConfig,
                                                             boolean canInherit) {
        CircularButton button = new CircularButton(
                minecraft,
                canInherit ? TYPES : NO_INHERIT_TYPES,
                activation.getType().ordinal(),
                0,
                0,
                ELEMENT_WIDTH,
                20,
                (index) -> {
                    activationConfig.getConfigType().set(
                            ClientActivation.Type.values()[index]
                    );
                    init();
                },
                Button.NO_TOOLTIP
        );

        return new OptionEntry<>(
                MinecraftTextComponent.translatable("gui.plasmovoice.activation.type"),
                button,
                activationConfig.getConfigType(),
                (btn, element) -> {
                    element.setIndex(0);
                    element.updateValue();
                    init();
                }
        );
    }

    private OptionEntry<HotKeyWidget> createActivationButton(VoiceClientActivation activation) {
        String translatable = "gui.plasmovoice.activation.toggle_button";
        KeyBindingConfigEntry entry = activation.getToggleConfigEntry();

        if (activation.getType() == ClientActivation.Type.PUSH_TO_TALK) {
            translatable = "gui.plasmovoice.activation.ptt_button";
            entry = activation.getPttConfigEntry();
        }

        return createHotKey(
                translatable,
                null,
                entry
        );
    }

    private void createDistance(ClientActivation activation,
                                          ConfigClientActivation activationConfig,
                                          IntConfigEntry activationDistance) {
        if (activation.getDistances().size() == 0) return;

        if (activation.getMinDistance() == -1) {
            addEntry(createDistanceText(activation, activationConfig, activationDistance));
        } else {
            addEntry(createDistanceSlider(activation, activationConfig, activationDistance));
        }
    }

    private OptionEntry<DistanceSliderWidget> createDistanceSlider(ClientActivation activation,
                                                                   ConfigClientActivation activationConfig,
                                                                   IntConfigEntry activationDistance) {
        DistanceSliderWidget sliderWidget = new DistanceSliderWidget(
                minecraft,
                activation,
                activationDistance,
                0,
                0,
                ELEMENT_WIDTH,
                20
        );

        return new OptionEntry<>(
                MinecraftTextComponent.translatable("gui.plasmovoice.activation.distance", MinecraftTextComponent.translatable(activation.getTranslation())),
                sliderWidget,
                activationDistance
        );
    }

    private OptionEntry<NumberTextFieldWidget> createDistanceText(ClientActivation activation,
                                                                  ConfigClientActivation activationConfig,
                                                                  IntConfigEntry activationDistance) {
        NumberTextFieldWidget textField = new NumberTextFieldWidget(
                minecraft,
                activationDistance,
                0,
                0,
                ELEMENT_WIDTH,
                20
        );

        return new OptionEntry<>(
                MinecraftTextComponent.translatable("gui.plasmovoice.activation.distance", MinecraftTextComponent.translatable(activation.getTranslation())),
                textField,
                activationDistance
        );
    }
}
