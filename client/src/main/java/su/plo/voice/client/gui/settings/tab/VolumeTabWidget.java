package su.plo.voice.client.gui.settings.tab;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.chat.style.McTextStyle;
import su.plo.slib.api.entity.player.McGameProfile;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.ConfigEntry;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.lib.mod.client.gui.components.Button;
import su.plo.lib.mod.client.gui.components.IconButton;
import su.plo.lib.mod.client.gui.components.TextFieldWidget;
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.lib.mod.client.render.texture.ModPlayerSkins;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.line.ClientSourceLine;
import su.plo.voice.api.client.audio.line.ClientSourceLineManager;
import su.plo.voice.api.client.audio.source.ClientAudioSource;
import su.plo.voice.api.client.event.connection.VoicePlayerConnectedEvent;
import su.plo.voice.api.client.event.connection.VoicePlayerDisconnectedEvent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.gui.settings.widget.UpdatableWidget;
import su.plo.voice.client.gui.settings.widget.VolumeSliderWidget;
import su.plo.voice.proto.data.audio.source.DirectSourceInfo;
import su.plo.voice.proto.data.player.VoicePlayerInfo;

import java.util.*;
import java.util.stream.Collectors;

public final class VolumeTabWidget extends TabWidget {

    private final PlasmoVoiceClient voiceClient;
    private final ClientSourceLineManager sourceLines;

    private String currentSearch = "";

    public VolumeTabWidget(@NotNull VoiceSettingsScreen parent,
                           @NotNull PlasmoVoiceClient voiceClient,
                           @NotNull VoiceClientConfig config) {
        super(parent, voiceClient, config);

        this.voiceClient = voiceClient;
        this.sourceLines = voiceClient.getSourceLineManager();
    }

    @Override
    public void init() {
        super.init();

        addEntry(new CategoryEntry(McTextComponent.translatable("gui.plasmovoice.volume.sources"), 24));

        List<ClientSourceLine> sourceLines = Lists.newArrayList(this.sourceLines.getLines());
        Collections.reverse(sourceLines);
        sourceLines.forEach(this::createSourceLineVolume);

        addEntry(new CategoryEntry(McTextComponent.translatable("gui.plasmovoice.volume.players"), 24));
        createPlayersSearch();
        refreshPlayerEntries();
    }

    @EventSubscribe
    public void onPlayerConnected(@NotNull VoicePlayerConnectedEvent event) {
        Minecraft.getInstance().execute(this::refreshPlayerEntries);
    }

    @EventSubscribe
    public void onPlayerDisconnected(@NotNull VoicePlayerDisconnectedEvent event) {
        Minecraft.getInstance().execute(this::refreshPlayerEntries);
    }

    private void createSourceLineVolume(@NotNull ClientSourceLine sourceLine) {
        DoubleConfigEntry volumeEntry = config.getVoice().getVolumes().getVolume(sourceLine.getName());
        ConfigEntry<Boolean> muteEntry = config.getVoice().getVolumes().getMute(sourceLine.getName());

        List<Button> buttons = Lists.newArrayList();
        Runnable updateButtons = createMuteButtonAction(buttons, muteEntry);

        addEntry(new SourceLineVolumeEntry<>(
                McTextComponent.translatable(sourceLine.getTranslation()),
                createVolumeSlider(volumeEntry),
                createMuteButton(buttons, updateButtons, muteEntry),
                volumeEntry,
                muteEntry,
                null,
                new ResourceLocation(sourceLine.getIcon()),
                (button, element) -> updateButtons.run()
        ));
    }

    private void createPlayersSearch() {
        TextFieldWidget textField = new TextFieldWidget(
                0,
                0,
                0,
                20,
                McTextComponent.translatable("gui.plasmovoice.volume.players_search").withStyle(McTextStyle.GRAY)
        );

        textField.setResponder((value) -> {
            this.currentSearch = value.toLowerCase();
            refreshPlayerEntries();
        });

        addEntry(new FullWidthEntry<>(
                textField,
                26
        ));
    }

