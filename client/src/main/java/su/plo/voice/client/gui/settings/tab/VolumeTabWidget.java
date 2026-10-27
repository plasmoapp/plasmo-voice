package su.plo.voice.client.gui.settings.tab;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.BooleanConfigEntry;
import su.plo.config.entry.ConfigEntry;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.lib.mod.client.gui.components.Button;
import su.plo.lib.mod.client.gui.components.CompositeColumnWidget;
import su.plo.lib.mod.client.gui.components.CompositeRowWidget;
import su.plo.lib.mod.client.gui.components.IconButton;
import su.plo.lib.mod.client.gui.components.TextFieldWidget;
import su.plo.lib.mod.client.gui.components.TextWidget;
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.chat.style.McTextStyle;
import su.plo.slib.api.entity.player.McGameProfile;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.line.ClientSourceLine;
import su.plo.voice.api.client.audio.line.ClientSourceLineManager;
import su.plo.voice.api.client.audio.source.ClientAudioSource;
import su.plo.voice.api.client.event.connection.VoicePlayerConnectedEvent;
import su.plo.voice.api.client.event.connection.VoicePlayerDisconnectedEvent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.gui.settings.widget.OptionWidget;
import su.plo.voice.client.gui.settings.widget.PlayerHeadWidget;
import su.plo.voice.client.gui.settings.widget.VolumeSliderWidget;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;
import su.plo.voice.proto.data.audio.source.DirectSourceInfo;
import su.plo.voice.proto.data.player.VoicePlayerInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public final class VolumeTabWidget extends TabWidget {

    private static final ResourceLocation SHOW_ALL_ICON =
        ResourceLocation.tryParse("plasmovoice:textures/icons/eye.png");
    private static final ResourceLocation SHOW_RECENT_ICON =
        ResourceLocation.tryParse(
            "plasmovoice:textures/icons/eye_disabled.png"
        );

    private static final long RECENT_THRESHOLD_MS = 5 * 60 * 1000L;
    private static final int ALWAYS_SHOWN_SOURCE_LINES_LIMIT = 3;

    private static final int PLAYER_LIST_GAP = 6;
    private static final int PLAYER_HEAD_SIZE = 24;
    private static final int PLAYER_LABEL_GAP = 6;

    private final PlasmoVoiceClient voiceClient;
    private final ClientSourceLineManager sourceLines;

    private String currentSearch = "";
    private CompositeColumnWidget playersCategory;
    private CompositeColumnWidget playerList;

    public VolumeTabWidget(
        @NotNull VoiceSettingsScreen parent,
        @NotNull PlasmoVoiceClient voiceClient,
        @NotNull VoiceClientConfig config
    ) {
        super(parent, voiceClient, config);

        this.voiceClient = voiceClient;
        this.sourceLines = voiceClient.getSourceLineManager();
    }

    @Override
    public void init() {
        super.init();
        buildEntries(true);
    }

    @EventSubscribe
    public void onPlayerConnected(@NotNull VoicePlayerConnectedEvent event) {
        Minecraft.getInstance().execute(this::refreshPlayerEntries);
    }

    @EventSubscribe
    public void onPlayerDisconnected(@NotNull VoicePlayerDisconnectedEvent event) {
        Minecraft.getInstance().execute(this::refreshPlayerEntries);
    }

    private void buildEntries(boolean focusSearch) {
        List<ClientSourceLine> sourceLines = Lists.newArrayList(this.sourceLines.getLines());
        Collections.reverse(sourceLines);

        McTextComponent sourcesTitle = McTextComponent.translatable("gui.plasmovoice.volume.sources");
        boolean filterable = sourceLines.size() > ALWAYS_SHOWN_SOURCE_LINES_LIMIT;
        CompositeColumnWidget sources = filterable
            ? addCategory(sourcesTitle, createShowAllButton())
            : addCategory(sourcesTitle);

        boolean showAll = !filterable || config.getVoice().getShowAllSourceLines().value();
        long now = voiceClient.getTimeSupplier().getCurrentTimeMillis();

        sourceLines
            .stream()
            .filter(line -> showAll || isAlwaysShown(line) || isRecentlyUsed(line, now))
            .map(this::createSourceLineVolume)
            .forEach(sources::add);

        createPlayersSearch(focusSearch);
        refreshPlayerEntries();
    }

    private void rebuild() {
        super.init();
        buildEntries(false);
    }

    private boolean isAlwaysShown(@NotNull ClientSourceLine sourceLine) {
        return VoiceSourceLine.PROXIMITY_NAME.equals(sourceLine.getName());
    }

    private boolean isRecentlyUsed(@NotNull ClientSourceLine sourceLine, long now) {
        long lastActivation = sourceLine.getLastActivationTime();
        return lastActivation > 0 && now - lastActivation <= RECENT_THRESHOLD_MS;
    }

    private IconButton createShowAllButton() {
        BooleanConfigEntry showAll = config.getVoice().getShowAllSourceLines();

        return new IconButton(
            0,
            0,
            20,
            20,
            button -> {
                showAll.invert();
                rebuild();
            },
            (button, mouseX, mouseY) ->
                setTooltip(
                    McTextComponent.translatable(
                        showAll.value()
                            ? "gui.plasmovoice.volume.sources.show_recent"
                            : "gui.plasmovoice.volume.sources.show_all"
                    ),
                    mouseX,
                    mouseY
                ),
            () -> showAll.value() ? SHOW_ALL_ICON : SHOW_RECENT_ICON,
            true
        );
    }

    private VolumeOption createSourceLineVolume(@NotNull ClientSourceLine sourceLine) {
        DoubleConfigEntry volumeEntry = config
            .getVoice()
            .getVolumes()
            .getVolume(sourceLine.getName());
        ConfigEntry<Boolean> muteEntry = config
            .getVoice()
            .getVolumes()
            .getMute(sourceLine.getName());

        List<Button> buttons = Lists.newArrayList();
        Runnable updateButtons = createMuteButtonAction(buttons, muteEntry);

        return new VolumeOption(
            iconLabel(
                ResourceLocation.tryParse(sourceLine.getIcon()),
                McTextComponent.translatable(sourceLine.getTranslation())
            ),
            createVolumeSlider(volumeEntry),
            createMuteButton(buttons, updateButtons, muteEntry),
            volumeEntry,
            muteEntry,
            (button, element) -> updateButtons.run()
        );
    }

    private void createPlayersSearch(boolean focusSearch) {
        this.playersCategory = addCategory(McTextComponent.translatable("gui.plasmovoice.volume.players"));

        TextFieldWidget textField = new TextFieldWidget(
            0,
            0,
            0,
            20,
            McTextComponent.translatable(
                "gui.plasmovoice.volume.players_search"
            ).withStyle(McTextStyle.GRAY)
        );

        textField.setValue(currentSearch);

        textField.setResponder(value -> {
            this.currentSearch = value;
            refreshPlayerEntries();

            int playersOffset = getContentOffset(playersCategory);
            if (playersOffset > getViewportHeight() / 2) {
                setScrollTop(playersOffset - PAGE_PADDING);
            } else {
                setScrollTop(0);
            }
        });

        playersCategory.add(textField);
        this.playerList = playersCategory.add(
            new CompositeColumnWidget(PLAYER_LIST_GAP)
        );

        if (!focusSearch) return;

        // Focus must be deferred because init() runs during the mouseClicked chain
        // (tab button click), and GuiScreenListener.mouseClicked overrides focus
        // after init() returns
        parent.deferredRender(() -> {
            boolean isActiveTab = parent
                .getNavigation()
                .getActiveTab()
                .map(tab -> tab == this)
                .orElse(false);
            if (!isActiveTab) return;

            parent.setFocused(this);
            focus(textField);
        });
    }

    private void refreshPlayerEntries() {
        if (playerList == null) return;

        String search = currentSearch.toLowerCase();
        List<GuiAbstractWidget> rows = voiceClient
            .getServerConnection()
            .map(connection -> {
                Map<UUID, McGameProfile> players = Maps.newHashMap();

                joinMap(
                    players,
                    connection
                        .getPlayers()
                        .stream()
                        .filter(player ->
                            player
                                .getPlayerNick()
                                .toLowerCase()
                                .contains(search)
                        )
                        .map(VoicePlayerInfo::toGameProfile)
                        .collect(Collectors.toList())
                );
                joinMap(
                    players,
                    voiceClient
                        .getSourceLineManager()
                        .getLines()
                        .stream()
                        .map(ClientSourceLine::getPlayers)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList())
                );
                joinMap(
                    players,
                    voiceClient
                        .getSourceManager()
                        .getSources()
                        .stream()
                        .map(ClientAudioSource::getSourceInfo)
                        .filter(
                            sourceInfo -> sourceInfo instanceof DirectSourceInfo
                        )
                        .map(sourceInfo ->
                            ((DirectSourceInfo) sourceInfo).getSender()
                        )
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
                );

                return players
                    .values()
                    .stream()
                    .filter(
                        player ->
                            !Minecraft.getInstance()
                                .player.getUUID()
                                .equals(player.getId())
                    )
                    .sorted(Comparator.comparing(McGameProfile::getName))
                    .map(this::createPlayerVolume)
                    .collect(Collectors.<GuiAbstractWidget>toList());
            })
            .orElse(Collections.emptyList());

        playerList.setWidgets(rows);
    }

    private void joinMap(Map<UUID, McGameProfile> map, Collection<McGameProfile> joinCollection) {
        joinCollection.forEach(gameProfile -> {
            McGameProfile mapGameProfile = map.get(gameProfile.getId());

            if (
                Objects.equals(gameProfile, mapGameProfile) &&
                gameProfile.getProperties().size() >
                    mapGameProfile.getProperties().size()
            ) {
                map.put(gameProfile.getId(), gameProfile);
                return;
            }

            map.put(gameProfile.getId(), gameProfile);
        });
    }

    private VolumeOption createPlayerVolume(@NotNull McGameProfile player) {
        DoubleConfigEntry volumeEntry = config
            .getVoice()
            .getVolumes()
            .getPlayerVolume(player.getId());
        ConfigEntry<Boolean> muteEntry = config
            .getVoice()
            .getVolumes()
            .getPlayerMute(player.getId());

        List<Button> buttons = Lists.newArrayList();
        Runnable updateButtons = createMuteButtonAction(buttons, muteEntry);

        return new VolumeOption(
            new CompositeRowWidget(
                PLAYER_LABEL_GAP,
                new PlayerHeadWidget(player, PLAYER_HEAD_SIZE),
                new TextWidget(McTextComponent.literal(player.getName()))
            ),
            createVolumeSlider(volumeEntry),
            createMuteButton(buttons, updateButtons, muteEntry),
            volumeEntry,
            muteEntry,
            (button, element) -> updateButtons.run()
        );
    }

    private VolumeSliderWidget createVolumeSlider(DoubleConfigEntry volumeEntry) {
        return new VolumeSliderWidget(
            voiceClient.getHotkeys(),
            volumeEntry,
            "%",
            0,
            0,
            ELEMENT_WIDTH - 24,
            20
        );
    }

    private Runnable createMuteButtonAction(
        @NotNull List<Button> buttons,
        @NotNull ConfigEntry<Boolean> muteEntry
    ) {
        return () -> {
            buttons.get(0).setVisible(!muteEntry.value());
            buttons.get(1).setVisible(muteEntry.value());
        };
    }

    private List<Button> createMuteButton(
        @NotNull List<Button> buttons,
        @NotNull Runnable updateButtons,
        @NotNull ConfigEntry<Boolean> muteEntry
    ) {
        Button.OnPress buttonClick = button -> {
            muteEntry.set(!muteEntry.value());
            updateButtons.run();
        };

        IconButton muteButton = new IconButton(
            0,
            0,
            20,
            20,
            buttonClick,
            Button.NO_TOOLTIP,
            ResourceLocation.tryParse(
                "plasmovoice:textures/icons/speaker_menu.png"
            ),
            true
        );

        IconButton unmuteButton = new IconButton(
            0,
            0,
            20,
            20,
            buttonClick,
            Button.NO_TOOLTIP,
            ResourceLocation.tryParse(
                "plasmovoice:textures/icons/speaker_menu_disabled.png"
            ),
            true
        );

        muteButton.setVisible(!muteEntry.value());
        unmuteButton.setVisible(muteEntry.value());

        buttons.add(muteButton);
        buttons.add(unmuteButton);

        return buttons;
    }

    class VolumeOption extends OptionWidget<VolumeSliderWidget> {

        private final ConfigEntry<Boolean> muteEntry;

        VolumeOption(
            @NotNull GuiAbstractWidget label,
            @NotNull VolumeSliderWidget slider,
            @NotNull List<Button> buttons,
            @NotNull ConfigEntry<?> entry,
            @NotNull ConfigEntry<Boolean> muteEntry,
            @NotNull ResetAction<VolumeSliderWidget> resetAction
        ) {
            super(parent, label, slider, entry, null, resetAction, buttons);

            this.muteEntry = muteEntry;
        }

        @Override
        protected boolean isDefault() {
            return entry.isDefault() && muteEntry.isDefault();
        }

        @Override
        protected void onReset() {
            muteEntry.reset();
            super.onReset();
        }
    }
}
