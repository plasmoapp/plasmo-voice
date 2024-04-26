package su.plo.voice.client.gui.settings.tab;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import gg.essential.universal.UMinecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.BooleanConfigEntry;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.config.entry.IntConfigEntry;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.chat.MinecraftTextStyle;
import su.plo.lib.mod.client.gui.components.IconButton;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.api.client.audio.capture.ClientActivationManager;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.client.event.audio.capture.ClientActivationRegisteredEvent;
import su.plo.voice.api.client.event.audio.capture.ClientActivationUnregisteredEvent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.audio.capture.VoiceClientActivation;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.config.capture.ConfigClientActivation;
import su.plo.voice.client.config.keybind.KeyBindingConfigEntry;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.gui.settings.widget.DistanceSliderWidget;
import su.plo.voice.client.gui.settings.widget.DropDownWidget;
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

    public ActivationTabWidget(@NotNull VoiceSettingsScreen parent,
                               @NotNull PlasmoVoiceClient voiceClient,
                               @NotNull VoiceClientConfig config) {
        super(parent, voiceClient, config);

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
        UMinecraft.getMinecraft().execute(this::init);
    }

    @EventSubscribe
    public void onActivationUnregister(@NotNull ClientActivationUnregisteredEvent event) {
        UMinecraft.getMinecraft().execute(this::init);
    }

    private void createActivation(ClientActivation activation, boolean canInherit) {
        Optional<ServerInfo> serverInfo = voiceClient.getServerInfo();
        if (!serverInfo.isPresent()) throw new IllegalStateException("Not connected");

        Optional<VoiceClientConfig.Server> serverConfig = config.getServers().getById(serverInfo.get().getServerId());
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

    private OptionEntry<DropDownWidget> createActivationType(ClientActivation activation,
                                                             ConfigClientActivation activationConfig,
                                                             boolean canInherit) {
        DropDownWidget dropdown = new DropDownWidget(
                parent,
                0,
                0,
                activation.getType() == ClientActivation.Type.PUSH_TO_TALK
                        ? ELEMENT_WIDTH
                        : ELEMENT_WIDTH - 24,
                20,
                TYPES.get(activation.getType().ordinal()),
                canInherit ? TYPES : NO_INHERIT_TYPES,
                false,
                (index) -> {
                    activationConfig.getConfigType().set(
                            ClientActivation.Type.values()[index]
                    );
                    init();
                }
        );

        return new ActivationToggleStateEntry(
                MinecraftTextComponent.translatable("gui.plasmovoice.activation.type"),
                dropdown,
                MinecraftTextComponent.translatable(activation.getTranslation()),
                activationConfig.getConfigType(),
                activationConfig.getConfigToggle(),
                null,
                (btn, element) -> {
                    element.setText(TYPES.get(activation.getType().ordinal()));
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

    private class ActivationToggleStateEntry extends ButtonOptionEntry<DropDownWidget> {

        public ActivationToggleStateEntry(@NotNull MinecraftTextComponent text,
                                          @NotNull DropDownWidget widget,
                                          @NotNull MinecraftTextComponent activationName,
                                          @NotNull EnumConfigEntry<ClientActivation.Type> entry,
                                          @NotNull BooleanConfigEntry stateEntry,
                                          @Nullable MinecraftTextComponent tooltip,
                                          @Nullable OptionResetAction<DropDownWidget> resetAction) {
            super(text, widget, Lists.newArrayList(), entry, tooltip, resetAction);

            if (entry.value() == ClientActivation.Type.PUSH_TO_TALK) return;

            IconButton disableToggleState = new IconButton(
                    parent.getWidth() - 52,
                    8,
                    20,
                    20,
                    (button) -> {
                        buttons.get(0).setVisible(false);
                        buttons.get(1).setVisible(true);

                        stateEntry.set(true);
                    },
                    (button, render, mouseX, mouseY) -> {
                        parent.setTooltip(MinecraftTextComponent.translatable(
                                "gui.plasmovoice.activation.toggle",
                                activationName,
                                MinecraftTextComponent.translatable("gui.plasmovoice.toggle.currently",
                                        MinecraftTextComponent.translatable("gui.plasmovoice.toggle.enabled").withStyle(MinecraftTextStyle.GREEN)
                                ).withStyle(MinecraftTextStyle.GRAY)
                        ));
                    },
                    new ResourceLocation("plasmovoice:textures/icons/microphone_menu.png"),
                    true
            );
            IconButton enableToggleState = new IconButton(
                    parent.getWidth() - 52,
                    8,
                    20,
                    20,
                    (button) -> {
                        buttons.get(0).setVisible(true);
                        buttons.get(1).setVisible(false);

                        stateEntry.set(false);
                    },
                    (button, render, mouseX, mouseY) -> {
                        parent.setTooltip(MinecraftTextComponent.translatable(
                                "gui.plasmovoice.activation.toggle",
                                activationName,
                                MinecraftTextComponent.translatable("gui.plasmovoice.toggle.currently",
                                        MinecraftTextComponent.translatable("gui.plasmovoice.toggle.disabled").withStyle(MinecraftTextStyle.RED)
                                ).withStyle(MinecraftTextStyle.GRAY)
                        ));
                    },
                    new ResourceLocation("plasmovoice:textures/icons/microphone_menu_disabled.png"),
                    true
            );

            disableToggleState.setVisible(!stateEntry.value());
            enableToggleState.setVisible(stateEntry.value());

            buttons.add(disableToggleState);
            buttons.add(enableToggleState);

            widgets.addAll(buttons);
        }
    }
}
