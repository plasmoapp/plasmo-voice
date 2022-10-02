package su.plo.lib.client.gui;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.gui.screen.GuiScreen;

import java.util.Optional;

@RequiredArgsConstructor
public final class ScreenContainer {

    private final Object screen;

    public <T> @NotNull T get() {
        return (T) screen;
    }

    public <T extends GuiScreen> Optional<T> getLibScreen() {
        if (!isLibScreen()) return Optional.empty();

        return Optional.of((T) screen);
    }

    /**
     * Checks if screen was created with pv lib
     *
     * @return true if screen was created with pv lib
     */
    private boolean isLibScreen() {
        return screen instanceof GuiScreen;
    }
}
