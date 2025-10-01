package su.plo.lib.mod.client.gui.components;

import su.plo.lib.mod.client.render.gui.GuiRenderContext;
import su.plo.slib.api.chat.component.McTextComponent;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.gui.narration.NarrationOutput;

import java.util.function.Consumer;

//#if MC>=12109
//$$ import com.mojang.blaze3d.platform.cursor.CursorTypes;
//#endif

public class Button extends AbstractButton {

    public static final OnTooltip NO_TOOLTIP = (button, render, mouseX, mouseY) -> {
    };
    public static final OnPress NO_ACTION = (button) -> {
    };

    protected final OnPress pressAction;
    protected final OnTooltip tooltipAction;

    public Button(
            int x,
            int y,
            int width,
            int height,
            @NotNull McTextComponent text,
            @NotNull OnPress pressAction,
            @NotNull OnTooltip tooltipAction
    ) {
        super(x, y, width, height, text);

        this.pressAction = pressAction;
        this.tooltipAction = tooltipAction;
    }

    // AbstractButton impl
    @Override
    public void onPress() {
        pressAction.onPress(this);
    }

    // GuiAbstractWidget impl
    @Override
    public void renderButton(@NotNull GuiRenderContext context, int mouseX, int mouseY, float delta) {
        super.renderButton(context, mouseX, mouseY, delta);
        if (isHoveredOrFocused()) {
            renderToolTip(context, mouseX, mouseY);
        }

        //#if MC>=12109
        //$$ if (isHovered()) {
        //$$     context.requestCursor(isActive() ? CursorTypes.POINTING_HAND : CursorTypes.NOT_ALLOWED);
        //$$ }
        //#endif
    }

    @Override
    public void renderToolTip(@NotNull GuiRenderContext context, int mouseX, int mouseY) {
        tooltipAction.onTooltip(this, context, mouseX, mouseY);
    }

    @Override
    public void updateNarration(@NotNull NarrationOutput narrationOutput) {
        super.updateNarration(narrationOutput);
        tooltipAction.narrateTooltip(
                (component) -> narrationOutput.add(NarrationOutput.Type.HINT, component)
        );
    }

    public interface OnTooltip {

        void onTooltip(Button button, GuiRenderContext context, int mouseX, int mouseY);

        default void narrateTooltip(Consumer<McTextComponent> consumer) {
        }
    }

    public interface OnPress {

        void onPress(Button button);
    }
}
