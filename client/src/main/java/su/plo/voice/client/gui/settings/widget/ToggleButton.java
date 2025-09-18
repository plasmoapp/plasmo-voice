package su.plo.voice.client.gui.settings.widget;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.ConfigEntry;
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget;
import su.plo.lib.mod.client.gui.widget.GuiWidgetTexture;
import su.plo.lib.mod.client.render.gui.GuiRenderContext;
import su.plo.slib.api.chat.component.McTextComponent;

//#if MC>=12109
//$$ import com.mojang.blaze3d.platform.cursor.CursorTypes;
//#endif

public final class ToggleButton extends GuiAbstractWidget {

    private static final McTextComponent ON = McTextComponent.translatable("message.plasmovoice.on");
    private static final McTextComponent OFF = McTextComponent.translatable("message.plasmovoice.off");

    private final @Nullable PressAction action;
    private final ConfigEntry<Boolean> entry;

    public ToggleButton(
            @NotNull ConfigEntry<Boolean> entry,
            int x,
            int y,
            int width,
            int height
    ) {
        this(entry, x, y, width, height, null);
    }

    public ToggleButton(@NotNull ConfigEntry<Boolean> entry,
                        int x,
                        int y,
                        int width,
                        int height,
                        @Nullable PressAction action) {
        super(x, y, width, height);

        this.entry = entry;
        this.action = action;
        this.active = !entry.isDisabled();
    }

    @Override
    public McTextComponent getText() {
        return entry.value() ? ON : OFF;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        invertToggle();
    }

    @Override
    protected @NotNull GuiWidgetTexture getButtonTexture(boolean hovered) {
        return GuiWidgetTexture.SLIDER;
    }

    @Override
    public void render(@NotNull GuiRenderContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        //#if MC>=12109
        //$$ if (isHovered()) {
        //$$     context.requestCursor(isActive() ? CursorTypes.POINTING_HAND : CursorTypes.NOT_ALLOWED);
        //$$ }
        //#endif
    }

    @Override
    protected void renderBackground(@NotNull GuiRenderContext context, int mouseX, int mouseY) {
        int x0 = entry.value() ? (x + width - 8) : x;

        GuiWidgetTexture sprite;
        if (isHoveredOrFocused() && active) {
            sprite = GuiWidgetTexture.BUTTON_ACTIVE;
        } else {
            sprite = GuiWidgetTexture.BUTTON_DEFAULT;
        }

        context.blitSprite(sprite, x0, y, 0, 0, 4, 20);
        context.blitSprite(sprite, x0 + 4, y, sprite.getSpriteWidth() - 4, 0, 4, 20);
    }

    public void invertToggle() {
        entry.set(!entry.value());
        if (action != null) action.onToggle(entry.value());
    }

    public interface PressAction {

        void onToggle(boolean toggled);
    }
}
