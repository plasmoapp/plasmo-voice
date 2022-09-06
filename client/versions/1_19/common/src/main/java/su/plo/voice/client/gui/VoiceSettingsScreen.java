package su.plo.voice.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.tab.ActivationTabWidget;
import su.plo.voice.client.gui.tab.DevicesTabWidget;

import java.util.ArrayList;
import java.util.List;

public final class VoiceSettingsScreen extends Screen {

    public static final int MIN_WIDTH = 640;

    private final PlasmoVoiceClient voiceClient;
    private final ClientConfig config;
    private final VoiceSettingsNavigation navigation;
    private final MicrophoneTestController testController;

    private int titleWidth;
    @Setter
    private List<Component> tooltip;

    public VoiceSettingsScreen(Minecraft minecraft, PlasmoVoiceClient voiceClient, ClientConfig config) {
        super(GuiUtil.getSettingsTitle(voiceClient));
        this.voiceClient = voiceClient;
        this.config = config;
        this.navigation = new VoiceSettingsNavigation(minecraft, this, config);
        this.testController = new MicrophoneTestController(voiceClient);
    }

    public int getHeaderHeight() {
        if (isHeaderMinimized()) {
            return 64;
        } else {
            return 36;
        }
    }

    public int getWidth() {
        return width;
    }

    public Font getFont() {
        return font;
    }

    // todo: название метода уебщиное
    public boolean isHeaderMinimized() {
        return false;
//        return width < MIN_WIDTH;
    }

    public boolean isTitleHovered(int mouseX, int mouseY) {
        return mouseX >= 14 && mouseX <= (14 + titleWidth) &&
                mouseY >= 15 && mouseY <= (15 + font.lineHeight);
    }

    @Override
    protected void init() {
        voiceClient.getEventBus().unregister(voiceClient, testController);
        voiceClient.getEventBus().register(voiceClient, testController);

        this.titleWidth = font.width(title);
        clearWidgets();

        navigation.addTab(
                Component.translatable("gui.plasmovoice.devices"),
                new DevicesTabWidget(
                        minecraft,
                        this,
                        testController,
                        voiceClient,
                        config
                )
        );
        navigation.addTab(
                Component.translatable("gui.plasmovoice.activation"),
                new ActivationTabWidget(minecraft, this, voiceClient.getAudioCapture())
        );

        navigation.init();
    }

    @Override
    public List<? extends GuiEventListener> children() {
        List<GuiEventListener> list = new ArrayList<>(super.children());
        list.addAll(navigation.children());

        return list;
    }

    @Override
    protected void clearWidgets() {
        super.clearWidgets();
        navigation.clearWidgets();
    }

    @Override
    public void removed() {
        super.removed();
        navigation.removed();

        voiceClient.getEventBus().unregister(voiceClient, testController);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float delta) {
        this.tooltip = null;

        super.renderBackground(poseStack);
        navigation.renderTab(poseStack, mouseX, mouseY, delta);
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);

        // render title
        font.drawShadow(poseStack, title, 14, 15, 0xFFFFFF);

        navigation.renderButtons(poseStack, mouseX, mouseY, delta);
//        renderParticles(delta);

        if (tooltip == null && isTitleHovered(mouseX, mouseY)) {
            this.tooltip = GuiUtil.getVersionTooltip(voiceClient);
        }

        if (tooltip != null) {
            renderComponentTooltip(poseStack, this.tooltip, mouseX, mouseY);
        }
    }

    @Override
    public void renderBackground(@NotNull PoseStack poseStack) {
        int height = getHeaderHeight();

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
                .vertex(this.width, height, 0.0D)
                .uv((float) this.width / 32.0F, (float) height / 32.0F + 0)
                .color(64, 64, 64, 255)
                .endVertex();
        bufferBuilder
                .vertex(this.width, 0.0D, 0.0D)
                .uv((float) this.width / 32.0F, 0)
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
                .vertex(this.width, height + 4, 0.0D)
                .color(0, 0, 0, 0)
                .endVertex();
        bufferBuilder
                .vertex(this.width, height, 0.0D)
                .color(0, 0, 0, 255)
                .endVertex();
        bufferBuilder
                .vertex(0, height, 0.0D)
                .color(0, 0, 0, 255)
                .endVertex();
        tesselator.end();
    }
}