    private void refreshPlayerEntries() {
        List<Entry> entries = this.entries.stream()
                .filter((entry) -> !(entry instanceof PlayerVolumeEntry))
                .collect(Collectors.toList());

        clearEntries();
        entries.forEach(this::addEntry);

        voiceClient.getServerConnection()
                .ifPresent((connection) -> {
                    Map<UUID, McGameProfile> players = Maps.newHashMap();

                    joinMap(players, connection.getPlayers()
                            .stream()
                            .filter(player -> player.getPlayerNick().toLowerCase().contains(currentSearch))
                            .map(VoicePlayerInfo::toGameProfile)
                            .collect(Collectors.toList())
                    );
                    joinMap(players, voiceClient.getSourceLineManager()
                            .getLines()
                            .stream()
                            .map(ClientSourceLine::getPlayers)
                            .filter(Objects::nonNull)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList())
                    );
                    joinMap(players, voiceClient.getSourceManager()
                            .getSources()
                            .stream()
                            .map(ClientAudioSource::getSourceInfo)
                            .filter(sourceInfo -> sourceInfo instanceof DirectSourceInfo)
                            .map(sourceInfo -> ((DirectSourceInfo) sourceInfo).getSender())
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
                    );

                    players.values()
                            .stream()
                            .filter(player -> !Minecraft.getInstance().player.getUUID().equals(player.getId()))
                            .sorted(Comparator.comparing(McGameProfile::getName))
                            .forEach(this::createPlayerVolume);
                });
    }

    private void joinMap(Map<UUID, McGameProfile> map, Collection<McGameProfile> joinCollection) {
        joinCollection.forEach(gameProfile -> {
            McGameProfile mapGameProfile = map.get(gameProfile.getId());

            if (Objects.equals(gameProfile, mapGameProfile) &&
                    gameProfile.getProperties().size() > mapGameProfile.getProperties().size()
            ) {
                map.put(gameProfile.getId(), gameProfile);
                return;
            }

            map.put(gameProfile.getId(), gameProfile);
        });
    }

    private void createPlayerVolume(@NotNull McGameProfile player) {
        DoubleConfigEntry volumeEntry = config.getVoice().getVolumes().getVolume("source_" + player.getId().toString());
        ConfigEntry<Boolean> muteEntry = config.getVoice().getVolumes().getMute("source_" + player.getId().toString());

        List<Button> buttons = Lists.newArrayList();
        Runnable updateButtons = createMuteButtonAction(buttons, muteEntry);

        addEntry(new PlayerVolumeEntry<>(
                createVolumeSlider(volumeEntry),
                createMuteButton(buttons, updateButtons, muteEntry),
                volumeEntry,
                muteEntry,
                null,
                player,
                (button, element) -> updateButtons.run()
        ));
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

    private Runnable createMuteButtonAction(@NotNull List<Button> buttons,
                                            @NotNull ConfigEntry<Boolean> muteEntry) {
        return () -> {
            buttons.get(0).setVisible(!muteEntry.value());
            buttons.get(1).setVisible(muteEntry.value());
        };
    }

    private List<Button> createMuteButton(@NotNull List<Button> buttons,
                                          @NotNull Runnable updateButtons,
                                          @NotNull ConfigEntry<Boolean> muteEntry) {
        Button.OnPress buttonClick = (button) -> {
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
                new ResourceLocation("plasmovoice:textures/icons/speaker_menu.png"),
                true
        );

        IconButton unmuteButton = new IconButton(
                0,
                0,
                20,
                20,
                buttonClick,
                Button.NO_TOOLTIP,
                new ResourceLocation("plasmovoice:textures/icons/speaker_menu_disabled.png"),
                true
        );

        muteButton.setVisible(!muteEntry.value());
        unmuteButton.setVisible(muteEntry.value());

        buttons.add(muteButton);
        buttons.add(unmuteButton);

        return buttons;
    }

    class SourceLineVolumeEntry<W extends GuiAbstractWidget> extends ButtonOptionEntry<W> {

        private final ResourceLocation iconLocation;
        private final ConfigEntry<Boolean> muteEntry;

        public SourceLineVolumeEntry(@NotNull McTextComponent text,
                                     @NotNull W widget,
                                     @NotNull List<Button> buttons,
                                     @NotNull ConfigEntry<?> entry,
                                     @NotNull ConfigEntry<Boolean> muteEntry,
                                     @Nullable McTextComponent tooltip,
                                     @NotNull ResourceLocation iconLocation,
                                     @NotNull OptionResetAction<W> resetAction) {
            super(text, widget, buttons, entry, tooltip, resetAction);

            this.muteEntry = muteEntry;
            this.iconLocation = iconLocation;
        }

        @Override
        protected void renderText(@NotNull PoseStack stack, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta) {
            RenderUtil.bindTexture(0, iconLocation);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

            RenderSystem.enableBlend();
            RenderUtil.blit(stack, x, y + height / 2 - 8, 0, 0, 16, 16, 16, 16);
            RenderSystem.disableBlend();

            RenderUtil.drawString(
                    stack,
                    text,
                    x + 20,
                    y + height / 2 - RenderUtil.getFontHeight() / 2,
                    0xFFFFFF
            );
        }

        @Override
        protected boolean isDefault() {
            return entry.isDefault() && muteEntry.isDefault();
        }

        @Override
        protected void onReset(@NotNull Button button) {
            entry.reset();
            muteEntry.reset();

            if (element instanceof UpdatableWidget)
                ((UpdatableWidget) element).updateValue();

            if (resetAction != null)
                resetAction.onReset(resetButton, element);
        }
    }

    class PlayerVolumeEntry<W extends GuiAbstractWidget> extends ButtonOptionEntry<W> {

        private final McGameProfile player;
        private final ConfigEntry<Boolean> muteEntry;

        public PlayerVolumeEntry(@NotNull W widget,
                                 @NotNull List<Button> buttons,
                                 @NotNull ConfigEntry<?> entry,
                                 @NotNull ConfigEntry<Boolean> muteEntry,
                                 @Nullable McTextComponent tooltip,
                                 @NotNull McGameProfile player,
                                 @NotNull OptionResetAction<W> resetAction) {
            super(McTextComponent.literal(player.getName()), widget, buttons, entry, tooltip, resetAction, 30);

            this.muteEntry = muteEntry;
            this.player = player;
        }

        @Override
        protected void renderText(@NotNull PoseStack stack, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta) {
            RenderUtil.bindTexture(0, loadSkin());
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

            int helmY = y + height / 2 - 12;

            // render helm
            RenderUtil.blit(stack, x, helmY, 24, 24, 8F, 8F, 8, 8, 64, 64);
            RenderSystem.enableBlend();
            RenderUtil.blit(stack, x, helmY, 24, 24, 40F, 8F, 8, 8, 64, 64);
            RenderSystem.disableBlend();

            RenderUtil.drawString(
                    stack,
                    text,
                    x + 30,
                    y + height / 2 - RenderUtil.getFontHeight() / 2,
                    0xFFFFFF
            );
        }

        @Override
        protected boolean isDefault() {
            return entry.isDefault() && muteEntry.isDefault();
        }

        @Override
        protected void onReset(@NotNull Button button) {
            entry.reset();
            muteEntry.reset();

            if (element instanceof UpdatableWidget)
                ((UpdatableWidget) element).updateValue();

            if (resetAction != null)
                resetAction.onReset(resetButton, element);
        }

        private ResourceLocation loadSkin() {
            ModPlayerSkins.loadSkin(player);
            return ModPlayerSkins.getSkin(player.getId(), player.getName());
        }
    }
}
