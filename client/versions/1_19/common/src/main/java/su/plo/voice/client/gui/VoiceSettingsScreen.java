package su.plo.voice.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.tab.ActivationTabWidget;
import su.plo.voice.client.gui.tab.AdvancedTabWidget;
import su.plo.voice.client.gui.tab.DevicesTabWidget;
import su.plo.voice.client.gui.tab.HotKeysTabWidget;

import java.util.ArrayList;
import java.util.List;

public final class VoiceSettingsScreen extends Screen {

    private final PlasmoVoiceClient voiceClient;
    private final ClientConfig config;
    @Getter
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
        this.testController = new MicrophoneTestController(voiceClient, config);
    }

    public int getWidth() {
        return width;
    }

    public Font getFont() {
        return font;
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
                new ActivationTabWidget(minecraft, this, voiceClient, config)
        );
        navigation.addTab(
                Component.translatable("gui.plasmovoice.advanced"),
                new AdvancedTabWidget(minecraft, this, voiceClient, config)
        );
        navigation.addTab(
                Component.translatable("gui.plasmovoice.hotkeys"),
                new HotKeysTabWidget(minecraft, this, voiceClient.getKeyBindings())
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
        testController.stop();

        voiceClient.getEventBus().unregister(voiceClient, testController);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return navigation.keyPressed(keyCode, scanCode, modifiers)
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return navigation.mouseReleased(mouseX, mouseY, button)
                || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float delta) {
        this.tooltip = null;

        super.renderBackground(poseStack);
        navigation.renderTab(poseStack, mouseX, mouseY, delta);
        navigation.renderBackground();
        super.render(poseStack, mouseX, mouseY, delta);

        // render title
        font.drawShadow(poseStack, title, 14, 15, 0xFFFFFF);

        navigation.renderButtons(poseStack, mouseX, mouseY, delta);
//        renderParticles(delta);

        if (tooltip == null && isTitleHovered(mouseX, mouseY)) {
            this.tooltip = GuiUtil.getVersionTooltip(voiceClient);
        }

        if (tooltip != null) {
            renderComponentTooltip(poseStack, tooltip, mouseX, mouseY);
        }
    }
}
