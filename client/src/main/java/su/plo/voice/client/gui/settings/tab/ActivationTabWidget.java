package su.plo.voice.client.gui.settings.tab;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.BooleanConfigEntry;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.config.entry.IntConfigEntry;
import su.plo.lib.mod.client.gui.components.IconButton;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.chat.style.McTextStyle;
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
import su.plo.voice.client.config.hotkey.HotkeyConfigEntry;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.gui.settings.widget.*;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;
import gg.essential.universal.UMinecraft;

import java.util.Collections;
import java.util.List;

public final class ActivationTabWidget extends AbstractHotKeysTabWidget {

    private static final List<McTextComponent> TYPES = ImmutableList.of(
            McTextComponent.translatable("gui.plasmovoice.activation.type_ptt"),
            McTextComponent.translatable("gui.plasmovoice.activation.type_voice"),
            McTextComponent.translatable("gui.plasmovoice.activation.type_inherit")
    );
    private static final List<McTextComponent> NO_INHERIT_TYPES = ImmutableList.of(
            McTextComponent.translatable("gui.plasmovoice.activation.type_ptt"),
            McTextComponent.translatable("gui.plasmovoice.activation.type_voice")
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
        ServerInfo serverInfo = voiceClient.getServerInfo()
                .orElseThrow(() -> new IllegalStateException("Not connected"));

        VoiceClientConfig.Server serverConfig = config.getServers().getById(serverInfo.getServerId())
                .orElseThrow(() -> new IllegalStateException("Not connected"));

        ConfigClientActivation activationConfig = config.getActivations().getActivation(activation.getId())
                .orElseThrow(() -> new IllegalStateException("Activation config is empty"));

        IntConfigEntry activationDistance = serverConfig.getActivationDistance(activation.getId())
                .orElseThrow(() -> new IllegalStateException("Activation distance config is empty"));

        addEntry(new CategoryEntry(McTextComponent.translatable(activation.getTranslation())));
        addEntry(createActivationType(activation, activationConfig, canInherit));
        addEntry(createActivationButton((VoiceClientActivation) activation));
        if (activation.getDistances().size() > 0) {
            createDistance(activation, activationDistance);
        }
    }

    private OptionEntry<DropDownWidget> createActivationType(
            @NotNull ClientActivation activation,
            @NotNull ConfigClientActivation activationConfig,
            boolean canInherit
    ) {
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
                McTextComponent.translatable("gui.plasmovoice.activation.type"),
                dropdown,
                McTextComponent.translatable(activation.getTranslation()),
                activationConfig.getConfigType(),
                activationConfig.getConfigToggle(),
                null,
                (btn, element) -> {
                    element.setText(TYPES.get(activation.getType().ordinal()));
                    init();
                }
        );
    }

    private OptionEntry<HotKeyWidget> createActivationButton(@NotNull VoiceClientActivation activation) {
        String translatable = "gui.plasmovoice.activation.toggle_button";
        HotkeyConfigEntry entry = activation.getToggleConfigEntry();

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

    private void createDistance(
            @NotNull ClientActivation activation,
            @NotNull IntConfigEntry activationDistance
    ) {
        if (activation.getDistances().size() == 0) return;

        if (activation.getMinDistance() == -1) {
            addEntry(createDistanceText(activation, activationDistance));
        } else {
            addEntry(createDistanceSlider(activation, activationDistance));
        }
    }

    private OptionEntry<DistanceSliderWidget> createDistanceSlider(
            @NotNull ClientActivation activation,
            @NotNull IntConfigEntry activationDistance
    ) {
        DistanceSliderWidget sliderWidget = new DistanceSliderWidget(
                activation,
                activationDistance,
                0,
                0,
                ELEMENT_WIDTH,
                20
        );

        return new OptionEntry<>(
                McTextComponent.translatable("gui.plasmovoice.activation.distance", McTextComponent.translatable(activation.getTranslation())),
                sliderWidget,
                activationDistance
        );
    }

    private OptionEntry<NumberTextFieldWidget> createDistanceText(
            @NotNull ClientActivation activation,
            @NotNull IntConfigEntry activationDistance
    ) {
        NumberTextFieldWidget textField = new NumberTextFieldWidget(
                activationDistance,
                0,
                0,
                ELEMENT_WIDTH,
                20
        );

        return new OptionEntry<>(
                McTextComponent.translatable("gui.plasmovoice.activation.distance", McTextComponent.translatable(activation.getTranslation())),
                textField,
                activationDistance
        );
    }

    private class ActivationToggleStateEntry extends ButtonOptionEntry<DropDownWidget> {

        public ActivationToggleStateEntry(
                @NotNull McTextComponent text,
                @NotNull DropDownWidget widget,
                @NotNull McTextComponent activationName,
                @NotNull EnumConfigEntry<ClientActivation.Type> entry,
                @NotNull BooleanConfigEntry stateEntry,
                @Nullable McTextComponent tooltip,
                @Nullable OptionResetAction<DropDownWidget> resetAction
        ) {
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
                        parent.setTooltip(McTextComponent.translatable(
                                "gui.plasmovoice.activation.toggle",
                                activationName,
                                McTextComponent.translatable("gui.plasmovoice.toggle.currently",
                                        McTextComponent.translatable("gui.plasmovoice.toggle.enabled").withStyle(McTextStyle.GREEN)
                                ).withStyle(McTextStyle.GRAY)
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
                        parent.setTooltip(McTextComponent.translatable(
                                "gui.plasmovoice.activation.toggle",
                                activationName,
                                McTextComponent.translatable("gui.plasmovoice.toggle.currently",
                                        McTextComponent.translatable("gui.plasmovoice.toggle.disabled").withStyle(McTextStyle.RED)
                                ).withStyle(McTextStyle.GRAY)
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
