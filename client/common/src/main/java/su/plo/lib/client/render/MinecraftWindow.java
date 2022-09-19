package su.plo.lib.client.render;

public interface MinecraftWindow {

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
