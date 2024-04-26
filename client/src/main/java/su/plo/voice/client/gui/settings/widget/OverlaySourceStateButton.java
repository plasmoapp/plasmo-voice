package su.plo.voice.client.gui.settings.widget;

import su.plo.lib.mod.client.gui.widget.GuiWidgetTexture;
import gg.essential.universal.UGraphics;
import gg.essential.universal.UMatrixStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.voice.api.client.config.overlay.OverlaySourceState;

public final class OverlaySourceStateButton extends GuiAbstractWidget {

    private static final MinecraftTextComponent ON = MinecraftTextComponent.translatable("message.plasmovoice.on");
    private static final MinecraftTextComponent OFF = MinecraftTextComponent.translatable("message.plasmovoice.off");

    private final @Nullable PressAction action;
    private final EnumConfigEntry<OverlaySourceState> entry;

    public OverlaySourceStateButton(EnumConfigEntry<OverlaySourceState> entry,
                                    int x,
                                    int y,
                                    int width,
                                    int height) {
        this(entry, x, y, width, height, null);
    }

    public OverlaySourceStateButton(@NotNull EnumConfigEntry<OverlaySourceState> entry,
                                    int x,
                                    int y,
                                    int width,
                                    int height,
                                    @Nullable PressAction action) {
        super(x, y, width, height);

        this.entry = entry;
        this.action = action;
    }

    @Override
    public MinecraftTextComponent getText() {
        return entry.value() == OverlaySourceState.ON ? ON : OFF;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        invertToggle();
    }

    @Override
    protected @NotNull GuiWidgetTexture getButtonTexture(boolean hovered) {
        return GuiWidgetTexture.BUTTON_DISABLED;
    }

    @Override
    protected void renderBackground(@NotNull UMatrixStack stack, int mouseX, int mouseY) {
        GuiWidgetTexture sprite;
        if (isHoveredOrFocused() && active) {
            sprite = GuiWidgetTexture.BUTTON_ACTIVE;
        } else {
            sprite = GuiWidgetTexture.BUTTON_DEFAULT;
        }

        UGraphics.bindTexture(0, sprite.getLocation());
        if (entry.value() == OverlaySourceState.ON) {
            int x0 = x + (int) ((double) (width - 8));
            RenderUtil.blitSprite(stack, sprite, x0, y, 0, 0, 4, 20);
            RenderUtil.blitSprite(stack, sprite, x0 + 4, y, sprite.getSpriteWidth() - 4, 0, 4, 20);
        } else {
            RenderUtil.blitSprite(stack, sprite, x, y, 0, 0, 4, 20);
            RenderUtil.blitSprite(stack, sprite, x + 4, y, sprite.getSpriteWidth() - 4, 0, 4, 20);
        }
    }

    public void invertToggle() {
        entry.set(entry.value() == OverlaySourceState.OFF
                ? OverlaySourceState.ON
                : OverlaySourceState.OFF
        );
        if (action != null) action.onToggle(entry.value());
    }

    public interface PressAction {

        void onToggle(OverlaySourceState state);
    }
}
