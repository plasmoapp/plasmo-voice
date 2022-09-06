package su.plo.voice.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.tab.AboutTabWidget;
import su.plo.voice.client.gui.tab.TabWidget;
import su.plo.voice.client.gui.widget.IconButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class VoiceSettingsNavigation {

    private final VoiceSettingsScreen parent;
    private final ClientConfig config;

    private final TabWidget aboutWidget;
    private final List<Button> tabButtons = new ArrayList<>();
    private final List<TabWidget> tabWidgets = new ArrayList<>();

    private int active;

    private List<Button> disableMicrophoneButtons;
    private List<Button> disableVoiceButtons;

    public VoiceSettingsNavigation(Minecraft minecraft, VoiceSettingsScreen parent, ClientConfig config) {
        this.parent = parent;
        this.config = config;
        this.aboutWidget = new AboutTabWidget(minecraft, parent);
    }

    public void addTab(@NotNull Component name, TabWidget tabWidget) {
        int textWidth = parent.getFont().width(name) + 16;
        int elementIndex = tabWidgets.size();
        Button button = new Button(
                0,
                0,
                textWidth,
                20,
                name,
                (btn) -> {
                    System.out.println(elementIndex);
                    active = elementIndex;
                    for (TabWidget widget : tabWidgets) {
                        widget.onClose();
                    }
                    aboutWidget.setScrollAmount(0);

                    getActiveTab().ifPresent(TabWidget::init);

                    // todo: disable mic test
                });

        tabButtons.add(button);
        tabWidgets.add(tabWidget);
    }

    public Optional<TabWidget> getActiveTab() {
        return active < 0
                ? Optional.ofNullable(aboutWidget)
                : tabWidgets.size() > 0
                ? Optional.of(tabWidgets.get(active))
                : Optional.empty();
    }

    public List<? extends GuiEventListener> children() {
        List<GuiEventListener> list = new ArrayList<>(tabButtons);

        if (disableMicrophoneButtons != null) list.addAll(disableMicrophoneButtons);
        if (disableVoiceButtons != null) list.addAll(disableVoiceButtons);

        if (this.tabWidgets.size() > 0) getActiveTab().ifPresent(list::add);

        return list;
    }

    public void clearWidgets() {
        tabButtons.clear();
        tabWidgets.clear();
        this.disableMicrophoneButtons = null;
        this.disableVoiceButtons = null;
    }

    public void removed() {
        aboutWidget.removed();
        tabWidgets.forEach(TabWidget::removed);
    }

    public void init() {
        // mute mic
        IconButton disableMicrophoneHide = new IconButton(
                parent.getWidth() - 52,
                8,
                20,
                20,
                new ResourceLocation("plasmovoice", "textures/icons/microphone.png"),
                button -> {
                    disableMicrophoneButtons.get(0).visible = false;
                    disableMicrophoneButtons.get(1).visible = true;
                    config.getVoice().getMicrophoneDisabled().set(true);
                },
                (button, matrices, mouseX, mouseY) -> {
                    parent.setTooltip(ImmutableList.of(
                            Component.translatable("gui.plasmovoice.toggle.microphone"),
                            Component.translatable("gui.plasmovoice.toggle.currently",
                                    Component.translatable("gui.plasmovoice.toggle.enabled").withStyle(ChatFormatting.GREEN)
                            ).withStyle(ChatFormatting.GRAY)
                    ));
                }
        );

        IconButton disableMicrophoneShow = new IconButton(
                parent.getWidth() - 52,
                8,
                20,
                20,
                new ResourceLocation("plasmovoice", "textures/icons/microphone_disabled_v1.png"),
                button -> {
                    disableMicrophoneButtons.get(0).visible = true;
                    disableMicrophoneButtons.get(1).visible = false;
                    config.getVoice().getMicrophoneDisabled().set(false);

                    if (config.getVoice().getDisabled().value()) {
                        this.disableVoiceButtons.get(0).visible = true;
                        this.disableVoiceButtons.get(1).visible = false;
                        config.getVoice().getDisabled().set(false);
                    }
                },
                (button, matrices, mouseX, mouseY) -> {
                    parent.setTooltip(ImmutableList.of(
                            Component.translatable("gui.plasmovoice.toggle.microphone"),
                            Component.translatable("gui.plasmovoice.toggle.currently",
                                    Component.translatable("gui.plasmovoice.toggle.disabled").withStyle(ChatFormatting.RED)
                            ).withStyle(ChatFormatting.GRAY)
                    ));
                }
        );

        disableMicrophoneHide.visible = !config.getVoice().getMicrophoneDisabled().value()
                && !config.getVoice().getDisabled().value();
        disableMicrophoneShow.visible = config.getVoice().getMicrophoneDisabled().value()
                || config.getVoice().getDisabled().value();

        // mute speaker
        IconButton disableVoiceHide = new IconButton(
                parent.getWidth() - 28,
                8,
                20,
                20,
                new ResourceLocation("plasmovoice", "textures/icons/speaker.png"),
                button -> {
                    disableVoiceButtons.get(0).visible = false;
                    disableVoiceButtons.get(1).visible = true;
                    config.getVoice().getDisabled().set(!config.getVoice().getDisabled().value());

                    // todo: clear sources
//                    SocketClientUDPQueue.closeAll();

                    if (!config.getVoice().getMicrophoneDisabled().value()) {
                        disableMicrophoneButtons.get(0).visible = false;
                        disableMicrophoneButtons.get(1).visible = true;
                    }
                },
                (button, matrices, mouseX, mouseY) -> {
                    parent.setTooltip(ImmutableList.of(
                            Component.translatable("gui.plasmovoice.toggle.voice"),
                            Component.translatable("gui.plasmovoice.toggle.currently",
                                    Component.translatable("gui.plasmovoice.toggle.enabled").withStyle(ChatFormatting.GREEN)
                            ).withStyle(ChatFormatting.GRAY)
                    ));
                }
        );

        IconButton disableVoiceShow = new IconButton(
                parent.getWidth() - 28,
                8,
                20,
                20,
                new ResourceLocation("plasmovoice", "textures/icons/speaker_disabled_v1.png"),
                button -> {
                    disableVoiceButtons.get(0).visible = true;
                    disableVoiceButtons.get(1).visible = false;
                    config.getVoice().getDisabled().set(!config.getVoice().getDisabled().value());

                    if (!config.getVoice().getMicrophoneDisabled().value()) {
                        disableMicrophoneButtons.get(0).visible = true;
                        disableMicrophoneButtons.get(1).visible = false;
                    }
                },
                (button, matrices, mouseX, mouseY) -> {
                    parent.setTooltip(ImmutableList.of(
                            Component.translatable("gui.plasmovoice.toggle.voice"),
                            Component.translatable("gui.plasmovoice.toggle.currently",
                                    Component.translatable("gui.plasmovoice.toggle.disabled").withStyle(ChatFormatting.RED)
                            ).withStyle(ChatFormatting.GRAY)
                    ));
                }
        );

        disableVoiceHide.visible = !config.getVoice().getDisabled().value();
        disableVoiceShow.visible = config.getVoice().getDisabled().value();

        this.disableMicrophoneButtons = ImmutableList.of(disableMicrophoneHide, disableMicrophoneShow);
        this.disableVoiceButtons = ImmutableList.of(disableVoiceHide, disableVoiceShow);

        // init active tab
        getActiveTab().ifPresent(TabWidget::init);
    }

    public void renderButtons(@NotNull PoseStack poseStack, int mouseX, int mouseY, float delta) {
        // render tabs buttons
        int buttonX = 14;
        int buttonY = 36;

        if (parent.isHeaderMinimized()) {
            int buttonsWidth = (tabButtons.size() - 1) * 4;
            for (Button button : tabButtons) {
                buttonsWidth += button.getWidth();
            }

            buttonX = (parent.getWidth() / 2) - (buttonsWidth / 2);
            buttonY = 8;
        }

        for (int i = 0; i < tabButtons.size(); i++) {
            Button button = tabButtons.get(i);
            button.active = active == -1 || i != active;

            button.x = buttonX;
            buttonX += button.getWidth() + 4;
            button.y = buttonY;

            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(519);
            button.render(poseStack, mouseX, mouseY, delta);
        }

        for (Button button : this.disableMicrophoneButtons) {
            button.render(poseStack, mouseX, mouseY, delta);
        }

        for (Button button : this.disableVoiceButtons) {
            button.render(poseStack, mouseX, mouseY, delta);
        }
    }

    public void renderTab(@NotNull PoseStack poseStack, int mouseX, int mouseY, float delta) {
        getActiveTab().ifPresent(tab -> tab.render(poseStack, mouseX, mouseY, delta));
    }
}
