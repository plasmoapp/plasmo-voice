package su.plo.voice.client.gui.widgets;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.gui.tabs.KeyBindingsTabWidget;
import su.plo.voice.client.utils.TextUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class KeyBindWidget extends Button {
    private final KeyBindingsTabWidget parent;
    private final VoiceClientConfig.KeyBindingConfigEntry entry;
    private final List<InputConstants.Key> pressedKeys = new ArrayList<>();

    public KeyBindWidget(KeyBindingsTabWidget parent, int x, int y, int width, int height, VoiceClientConfig.KeyBindingConfigEntry entry) {
        super(x, y, width, height, TextComponent.EMPTY, button -> {});
        this.parent = parent;
        this.entry = entry;

        updateValue();
    }

    public boolean isActive() {
        return parent.getFocusedBinding() != null && parent.getFocusedBinding().equals(this);
    }

    public void updateValue() {
        TextComponent text = new TextComponent("");
        if (entry.get().getKeys().size() == 0) {
            text.append(new TranslatableComponent("gui.none"));
        } else {
            for (int i = 0; i < entry.get().getKeys().size(); i ++) {
                InputConstants.Key key = entry.get().getKeys().get(i);
                text.append(key.getDisplayName());
                if (i != entry.get().getKeys().size() - 1) {
                    text.append(new TextComponent(" + "));
                }
            }
        }

        if (isActive()) {
            if (pressedKeys.size() > 0) {
                text = new TextComponent("");
                List<InputConstants.Key> sorted = pressedKeys.stream()
                        .sorted(Comparator.comparingInt(key -> key.getType().ordinal()))
                        .collect(Collectors.toList());
                for (int i = 0; i < sorted.size(); i++) {
                    InputConstants.Key pressedKey = sorted.get(i);
                    text.append(pressedKey.getDisplayName());
                    if (i != sorted.size() - 1) {
                        text.append(new TextComponent(" + "));
                    }
                }
            }

            this.setMessage((new TextComponent("> ")).append(text.withStyle(ChatFormatting.YELLOW)).append(" <").withStyle(ChatFormatting.YELLOW));
        } else {
            this.setMessage(text);
        }
    }

    public void keysReleased() {
        entry.get().setKeys(ImmutableList.copyOf(pressedKeys));
        pressedKeys.clear();
        parent.setFocusedBinding(null);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isActive() && !(button == GLFW.GLFW_MOUSE_BUTTON_1 && pressedKeys.size() == 0) &&
                pressedKeys.stream().anyMatch(key -> key.getType().equals(InputConstants.Type.MOUSE) && key.getValue() == button)) {
            keysReleased();
            updateValue();
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isActive()) {
            if (pressedKeys.size() < 3) {
                pressedKeys.add(InputConstants.Type.MOUSE.getOrCreate(button));
            }
            updateValue();
            return true;
        } else if (this.clicked(mouseX, mouseY)) {
            parent.setFocusedBinding(this);
            updateValue();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isActive()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                if (pressedKeys.size() > 0) {
                    keysReleased();
                } else {
                    parent.setFocusedBinding(null);
                    entry.get().setKeys(ImmutableList.of());
                    updateValue();
                }
                return true;
            }

            if (pressedKeys.size() < 3) {
                pressedKeys.add(InputConstants.Type.KEYSYM.getOrCreate(keyCode));
            }
            updateValue();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (isActive() &&
                pressedKeys.stream().anyMatch(key -> key.getType().equals(InputConstants.Type.KEYSYM) && key.getValue() == keyCode)) {
            keysReleased();
            updateValue();
            return true;
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderToolTip(PoseStack matrices, int mouseX, int mouseY) {
        if (parent.getFocusedBinding() == null || !parent.getFocusedBinding().equals(this)) {
            int width = Minecraft.getInstance().font.width(getMessage());
            if (width > this.width - 16) {
                parent.setTooltip(ImmutableList.of(getMessage()));
            }
        }
        super.renderToolTip(matrices, mouseX, mouseY);
    }

    @Override
    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
        Minecraft minecraftClient = Minecraft.getInstance();
        Font textRenderer = minecraftClient.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        blit(matrices, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        blit(matrices, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
        this.renderBg(matrices, minecraftClient, mouseX, mouseY);
        int j = this.active ? 16777215 : 10526880;

        if (parent.getFocusedBinding() != null && parent.getFocusedBinding().equals(this)) {
            drawCenteredString(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
        } else {
            FormattedCharSequence orderedText = TextUtils.getOrderedText(textRenderer, getMessage(), this.width - 16);
            textRenderer.drawShadow(matrices, orderedText, (float)((this.x + this.width / 2) - textRenderer.width(orderedText) / 2), this.y + (this.height - 8) / 2,
                    j | Mth.ceil(this.alpha * 255.0F) << 24);
        }

        if (this.isHovered()) {
            this.renderToolTip(matrices, mouseX, mouseY);
        }
    }
}
