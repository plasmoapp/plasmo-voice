package su.plo.voice.client.gui.settings.widget;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.ConfigEntry;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.lib.api.client.gui.GuiRender;
import su.plo.lib.api.client.gui.widget.GuiAbstractWidget;

public final class ToggleButton extends GuiAbstractWidget {

    private static final MinecraftTextComponent ON = MinecraftTextComponent.translatable("message.plasmovoice.on");
    private static final MinecraftTextComponent OFF = MinecraftTextComponent.translatable("message.plasmovoice.off");

    private final @Nullable PressAction action;
    private final ConfigEntry<Boolean> entry;

    public ToggleButton(@NotNull MinecraftClientLib minecraft,
                        @NotNull ConfigEntry<Boolean> entry,
                        int x,
                        int y,
                        int width,
                        int height) {
        this(minecraft, entry, x, y, width, height, null);
    }

    public ToggleButton(@NotNull MinecraftClientLib minecraft,
                        @NotNull ConfigEntry<Boolean> entry,
                        int x,
                        int y,
                        int width,
                        int height,
                        @Nullable PressAction action) {
        super(minecraft, x, y, width, height);

        this.entry = entry;
        this.action = action;
    }

    @Override
    public MinecraftTextComponent getText() {
        return entry.value() ? ON : OFF;
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
    protected void renderBackground(@NotNull GuiRender render, int mouseX, int mouseY) {
        render.setShaderTexture(0, WIDGETS_LOCATION);
        int i = (isHoveredOrFocused() && active ? 2 : 1) * 20;
        if (entry.value()) {
            render.blit(x + (int)((double)(width - 8)), y, 0, 46 + i, 4, 20);
            render.blit(x + (int)((double)(width - 8)) + 4, y, 196, 46 + i, 4, 20);
        } else {
            render.blit(x, y, 0, 46 + i, 4, 20);
            render.blit(x + 4, y, 196, 46 + i, 4, 20);
        }
    }

    public void invertToggle() {
        entry.set(!entry.value());
        if (action != null) action.onToggle(entry.value());
    }

    public interface PressAction {

        void onToggle(boolean toggled);
    }
}
