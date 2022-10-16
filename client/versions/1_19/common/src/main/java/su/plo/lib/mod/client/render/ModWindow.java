package su.plo.lib.mod.client.render;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.render.MinecraftWindow;

import java.util.function.Consumer;

public final class ModWindow implements MinecraftWindow {

    private final Minecraft minecraft = Minecraft.getInstance();

    @Override
    public void openLink(@NotNull String link, boolean trusted, @NotNull Consumer<Boolean> onConfirm) {
        minecraft.setScreen(new ConfirmLinkScreen(
                        (ok) -> {
                            if (ok) Util.getPlatform().openUri(link);
                            onConfirm.accept(ok);
                        },
                        link,
                        true
                )
        );
    }

    @Override
    public int getWidth() {
        return minecraft.getWindow().getWidth();
    }

    @Override
    public int getGuiScaledWidth() {
        return minecraft.getWindow().getGuiScaledWidth();
    }

    @Override
    public int getHeight() {
        return minecraft.getWindow().getHeight();
    }

    @Override
    public int getGuiScaledHeight() {
        return minecraft.getWindow().getGuiScaledHeight();
    }

    @Override
    public double getGuiScale() {
        return minecraft.getWindow().getGuiScale();
    }

    @Override
    public boolean hasControlDown() {
        return Screen.hasControlDown();
    }

    @Override
    public boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }

    @Override
    public boolean hasAltDown() {
        return Screen.hasAltDown();
    }

    @Override
    public boolean isSelectAll(int keyCode) {
        return Screen.isSelectAll(keyCode);
    }

    @Override
    public boolean isCopy(int keyCode) {
        return Screen.isCopy(keyCode);
    }

    @Override
    public boolean isPaste(int keyCode) {
        return Screen.isPaste(keyCode);
    }

    @Override
    public boolean isCut(int keyCode) {
        return Screen.isCut(keyCode);
    }

    @Override
    public String getClipboard() {
        return minecraft.keyboardHandler.getClipboard();
    }

    @Override
    public void setClipboard(String clipboard) {
        minecraft.keyboardHandler.setClipboard(clipboard);
    }
}
