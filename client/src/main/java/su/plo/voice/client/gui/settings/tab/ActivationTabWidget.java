package su.plo.voice.client.gui.settings.tab;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.BooleanConfigEntry;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.config.entry.IntConfigEntry;
import su.plo.lib.mod.client.ResourceLocationUtil;
import su.plo.lib.mod.client.gui.components.IconButton;
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.chat.style.McTextStyle;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.api.client.audio.capture.ClientActivationManager;
import su.plo.voice.api.client.config.hotkey.Hotkey;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.client.event.audio.capture.ClientActivationRegisteredEvent;
import su.plo.voice.api.client.event.audio.capture.ClientActivationUnregisteredEvent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.audio.capture.VoiceClientActivation;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.config.capture.ConfigClientActivation;
import su.plo.voice.client.config.hotkey.HotkeyConfigEntry;
import su.plo.voice.client.extension.TextKt;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.gui.settings.widget.CompositeRowWidget;
import su.plo.voice.client.gui.settings.widget.DistanceSliderWidget;
import su.plo.voice.client.gui.settings.widget.DropDownWidget;
import su.plo.voice.client.gui.settings.widget.HotKeyWidget;
import su.plo.voice.client.gui.settings.widget.NumberTextFieldWidget;
import su.plo.voice.proto.data.audio.capture.Activation;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;

