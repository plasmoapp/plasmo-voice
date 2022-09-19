package su.plo.voice.client.gui.settings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.gui.GuiRender;
import su.plo.lib.client.gui.components.Button;
import su.plo.lib.client.gui.components.IconButton;
import su.plo.lib.client.gui.widget.GuiWidgetListener;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.chat.TextComponent;
import su.plo.voice.chat.TextStyle;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.settings.tab.AboutTabWidget;
import su.plo.voice.client.gui.settings.tab.AbstractHotKeysTabWidget;
import su.plo.voice.client.gui.settings.tab.TabWidget;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static su.plo.lib.client.gui.widget.GuiWidget.BACKGROUND_LOCATION;

public final class VoiceSettingsNavigation implements GuiWidgetListener {

    private final MinecraftClientLib minecraft;
    private final PlasmoVoiceClient voiceClient;
    private final VoiceSettingsScreen parent;
    private final ClientConfig config;
    private final Consumer<Integer> onTabChange;

    private final List<Button> disableMicrophoneButtons = Lists.newArrayList();
    private final List<Button> disableVoiceButtons = Lists.newArrayList();
    private final List<TabWidget> tabWidgets = Lists.newArrayList();
    private final List<Button> tabButtons = Lists.newArrayList();
    @Nullable
    private AboutTabWidget aboutTabWidget;

    @Getter
    private int active;

    public VoiceSettingsNavigation(@NotNull MinecraftClientLib minecraft,
                                   @NotNull PlasmoVoiceClient voiceClient,
                                   @NotNull VoiceSettingsScreen parent,
                                   @NotNull ClientConfig config,
                                   int tab,
                                   @NotNull Consumer<Integer> onTabChange) {
        this.minecraft = minecraft;
        this.voiceClient = voiceClient;
        this.parent = parent;
        this.config = config;

        this.active = tab;
        this.onTabChange = onTabChange;
    }

