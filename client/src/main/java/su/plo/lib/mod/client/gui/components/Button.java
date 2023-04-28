package su.plo.lib.mod.client.gui.components;

import su.plo.voice.universal.UMatrixStack;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.mod.client.gui.narration.NarrationOutput;

import java.util.function.Consumer;

public class Button extends AbstractButton {

    public static final OnTooltip NO_TOOLTIP = (button, render, mouseX, mouseY) -> {
    };
    public static final OnPress NO_ACTION = (button) -> {
    };

    protected final OnPress pressAction;
    protected final OnTooltip tooltipAction;

    public Button(int x,
                  int y,
                  int width,
                  int height,
                  @NotNull MinecraftTextComponent text,
                  @NotNull OnPress pressAction,
                  @NotNull OnTooltip tooltipAction) {
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
    public void renderButton(@NotNull UMatrixStack stack, int mouseX, int mouseY, float delta) {
        super.renderButton(stack, mouseX, mouseY, delta);
        if (isHoveredOrFocused()) {
            renderToolTip(stack, mouseX, mouseY);
        }
    }

    @Override
    public void renderToolTip(@NotNull UMatrixStack stack, int mouseX, int mouseY) {
        tooltipAction.onTooltip(this, stack, mouseX, mouseY);
    }

    @Override
    public void updateNarration(@NotNull NarrationOutput narrationOutput) {
        super.updateNarration(narrationOutput);
        tooltipAction.narrateTooltip(
                (component) -> narrationOutput.add(NarrationOutput.Type.HINT, component)
        );
    }

    public interface OnTooltip {

        void onTooltip(Button button, UMatrixStack stack, int mouseX, int mouseY);

        default void narrateTooltip(Consumer<MinecraftTextComponent> consumer) {
        }
    }

    public interface OnPress {

        void onPress(Button button);
    }
}