import java.awt.Color;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private Map<UUID, Set<UUID>> activationConflicts;

    public ActivationTabWidget(@NotNull VoiceSettingsScreen parent,
                               @NotNull PlasmoVoiceClient voiceClient,
                               @NotNull VoiceClientConfig config) {
        super(parent, voiceClient, config);

        this.activations = voiceClient.getActivationManager();
    }

    @Override
    public void init() {
        super.init();

        Map<UUID, Set<UUID>> proximityConflicts = detectActivationConflicts(true);
        Map<UUID, Set<UUID>> nonProximityConflicts = detectActivationConflicts(false);
        Map<UUID, Set<UUID>> conflicts = Maps.newHashMap();
        conflicts.putAll(proximityConflicts);
        conflicts.putAll(nonProximityConflicts);

        this.activationConflicts = conflicts;

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
        Minecraft.getInstance().execute(this::init);
    }

    @EventSubscribe
    public void onActivationUnregister(@NotNull ClientActivationUnregisteredEvent event) {
        Minecraft.getInstance().execute(this::init);
    }

    private Map<UUID, Set<UUID>> detectActivationConflicts(boolean proximity) {
        Map<UUID, Set<UUID>> conflicts = Maps.newHashMap();
        List<ClientActivation> allActivations = Lists.newArrayList(activations.getActivations());

        List<ClientActivation> activationsToCheck = allActivations.stream()
                .filter(activation -> activation.isProximity() == proximity)
                .sorted(Comparator.comparingInt(Activation::getWeight))
                .collect(Collectors.toList());

        if (activationsToCheck.size() < 2) return conflicts;

        for (int i = 0; i < activationsToCheck.size(); i++) {
            ClientActivation current = activationsToCheck.get(i);
            Set<UUID> currentConflicts = conflicts.getOrDefault(current.getId(), Sets.newHashSet());

            for (int j = i + 1; j < activationsToCheck.size(); j++) {
                ClientActivation higher = activationsToCheck.get(j);

                if (!higher.isTransitive()) {
                    ConfigClientActivation higherConfig = config.getActivations()
                            .getActivation(higher.getId())
                            .orElse(null);
                    if (higherConfig == null) continue;

                    ConfigClientActivation currentConfig = config.getActivations()
                            .getActivation(current.getId())
                            .orElse(null);
                    if (currentConfig == null) continue;

                    ClientActivation.Type higherType = higherConfig.getConfigType().value();
                    ClientActivation.Type currentType = currentConfig.getConfigType().value();

                    if (higherType == ClientActivation.Type.PUSH_TO_TALK &&
                        currentType == ClientActivation.Type.PUSH_TO_TALK) {

                        VoiceClientActivation higherVoice = (VoiceClientActivation) higher;
                        VoiceClientActivation currentVoice = (VoiceClientActivation) current;

                        Hotkey higherKey = higherVoice.getPttConfigEntry().value();
                        Hotkey currentKey = currentVoice.getPttConfigEntry().value();

                        if (higherKey.getKeys().equals(currentKey.getKeys())) {
                            currentConflicts.add(higher.getId());
                        }
                    } else if (
                            (currentType == ClientActivation.Type.VOICE || currentType == ClientActivation.Type.INHERIT) &&
                                    (higherType == ClientActivation.Type.VOICE || higherType == ClientActivation.Type.INHERIT)) {
                        currentConflicts.add(higher.getId());
                    }
                }
            }

            if (!currentConflicts.isEmpty()) {
                conflicts.put(current.getId(), currentConflicts);
            }
        }

        return conflicts;
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

    private OptionEntry<?> createActivationType(
            @NotNull ClientActivation activation,
            @NotNull ConfigClientActivation activationConfig,
            boolean canInherit
    ) {
        Set<UUID> conflicts = activationConflicts.get(activation.getId());
        boolean hasConflicts = conflicts != null;

        int dropdownWidth = activation.getType() == ClientActivation.Type.PUSH_TO_TALK
                ? ELEMENT_WIDTH
                : ELEMENT_WIDTH - 24;

        if (hasConflicts) {
            dropdownWidth -= 24;
        }

        DropDownWidget dropdown = new DropDownWidget(
                parent,
                0,
                0,
                dropdownWidth,
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

        IconButton warningIcon = null;
        if (hasConflicts) {
            warningIcon = createConflictIcon(activation, conflicts);
        }

        CompositeRowWidget row = new CompositeRowWidget(
                0,
                0,
                activation.getType() == ClientActivation.Type.PUSH_TO_TALK
                        ? ELEMENT_WIDTH
                        : ELEMENT_WIDTH - 24,
                20,
                4,
                dropdown,
                warningIcon
        );

        return new ActivationToggleStateEntry<>(
                McTextComponent.translatable("gui.plasmovoice.activation.type"),
                row,
                McTextComponent.translatable(activation.getTranslation()),
                activationConfig.getConfigType(),
                activationConfig.getConfigToggle(),
                null,
                (btn, element) -> {
                    dropdown.setText(TYPES.get(activation.getType().ordinal()));
                    init();
                }
        );
    }

    private IconButton createConflictIcon(@NotNull ClientActivation activation, @NotNull Set<UUID> conflicts) {
        McTextComponent conflictingNames = TextKt.join(
                conflicts
                        .stream()
                        .map(activations::getActivationById)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(ClientActivation::getTranslation)
                        .map(McTextComponent::translatable)
                        .collect(Collectors.toList()),
                McTextComponent.literal("\", \"")
        );

        IconButton icon = new IconButton(
                0,
                0,
                20,
                20,
                button -> {},
                (button, context, mouseX, mouseY) -> {
                    parent.setTooltip(McTextComponent.translatable(
                            "gui.plasmovoice.activation.conflict",
                            McTextComponent.translatable(activation.getTranslation()),
                            conflictingNames
                    ));
                },
                ResourceLocationUtil.mod("textures/icons/warning.png"),
                false
        );

        icon.setIconColor(new Color(0xFAC653));
        return icon;
    }

    private OptionEntry<HotKeyWidget> createActivationButton(@NotNull VoiceClientActivation activation) {
        String translatable = "gui.plasmovoice.activation.toggle_button";
        HotkeyConfigEntry entry = activation.getToggleConfigEntry();

        if (activation.getType() == ClientActivation.Type.PUSH_TO_TALK) {
            translatable = "gui.plasmovoice.activation.ptt_button";
            entry = activation.getPttConfigEntry();
        }

        entry.clearChangeListeners();
        entry.addChangeListener(event -> init());

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

    private class ActivationToggleStateEntry<W extends GuiAbstractWidget> extends ButtonOptionEntry<W> {

        public ActivationToggleStateEntry(
                @NotNull McTextComponent text,
                @NotNull W widget,
                @NotNull McTextComponent activationName,
                @NotNull EnumConfigEntry<ClientActivation.Type> entry,
                @NotNull BooleanConfigEntry stateEntry,
                @Nullable McTextComponent tooltip,
                @Nullable OptionResetAction<W> resetAction
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
                    ResourceLocation.tryParse("plasmovoice:textures/icons/microphone_menu.png"),
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
                    ResourceLocation.tryParse("plasmovoice:textures/icons/microphone_menu_disabled.png"),
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
