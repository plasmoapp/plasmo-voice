package su.plo.voice.client.gui.settings.tab;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.gui.components.Button;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.AudioCapture;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.chat.TextComponent;
import su.plo.voice.client.audio.capture.VoiceClientActivation;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.config.capture.ConfigClientActivation;
import su.plo.voice.client.config.keybind.KeyBindingConfigEntry;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.gui.settings.widget.CircularButton;
import su.plo.voice.client.gui.settings.widget.DistanceSliderWidget;
import su.plo.voice.client.gui.settings.widget.HotKeyWidget;
import su.plo.voice.client.gui.settings.widget.NumberTextFieldWidget;
import su.plo.voice.proto.data.capture.VoiceActivation;

import java.util.List;
import java.util.Optional;

public final class ActivationTabWidget extends AbstractHotKeysTabWidget {

    private static final List<TextComponent> TYPES = ImmutableList.of(
            TextComponent.translatable("gui.plasmovoice.activation.type_ptt"),
            TextComponent.translatable("gui.plasmovoice.activation.type_voice"),
            TextComponent.translatable("gui.plasmovoice.activation.type_inherit")
    );
    private static final List<TextComponent> NO_INHERIT_TYPES = ImmutableList.of(
            TextComponent.translatable("gui.plasmovoice.activation.type_ptt"),
            TextComponent.translatable("gui.plasmovoice.activation.type_voice")
    );

    private final AudioCapture capture;

    public ActivationTabWidget(@NotNull MinecraftClientLib minecraft,
                               @NotNull VoiceSettingsScreen parent,
                               @NotNull PlasmoVoiceClient voiceClient,
                               @NotNull ClientConfig config) {
        super(minecraft, parent, voiceClient, config);

        this.capture = voiceClient.getAudioCapture();
    }

    @Override
    public void init() {
        super.init();

        capture.getActivationById(VoiceActivation.PROXIMITY_ID).ifPresent(activation ->
                createActivation(activation, false)
        );

        capture.getActivations().forEach(activation -> createActivation(activation, true));
    }

    private void createActivation(ClientActivation activation, boolean canInherit) {
        Optional<ServerInfo> serverInfo = voiceClient.getServerInfo();
        if (!serverInfo.isPresent()) throw new IllegalStateException("Not connected");

        Optional<ClientConfig.Server> serverConfig = config.getServers().getById(serverInfo.get().getServerId());
        if (!serverConfig.isPresent()) throw new IllegalStateException("Not connected");

        Optional<ConfigClientActivation> activationConfig = serverConfig.get().getActivation(activation.getId());
        if (!activationConfig.isPresent()) throw new IllegalStateException("Activation client config is empty");

        addEntry(new CategoryEntry(TextComponent.translatable(activation.getTranslation())));
        addEntry(createActivationType(activation, activationConfig.get(), canInherit));
        addEntry(createActivationButton((VoiceClientActivation) activation));
        if (activation.getDistances().size() > 0)
            addEntry(createDistance(activation, activationConfig.get()));
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
                TextComponent.translatable("gui.plasmovoice.activation.type"),
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

    private OptionEntry<?> createDistance(ClientActivation activation,
                                          ConfigClientActivation activationConfig) {
        if (activation.getMinDistance() == -1)
            return createDistanceText(activation, activationConfig);

        return createDistanceSlider(activation, activationConfig);
    }

    private OptionEntry<DistanceSliderWidget> createDistanceSlider(ClientActivation activation,
                                                                   ConfigClientActivation activationConfig) {
        DistanceSliderWidget sliderWidget = new DistanceSliderWidget(
                minecraft,
                activation,
                activationConfig.getConfigDistance(),
                0,
                0,
                ELEMENT_WIDTH,
                20
        );

        return new OptionEntry<>(
                TextComponent.translatable("gui.plasmovoice.activation.distance", TextComponent.translatable(activation.getTranslation())),
                sliderWidget,
                activationConfig.getConfigDistance()
        );
    }

    private OptionEntry<NumberTextFieldWidget> createDistanceText(ClientActivation activation,
                                                                  ConfigClientActivation activationConfig) {
        NumberTextFieldWidget textField = new NumberTextFieldWidget(
                minecraft,
                activationConfig.getConfigDistance(),
                0,
                0,
                ELEMENT_WIDTH,
                20
        );

        return new OptionEntry<>(
                TextComponent.translatable("gui.plasmovoice.activation.distance", TextComponent.translatable(activation.getTranslation())),
                textField,
                activationConfig.getConfigDistance()
        );
    }
}