    // GuiWidgetEventListener impl
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 // GLFW_KEY_ESCAPE
                || keyCode == 258) { // GLFW_KEY_TAB
            Optional<TabWidget> tab = getActiveTab();
            if (!tab.isPresent()) return false;

            if (!(tab.get() instanceof AbstractHotKeysTabWidget)) return false;

            return tab.get().keyPressed(keyCode, scanCode, modifiers);
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        getActiveTab().ifPresent(tab -> {
            if (!(tab instanceof AbstractHotKeysTabWidget)) return;
            tab.mouseReleased(mouseX, mouseY, button);
        });

        return false;
    }

    // Class methods
    public void tick() {
        getActiveTab().ifPresent(TabWidget::tick);
    }

    public void init() {
        this.aboutTabWidget = new AboutTabWidget(minecraft, parent, voiceClient, config);

        disableMicrophoneButtons.clear();
        disableVoiceButtons.clear();

        // mute mic
        IconButton disableMicrophoneHide = new IconButton(
                minecraft,
                parent.getWidth() - 52,
                8,
                20,
                20,
                (button) -> {
                    disableMicrophoneButtons.get(0).setVisible(false);
                    disableMicrophoneButtons.get(1).setVisible(true);
                    config.getVoice().getMicrophoneDisabled().set(true);
                },
                (button, render, mouseX, mouseY) -> {
                    parent.setTooltip(ImmutableList.of(
                            TextComponent.translatable("gui.plasmovoice.toggle.microphone"),
                            TextComponent.translatable("gui.plasmovoice.toggle.currently",
                                    TextComponent.translatable("gui.plasmovoice.toggle.enabled").withStyle(TextStyle.GREEN)
                            ).withStyle(TextStyle.GRAY)
                    ));
                },
                "plasmovoice:textures/icons/microphone.png",
                true
        );

        IconButton disableMicrophoneShow = new IconButton(
                minecraft,
                parent.getWidth() - 52,
                8,
                20,
                20,
                (button) -> {
                    disableMicrophoneButtons.get(0).setVisible(true);
                    disableMicrophoneButtons.get(1).setVisible(false);
                    config.getVoice().getMicrophoneDisabled().set(false);

                    if (config.getVoice().getDisabled().value()) {
                        this.disableVoiceButtons.get(0).setVisible(true);
                        this.disableVoiceButtons.get(1).setVisible(false);
                        config.getVoice().getDisabled().set(false);
                    }
                },
                (button, render, mouseX, mouseY) -> {
                    parent.setTooltip(ImmutableList.of(
                            TextComponent.translatable("gui.plasmovoice.toggle.microphone"),
                            TextComponent.translatable("gui.plasmovoice.toggle.currently",
                                    TextComponent.translatable("gui.plasmovoice.toggle.disabled").withStyle(TextStyle.RED)
                            ).withStyle(TextStyle.GRAY)
                    ));
                },
                "plasmovoice:textures/icons/microphone_disabled_v1.png",
                true
        );

        disableMicrophoneHide.setVisible(
                !config.getVoice().getMicrophoneDisabled().value()
                        && !config.getVoice().getDisabled().value()
        );
        disableMicrophoneShow.setVisible(
                config.getVoice().getMicrophoneDisabled().value()
                        || config.getVoice().getDisabled().value()
        );

        // mute speaker
        IconButton disableVoiceHide = new IconButton(
                minecraft,
                parent.getWidth() - 28,
                8,
                20,
                20,
                (button) -> {
                    disableVoiceButtons.get(0).setVisible(false);
                    disableVoiceButtons.get(1).setVisible(true);
                    config.getVoice().getDisabled().set(!config.getVoice().getDisabled().value());

                    // todo: clear sources
//                    SocketClientUDPQueue.closeAll();

                    if (!config.getVoice().getMicrophoneDisabled().value()) {
                        disableMicrophoneButtons.get(0).setVisible(false);
                        disableMicrophoneButtons.get(1).setVisible(true);
                    }
                },
                (button, render, mouseX, mouseY) -> {
                    parent.setTooltip(ImmutableList.of(
                            TextComponent.translatable("gui.plasmovoice.toggle.voice"),
                            TextComponent.translatable("gui.plasmovoice.toggle.currently",
                                    TextComponent.translatable("gui.plasmovoice.toggle.enabled").withStyle(TextStyle.GREEN)
                            ).withStyle(TextStyle.GRAY)
                    ));
                },
                "plasmovoice:textures/icons/speaker.png",
                true
        );

        IconButton disableVoiceShow = new IconButton(
                minecraft,
                parent.getWidth() - 28,
                8,
                20,
                20,
                (button) -> {
                    disableVoiceButtons.get(0).setVisible(true);
                    disableVoiceButtons.get(1).setVisible(false);
                    config.getVoice().getDisabled().set(!config.getVoice().getDisabled().value());

                    if (!config.getVoice().getMicrophoneDisabled().value()) {
                        disableMicrophoneButtons.get(0).setVisible(true);
                        disableMicrophoneButtons.get(1).setVisible(false);
                    }
                },
                (button, matrices, mouseX, mouseY) -> {
                    parent.setTooltip(ImmutableList.of(
                            TextComponent.translatable("gui.plasmovoice.toggle.voice"),
                            TextComponent.translatable("gui.plasmovoice.toggle.currently",
                                    TextComponent.translatable("gui.plasmovoice.toggle.disabled").withStyle(TextStyle.RED)
                            ).withStyle(TextStyle.GRAY)
                    ));
                },
                "plasmovoice:textures/icons/speaker_disabled_v1.png",
                true
        );

        disableVoiceHide.setVisible(!config.getVoice().getDisabled().value());
        disableVoiceShow.setVisible(config.getVoice().getDisabled().value());

        disableMicrophoneButtons.add(disableMicrophoneHide);
        disableMicrophoneButtons.add(disableMicrophoneShow);
        parent.addWidget(disableMicrophoneHide);
        parent.addWidget(disableMicrophoneShow);

        disableVoiceButtons.add(disableVoiceHide);
        disableVoiceButtons.add(disableVoiceShow);
        parent.addWidget(disableVoiceHide);
        parent.addWidget(disableVoiceShow);

        getActiveTab().ifPresent((widget) -> {
            widget.init();
            parent.addWidget(widget);
        });
    }

    public void removed() {
        if (aboutTabWidget != null) aboutTabWidget.removed();
        tabWidgets.forEach(TabWidget::removed);
    }

    public void renderButtons(@NotNull GuiRender render, int mouseX, int mouseY, float delta) {
        int buttonX = 14;
        int buttonY = 36;

        if (!isMinimized()) {
            int buttonsWidth = getButtonsWidth();

            buttonX = (parent.getWidth() / 2) - (buttonsWidth / 2);
            buttonY = 8;
        }

        for (int index = 0; index < tabButtons.size(); index++) {
            Button button = tabButtons.get(index);
            button.setActive(active == -1 || index != active);

            if (buttonX + button.getWidth() > parent.getWidth() - 8) {
                buttonX = 14;
                buttonY += 26;
            }

            button.setX(buttonX);
            buttonX += button.getWidth() + 4;
            button.setY(buttonY);

            render.enableDepthTest();
            render.depthFunc(519);
            button.render(render, mouseX, mouseY, delta);
        }

        for (Button button : disableMicrophoneButtons) {
            button.render(render, mouseX, mouseY, delta);
        }

        for (Button button : disableVoiceButtons) {
            button.render(render, mouseX, mouseY, delta);
        }
    }

    public void renderTab(@NotNull GuiRender render, int mouseX, int mouseY, float delta) {
        getActiveTab().ifPresent(tab -> tab.render(render, mouseX, mouseY, delta));
    }

    public void renderBackground(@NotNull GuiRender render) {
        int width = parent.getWidth();
        int height = getHeight();

        render.setShaderTexture(0, BACKGROUND_LOCATION);
        render.setShaderColor(1F, 1F, 1F, 1F);

        render.blitColor(
                0, width,
                0, height,
                0,
                0, width / 32F,
                0, height / 32F,
                64, 64, 64, 255
        );


        render.depthFunc(515);
        render.disableDepthTest();
        render.enableBlend();
        render.blendFuncSeparate(
                770, // SRC_ALPHA,
                771, // ONE_MINUS_SRC_ALPHA
                0, // ZERO
                1 // ONE
        );
        render.disableTexture();

        render.fillGradient(
                width, height + 4, 0, height,
                0, 0, 0, 0,
                0, 0, 0, 255,
                0,
                false
        );

        render.enableTexture();
        render.enableDepthTest();
        render.defaultBlendFunc();
    }

    public void openTab(int index) {
        getActiveTab().ifPresent((widget) -> {
            widget.removed();
            parent.removeWidget(widget);
        });

        active = index;

        getActiveTab().ifPresent((widget) -> {
            widget.init();
            parent.addWidget(widget);
        });

        onTabChange.accept(index);
    }

    public void addTab(@NotNull TextComponent name, @NotNull TabWidget tabWidget) {
        int elementIndex = tabWidgets.size();
        Button tabButton = new Button(
                minecraft,
                0,
                0,
                minecraft.getFont().width(name) + 16,
                20,
                name,
                (btn) -> {
                    openTab(elementIndex);
                    // todo: disable mic test
                }, Button.NO_TOOLTIP);

        parent.addWidget(tabButton);

        tabWidgets.add(tabWidget);
        tabButtons.add(tabButton);
    }

    public void clearTabs() {
        tabWidgets.clear();
        tabButtons.clear();
    }

    public Optional<TabWidget> getActiveTab() {
        return active < 0
                ? Optional.ofNullable(aboutTabWidget)
                : tabWidgets.size() > 0
                ? Optional.of(tabWidgets.get(active))
                : Optional.empty();
    }

    public int getHeight() {
        if (isMinimized()) {
            return 36 + (getLines() * 28);
        } else {
            return 36;
        }
    }

    public boolean isMinimized() {
        int titleWidth = 14 + minecraft.getFont().width(parent.getTitle()) + 4;

        int buttonsWidth = getButtonsWidth();
        int buttonX = (parent.getWidth() / 2) - (buttonsWidth / 2);

        if (buttonX < titleWidth) return true;

        int disableButtonsWidth = 14 + 48; // 48 = 20 + 4 + 20 + 4

        return (titleWidth + buttonsWidth + disableButtonsWidth) > parent.getWidth();
    }

    public int getLines() {
        int buttonX = 14;
        int lines = 1;

        for (Button button : tabButtons) {
            if (buttonX + button.getWidth() > parent.getWidth() - 8) {
                buttonX = 14;
                lines++;
            }

            buttonX += button.getWidth() + 4;
        }

        return lines;
    }

    public int getButtonsWidth() {
        int width = tabButtons.stream()
                .map(Button::getWidth)
                .reduce(0, Integer::sum);
        width += (tabButtons.size() - 1) * 4;

        return width;
    }
}
