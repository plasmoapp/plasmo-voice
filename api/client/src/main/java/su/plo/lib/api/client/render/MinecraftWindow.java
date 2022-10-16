package su.plo.lib.api.client.render;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface MinecraftWindow {

    void openLink(@NotNull String link, boolean trusted, @NotNull Consumer<Boolean> onConfirm);

    int getWidth();

    int getGuiScaledWidth();

    int getHeight();

    int getGuiScaledHeight();

    double getGuiScale();

    boolean hasControlDown();

    boolean hasShiftDown();

    boolean hasAltDown();

    boolean isSelectAll(int keyCode);

    boolean isCopy(int keyCode);

    boolean isPaste(int keyCode);

    boolean isCut(int keyCode);

    String getClipboard();

    void setClipboard(String clipboard);
}
