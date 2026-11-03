package su.plo.lib.mod.client.gui.components;

import org.jetbrains.annotations.Nullable;
import su.plo.lib.mod.client.render.Colors;
import su.plo.lib.mod.client.render.gui.GuiRenderContext;
import su.plo.slib.api.chat.component.McTextComponent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import java.awt.Color;
import java.util.function.Supplier;

public final class IconButton extends Button {

    private final boolean shadow;
    private final Color shadowColor;
    @Getter
    @Setter
    private Supplier<ResourceLocation> iconLocation;
    @Getter
    @Setter
    private @Nullable Color iconColor = null;

    public IconButton(
            int x,
            int y,
            int width,
            int height,
            @NotNull OnPress pressAction,
            @NotNull OnTooltip tooltipAction,
            @NotNull Supplier<ResourceLocation> iconLocation,
            boolean shadow
    ) {
        this(x, y, width, height, pressAction, tooltipAction, iconLocation, shadow, Colors.WHITE);
    }

    public IconButton(
            int x,
            int y,
            int width,
            int height,
            @NotNull OnPress pressAction,
            @NotNull OnTooltip tooltipAction,
            @NotNull ResourceLocation iconLocation,
            boolean shadow
    ) {
        this(x, y, width, height, pressAction, tooltipAction, () -> iconLocation, shadow, Colors.WHITE);
    }

    public IconButton(
            int x,
            int y,
            int width,
            int height,
            @NotNull OnPress pressAction,
            @NotNull OnTooltip tooltipAction,
            @NotNull Supplier<ResourceLocation> iconLocation,
            boolean shadow,
            Color shadowColor
    ) {
        super(x, y, width, height, McTextComponent.empty(), pressAction, tooltipAction);

        this.iconLocation = iconLocation;
        this.shadow = shadow;
        this.shadowColor = shadowColor;
    }

    @Override
    public void renderButton(@NotNull GuiRenderContext context, int mouseX, int mouseY, float delta) {
        super.renderButton(context, mouseX, mouseY, delta);

        ResourceLocation iconLocation = this.iconLocation.get();

        Color iconColor = this.iconColor;
        if (iconColor == null) {
            iconColor = active ? Colors.WHITE : Colors.GRAY;
        }

        Color shadowColor = null;
        if (hasShadow()) {
            shadowColor = active ? this.shadowColor : Colors.GRAY;
        }

        context.blitIcon(iconLocation, x + 2, y + 2, 16, 16, iconColor, shadowColor, 0, 1);
    }

    public boolean hasShadow() {
        return shadow;
    }
}
