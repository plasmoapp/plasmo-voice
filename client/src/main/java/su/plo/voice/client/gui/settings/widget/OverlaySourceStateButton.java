package su.plo.voice.client.gui.settings.widget;

import gg.essential.universal.UGraphics;
import gg.essential.universal.UMatrixStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.voice.client.config.overlay.OverlaySourceState;

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
    protected int getYImage(boolean hovered) {
        return 0;
    }

    @Override
    protected void renderBackground(@NotNull UMatrixStack stack, int mouseX, int mouseY) {
        UGraphics.bindTexture(0, WIDGETS_LOCATION);
        int i = (isHoveredOrFocused() && active ? 2 : 1) * 20;
        if (entry.value() == OverlaySourceState.ON) {
            RenderUtil.blit(stack, x + (int) ((double) (width - 8)), y, 0, 46 + i, 4, 20);
            RenderUtil.blit(stack, x + (int) ((double) (width - 8)) + 4, y, 196, 46 + i, 4, 20);
        } else {
            RenderUtil.blit(stack, x, y, 0, 46 + i, 4, 20);
            RenderUtil.blit(stack, x + 4, y, 196, 46 + i, 4, 20);
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
