package su.plo.lib.client.gui.widget;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.gui.narration.NarrationOutput;

public interface GuiNarrationWidget {

    void updateNarration(@NotNull NarrationOutput narrationOutput);

    NarrationPriority narrationPriority();

    default boolean isActive() {
        return true;
    }

    enum NarrationPriority {

        NONE,
        HOVERED,
        FOCUSED;

        NarrationPriority() {
        }

        public boolean isTerminal() {
            return this == FOCUSED;
        }
    }
}
