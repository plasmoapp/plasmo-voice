package su.plo.voice.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.tab.AboutTabWidget;
import su.plo.voice.client.gui.tab.KeyBindingTabWidget;
import su.plo.voice.client.gui.tab.TabWidget;
import su.plo.voice.client.gui.widget.IconButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.minecraft.client.gui.GuiComponent.BACKGROUND_LOCATION;

public final class VoiceSettingsNavigation {

    private final Minecraft minecraft;
    private final VoiceSettingsScreen parent;
    private final ClientConfig config;

    private final TabWidget aboutWidget;
    private final List<Button> tabButtons = new ArrayList<>();
    private final List<TabWidget> tabWidgets = new ArrayList<>();

    private int active;

    private List<Button> disableMicrophoneButtons;
    private List<Button> disableVoiceButtons;

    public VoiceSettingsNavigation(Minecraft minecraft, VoiceSettingsScreen parent, ClientConfig config) {
        this.minecraft = minecraft;
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
                ? Optional.of(aboutWidget)
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
        int titleWidth = 14 + minecraft.font.width(parent.getTitle()) + 4;

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
            if (buttonX + button.getWidth() > parent.getWidth() - 14) {
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

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_TAB) {
            Optional<TabWidget> tab = getActiveTab();
            if (tab.isEmpty()) return false;

            if (!(tab.get() instanceof KeyBindingTabWidget)) return false;

            return tab.get().keyPressed(keyCode, scanCode, modifiers);
        }

        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        getActiveTab().ifPresent(tab -> {
            if (!(tab instanceof KeyBindingTabWidget)) return;
            tab.mouseReleased(mouseX, mouseY, button);
        });

        return false;
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

        if (!isMinimized()) {
            int buttonsWidth = getButtonsWidth();

            buttonX = (parent.getWidth() / 2) - (buttonsWidth / 2);
            buttonY = 8;
        }

        for (int i = 0; i < tabButtons.size(); i++) {
            Button button = tabButtons.get(i);
            button.active = active == -1 || i != active;

            if (buttonX + button.getWidth() > parent.getWidth() - 14) {
                buttonX = 14;
                buttonY += 26;
            }

            button.x = buttonX;
            buttonX += button.getWidth() + 4;
            button.y = buttonY;

            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(519);
            button.render(poseStack, mouseX, mouseY, delta);
        }

        for (Button button : disableMicrophoneButtons) {
            button.render(poseStack, mouseX, mouseY, delta);
        }

        for (Button button : disableVoiceButtons) {
            button.render(poseStack, mouseX, mouseY, delta);
        }
    }

    public void renderBackground() {
        int width = parent.getWidth();
        int height = getHeight();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder
                .vertex(0.0D, height, 0.0D)
                .uv(0.0F, (float) height / 32.0F + 0)
                .color(64, 64, 64, 255)
                .endVertex();
        bufferBuilder
                .vertex(width, height, 0.0D)
                .uv((float) width / 32.0F, (float) height / 32.0F + 0)
                .color(64, 64, 64, 255)
                .endVertex();
        bufferBuilder
                .vertex(width, 0.0D, 0.0D)
                .uv((float) width / 32.0F, 0)
                .color(64, 64, 64, 255)
                .endVertex();
        bufferBuilder
                .vertex(0.0D, 0.0D, 0.0D)
                .uv(0.0F, 0)
                .color(64, 64, 64, 255)
                .endVertex();
        tesselator.end();


        RenderSystem.depthFunc(515);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder
                .vertex(0, height + 4, 0.0D)
                .color(0, 0, 0, 0)
                .endVertex();
        bufferBuilder
                .vertex(width, height + 4, 0.0D)
                .color(0, 0, 0, 0)
                .endVertex();
        bufferBuilder
                .vertex(width, height, 0.0D)
                .color(0, 0, 0, 255)
                .endVertex();
        bufferBuilder
                .vertex(0, height, 0.0D)
                .color(0, 0, 0, 255)
                .endVertex();
        tesselator.end();
    }

    public void renderTab(@NotNull PoseStack poseStack, int mouseX, int mouseY, float delta) {
        getActiveTab().ifPresent(tab -> tab.render(poseStack, mouseX, mouseY, delta));
    }
}
