package su.plo.lib.mod.client.gui.screen;

import org.jetbrains.annotations.Nullable;
import su.plo.lib.mod.client.gui.widget.GuiWidgetListener;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public interface GuiScreenListener extends GuiWidgetListener {

    List<? extends GuiWidgetListener> widgets();

    default Optional<? extends GuiWidgetListener> getWidgetAt(double mouseX, double mouseY) {
        return widgets()
                .stream()
                .filter(widget -> widget.isMouseOver(mouseX, mouseY))
                .findFirst();
    }

    @Override
    default boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (GuiWidgetListener widget : widgets()) {
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                setFocused(widget);
                if (button == 0) setDragging(true);
                return true;
            }
        }

        return false;
    }

    @Override
    default boolean mouseReleased(double mouseX, double mouseY, int button) {
        setDragging(false);

        if (getFocused() != null && getFocused().mouseReleased(mouseX, mouseY, button)) {
            return true;
        }

        return getWidgetAt(mouseX, mouseY)
                .filter((element) ->
                        element.mouseReleased(mouseX, mouseY, button)
                )
                .isPresent();
    }

    @Override
    default boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return getWidgetAt(mouseX, mouseY)
                .filter((guiEventListener) ->
                        guiEventListener.mouseScrolled(mouseX, mouseY, delta)
                )
                .isPresent();
    }

    @Override
    default boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return getFocused() != null &&
                isDragging() &&
                button == 0 &&
                getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    default boolean keyPressed(int keyCode, int modifiers) {
        return getFocused() != null && getFocused().keyPressed(keyCode, modifiers);
    }

    @Override
    default boolean charTyped(char typedChar, int modifiers) {
        return getFocused() != null && getFocused().charTyped(typedChar, modifiers);
    }

    @Override
    default boolean keyReleased(int keyCode, char typedChar, int modifiers) {
        return getFocused() != null && getFocused().keyReleased(keyCode, typedChar, modifiers);
    }

    default boolean changeFocus(boolean lookForwards) {
        GuiWidgetListener focused = getFocused();
        if (focused != null && focused.changeFocus(lookForwards)) return true;

        List<? extends GuiWidgetListener> widgets = widgets();
        int indexOfFocused = widgets.indexOf(focused);

        int nextIndex;
        if (focused != null && indexOfFocused >= 0) {
            nextIndex = indexOfFocused + (lookForwards ? 1 : 0);
        } else if (lookForwards) {
            nextIndex = 0;
        } else {
            nextIndex = widgets.size();
        }

        ListIterator<? extends GuiWidgetListener> nextIterator = widgets.listIterator(nextIndex);
        BooleanSupplier nextCheck = lookForwards ? nextIterator::hasNext : nextIterator::hasPrevious;
        Supplier<? extends GuiWidgetListener> nextSupplier = lookForwards ? nextIterator::next : nextIterator::previous;

        while (nextCheck.getAsBoolean()) {
            GuiWidgetListener nextFocused = nextSupplier.get();
            if (nextFocused.changeFocus(lookForwards)) {
                setFocused(nextFocused);
                return true;
            }
        }

        setFocused(null);
        return false;
    }

    boolean isDragging();

    void setDragging(boolean dragging);

    @Nullable GuiWidgetListener getFocused();

    void setFocused(@Nullable GuiWidgetListener focused);
}
