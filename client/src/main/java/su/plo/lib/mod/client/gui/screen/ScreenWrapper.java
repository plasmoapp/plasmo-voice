package su.plo.lib.mod.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import su.plo.slib.api.chat.component.McTextComponent;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.voice.api.client.config.hotkey.Hotkey;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.ModVoiceClient;
import su.plo.voice.client.event.key.KeyPressedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//#if MC>=12000
//$$ import net.minecraft.client.gui.GuiGraphics;
//$$ import net.minecraft.locale.Language;
//#endif

@ToString
public final class ScreenWrapper
        extends Screen {

    public static void openScreen(@Nullable GuiScreen screen) {
        if (screen == null) {
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(null));
            return;
        }

        ScreenWrapper wrapped = new ScreenWrapper(screen);

        wrapped.screen.screen = wrapped;
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(wrapped));
    }

    public static Optional<ScreenWrapper> getCurrentWrappedScreen() {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof ScreenWrapper) {
            return Optional.of((ScreenWrapper) screen);
        }

        return Optional.empty();
    }

    @Getter
    private final GuiScreen screen;

    private boolean ignoreFirstMove = true;

    //#if MC>=12000
    //$$ private @Nullable GuiGraphics currentContext;
    //$$ private int lastMouseX;
    //$$ private int lastMouseY;
    //$$ private float lastPartialTicks;
    //#endif

    private ScreenWrapper(@NotNull GuiScreen screen) {
        super(RenderUtil.getTextConverter().convert(McTextComponent.empty()));

        this.screen = screen;
    }

    // Screen override
    @NotNull
    @Override
    public Component getTitle() {
        return RenderUtil.getTextConverter().convert(screen.getTitle());
    }

    @Override
    public void tick() {
        screen.tick();
    }

    @Override
    protected void init() {
        screen.init();

        ModVoiceClient.INSTANCE.getEventBus().unregister(
                ModVoiceClient.INSTANCE,
                this
        );
        ModVoiceClient.INSTANCE.getEventBus().register(
                ModVoiceClient.INSTANCE,
                this
        );
    }

    @Override
    public void removed() {
        ModVoiceClient.INSTANCE.getEventBus().unregister(
                ModVoiceClient.INSTANCE,
                this
        );

        screen.removed();
    }

    //#if MC>=12000
    //$$ @Override
    //$$ public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
    //$$     currentContext = guiGraphics;
    //$$     lastMouseX = mouseX;
    //$$     lastMouseY = mouseY;
    //$$     lastPartialTicks = partialTicks;
    //$$     screen.render(guiGraphics.pose(), mouseX, mouseY, partialTicks);
    //$$     currentContext = null;
    //$$ }
    //$$
    //$$ public void renderBackground(PoseStack stack) {
    //$$     if (currentContext == null) return;
    //#if MC>=12002
    //$$     super.renderBackground(currentContext, lastMouseX, lastMouseY, lastPartialTicks);
    //#else
    //$$     super.renderBackground(currentContext);
    //#endif
    //$$ }
    //#else
    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        screen.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    //#endif

//    @Override
//    protected void updateNarratedWidget(@NotNull NarrationElementOutput narrationOutput) {
//        screen.updateNarratedWidget(new ModScreenNarrationOutput(narrationOutput));
//    }

    // ContainerEventHandler override
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        screen.mouseClicked(mouseX, mouseY, mouseButton);
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        screen.mouseReleased(mouseX, mouseY, button);
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
//        if (this.ignoreFirstMove) {
//            this.lastDraggedMouseX = mouseX;
//            this.lastDraggedMouseY = mouseY;
//            this.ignoreFirstMove = false;
//        }
//
//        double deltaX = (mouseX - lastDraggedMouseX)
//                * (double)UResolution.getScaledWidth() / (double)UResolution.getWindowWidth()
//                * UResolution.getScaleFactor();
//        double deltaY = (mouseY - lastDraggedMouseY)
//                * (double)UResolution.getScaledHeight() / (double)UResolution.getWindowHeight()
//                * UResolution.getScaleFactor();

//        this.lastDraggedMouseX = mouseX;
//        this.lastDraggedMouseY = mouseY;

        screen.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        return false;
    }

    @Override
    //#if MC>=12002
    //$$ public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double delta) {
    //#else
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    //#endif
        screen.mouseScrolled(mouseX, mouseY, delta);
//        super.onMouseScrolled(delta);
        return false;
    }

    //    @Override
//    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
//        return screen.mouseScrolled(mouseX, mouseY, delta);
//    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 0) {
            screen.charTyped((char) 0, modifiers);
            return false;
        }

        if (screen.keyPressed(keyCode, modifiers)) {
            return true;
        }

        if (shouldCloseOnEsc() && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_TAB) {
            boolean shiftKeyDown = Screen.hasShiftDown();

            if (!screen.changeFocus(shiftKeyDown)) {
                screen.changeFocus(shiftKeyDown);
            }
        }

        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (screen.keyReleased(keyCode, (char) 0, modifiers)) {
            return false;
        }

        super.keyReleased(keyCode, 0, modifiers);
        return false;
    }

    // MinecraftScreen impl
    @Override
    public boolean shouldCloseOnEsc() {
        return screen.shouldCloseOnEsc();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(null));
    }

    public void renderTooltipWrapped(PoseStack stack, List<McTextComponent> tooltip, int mouseX, int mouseY) {
        //#if MC>=12000
        //$$ setTooltipForNextRenderPass(
        //$$         Language.getInstance().getVisualOrder(
        //$$                 new ArrayList<>(
        //$$                         RenderUtil.getTextConverter().convert(tooltip)
        //$$                 )
        //$$         )
        //$$ );
        //#else
        renderComponentTooltip(
                stack,
                new ArrayList<>(
                        RenderUtil.getTextConverter().convert(tooltip)
                ),
                mouseX,
                mouseY
        );
        //#endif
    }

//    @RequiredArgsConstructor
//    class ModScreenNarrationOutput implements NarrationOutput {
//
//        private final NarrationElementOutput narrationOutput;
//
//        @Override
//        public void add(@NotNull Type type, @NotNull MinecraftTextComponent component) {
//            narrationOutput.add(
//                    NarratedElementType.valueOf(type.name()),
//                    textConverter.convert(component)
//            );
//        }
//
//        @Override
//        public NarrationOutput nest() {
//            return new ModScreenNarrationOutput(narrationOutput.nest());
//        }
//    }
}
