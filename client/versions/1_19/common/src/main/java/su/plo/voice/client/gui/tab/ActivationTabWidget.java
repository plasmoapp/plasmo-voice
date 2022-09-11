package su.plo.voice.client.gui.tab;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.AudioCapture;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.client.audio.capture.VoiceClientActivation;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.config.capture.ConfigClientActivation;
import su.plo.voice.client.config.keybind.KeyBindingConfigEntry;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.gui.widget.CircularButton;
import su.plo.voice.client.gui.widget.DistanceSliderWidget;
import su.plo.voice.client.gui.widget.KeyBindingWidget;
import su.plo.voice.client.gui.widget.NumberTextFieldWidget;
import su.plo.voice.proto.data.capture.VoiceActivation;

import java.util.List;
import java.util.Optional;

public final class ActivationTabWidget extends KeyBindingTabWidget {

    private static final List<Component> TYPES = ImmutableList.of(
            Component.translatable("gui.plasmovoice.activation.type_ptt"),
            Component.translatable("gui.plasmovoice.activation.type_voice"),
            Component.translatable("gui.plasmovoice.activation.type_inherit")
    );
    private static final List<Component> NO_INHERIT_TYPES = ImmutableList.of(
            Component.translatable("gui.plasmovoice.activation.type_ptt"),
            Component.translatable("gui.plasmovoice.activation.type_voice")
    );

    private final PlasmoVoiceClient voiceClient;
    private final AudioCapture capture;
    private final ClientConfig config;

    public ActivationTabWidget(Minecraft minecraft,
                               VoiceSettingsScreen parent,
                               PlasmoVoiceClient voiceClient,
                               ClientConfig config) {
        super(minecraft, parent, voiceClient.getKeyBindings());

        this.voiceClient = voiceClient;
        this.capture = voiceClient.getAudioCapture();
        this.config = config;
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
        if (serverInfo.isEmpty()) throw new IllegalStateException("Not connected");

        Optional<ClientConfig.Server> serverConfig = config.getServers().getById(serverInfo.get().getServerId());
        if (serverConfig.isEmpty()) throw new IllegalStateException("Not connected");

        Optional<ConfigClientActivation> activationConfig = serverConfig.get().getActivation(activation.getId());
        if (activationConfig.isEmpty()) throw new IllegalStateException("Activation client config is empty");


        addEntry(new CategoryEntry(Component.translatable(activation.getTranslation())));
        addEntry(createActivationType(activation, activationConfig.get(), canInherit));
        addEntry(createActivationButton((VoiceClientActivation) activation));
        if (activation.getDistances().size() > 0)
            addEntry(createDistance(activation, activationConfig.get()));
    }

    private OptionEntry<CircularButton> createActivationType(ClientActivation activation,
                                                             ConfigClientActivation activationConfig,
                                                             boolean canInherit) {
        CircularButton button = new CircularButton(
                0,
                0,
                97,
                20,
                canInherit ? TYPES : NO_INHERIT_TYPES,
                activation.getType().ordinal(),
                (index) -> {
                    activationConfig.getConfigType().set(
                            ClientActivation.Type.values()[index]
                    );
                    init();
                },
                Button.NO_TOOLTIP
        );

        return new OptionEntry<>(
                Component.translatable("gui.plasmovoice.activation.type"),
                button,
                activationConfig.getConfigType(),
                (btn, element) -> {
                    element.setIndex(0);
                    element.updateValue();
                    init();
                }
        );
    }

    private OptionEntry<KeyBindingWidget> createActivationButton(VoiceClientActivation activation) {
        String translatable = "gui.plasmovoice.activation.toggle_button";
        KeyBindingConfigEntry entry = activation.getToggleConfigEntry();

        if (activation.getType() == ClientActivation.Type.PUSH_TO_TALK) {
            translatable = "gui.plasmovoice.activation.ptt_button";
            entry = activation.getPttConfigEntry();
        }

        return createKeyBinding(
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
                0,
                0,
                97,
                activation,
                activationConfig.getConfigDistance()
        );

        return new OptionEntry<>(
                Component.translatable("gui.plasmovoice.activation.distance", Component.translatable(activation.getTranslation())),
                sliderWidget,
                activationConfig.getConfigDistance()
        );
    }

    private OptionEntry<NumberTextFieldWidget> createDistanceText(ClientActivation activation,
                                                                  ConfigClientActivation activationConfig) {
        NumberTextFieldWidget textField = new NumberTextFieldWidget(
                minecraft.font,
                0,
                0,
                97,
                20,
                activationConfig.getConfigDistance()
        );

        return new OptionEntry<>(
                Component.translatable("gui.plasmovoice.activation.distance", Component.translatable(activation.getTranslation())),
                textField,
                activationConfig.getConfigDistance()
        );
    }
}
