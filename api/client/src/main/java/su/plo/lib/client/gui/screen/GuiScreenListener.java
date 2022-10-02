package su.plo.lib.client.gui.screen;

import org.jetbrains.annotations.Nullable;
import su.plo.lib.client.gui.widget.GuiWidgetListener;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public interface GuiScreenListener extends GuiWidgetListener {

    List<? extends GuiWidgetListener> widgets();

    default Optional<GuiWidgetListener> getWidgetAt(double mouseX, double mouseY) {
        Iterator<? extends GuiWidgetListener> iter = widgets().iterator();

        GuiWidgetListener guiEventListener;
        do {
            if (!iter.hasNext()) return Optional.empty();

            guiEventListener = iter.next();
        } while (!guiEventListener.isMouseOver(mouseX, mouseY));

        return Optional.of(guiEventListener);
    }

    @Override
    default boolean mouseClicked(double mouseX, double mouseY, int button) {
        Iterator<? extends GuiWidgetListener> iter = this.widgets().iterator();

        GuiWidgetListener widget;
        do {
            if (!iter.hasNext()) return false;
            widget = iter.next();
        } while (!widget.mouseClicked(mouseX, mouseY, button));

        setFocused(widget);
        if (button == 0) setDragging(true);

        return true;
    }

    @Override
    default boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.setDragging(false);

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
    default boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return getFocused() != null && getFocused().keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    default boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return getFocused() != null && getFocused().keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    default boolean charTyped(char c, int i) {
        return getFocused() != null && getFocused().charTyped(c, i);
    }

    @Override
    default boolean changeFocus(boolean lookForwards) {
        GuiWidgetListener guiEventListener = getFocused();

        boolean bl2 = guiEventListener != null;
        if (!bl2 || !guiEventListener.changeFocus(lookForwards)) {
            List<? extends GuiWidgetListener> list = widgets();
            int i = list.indexOf(guiEventListener);
            int j;
            if (bl2 && i >= 0) {
                j = i + (lookForwards ? 1 : 0);
            } else if (lookForwards) {
                j = 0;
            } else {
                j = list.size();
            }

            ListIterator<? extends GuiWidgetListener> listIterator = list.listIterator(j);
            BooleanSupplier var10000;
            if (lookForwards) {
                Objects.requireNonNull(listIterator);
                var10000 = listIterator::hasNext;
            } else {
                Objects.requireNonNull(listIterator);
                var10000 = listIterator::hasPrevious;
            }

            BooleanSupplier booleanSupplier = var10000;
            Supplier<? extends GuiWidgetListener> var11;
            if (lookForwards) {
                Objects.requireNonNull(listIterator);
                var11 = listIterator::next;
            } else {
                Objects.requireNonNull(listIterator);
                var11 = listIterator::previous;
            }

            Supplier<? extends GuiWidgetListener> supplier = var11;

            GuiWidgetListener guiEventListener2;
            do {
                if (!booleanSupplier.getAsBoolean()) {
                    this.setFocused(null);
                    return false;
                }

                guiEventListener2 = supplier.get();
            } while (!guiEventListener2.changeFocus(lookForwards));

            this.setFocused(guiEventListener2);
        }
        return true;
    }

    boolean isDragging();

    void setDragging(boolean dragging);

    @Nullable GuiWidgetListener getFocused();

    void setFocused(@Nullable GuiWidgetListener focused);
}
