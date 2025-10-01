package su.plo.lib.mod.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import su.plo.lib.mod.client.Inputs;
import su.plo.lib.mod.client.render.gui.GuiRenderContext;
import su.plo.slib.api.chat.component.McTextComponent;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.voice.client.ModVoiceClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//#if MC>=12109
//$$ import net.minecraft.client.input.CharacterEvent;
//$$ import net.minecraft.client.input.KeyEvent;
//$$ import net.minecraft.client.input.MouseButtonEvent;
//#endif

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
        super(RenderUtil.getTextConverter().convert(screen.getTitle()));

        this.screen = screen;
    }

    // Screen override
    @NotNull
    @Override
    public Component getTitle() {
        if (screen == null) {
            return super.getTitle();
        }

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
    //#if MC<12105
    //$$     RenderUtil.preserveGlState();
    //#endif
    //$$     currentContext = guiGraphics;
    //$$     lastMouseX = mouseX;
    //$$     lastMouseY = mouseY;
    //$$     lastPartialTicks = partialTicks;
    //$$     GuiRenderContext context = new GuiRenderContext(guiGraphics);
    //$$     screen.render(context, mouseX, mouseY, partialTicks);
    //$$     currentContext = null;
    //#if MC<12105
    //$$     RenderUtil.restoreGlState();
    //#endif
    //$$ }
    //$$
    //$$ public void renderBackground(@NotNull GuiRenderContext context) {
    //$$     if (currentContext == null) return;
    //#if MC>=12002

    //#if MC>=12106
    //$$     context.flush();
    //#endif

    //$$     super.renderBackground(currentContext, lastMouseX, lastMouseY, lastPartialTicks);

    //#if MC>=12102
    //$$     context.flush();
    //#endif

    //#else
    //$$     super.renderBackground(currentContext);
    //#if MC<12105
    //$$     RenderUtil.restoreGlState(true);
    //#endif
    //#endif
    //$$ }
    //#else
    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        //#if MC<12105
        RenderUtil.preserveGlState();
        //#endif

        GuiRenderContext context = new GuiRenderContext(matrixStack);

        screen.render(context, mouseX, mouseY, partialTicks);

        //#if MC<12105
        RenderUtil.restoreGlState();
        //#endif
    }

    public void renderBackground(@NotNull GuiRenderContext context) {
        super.renderBackground(context.getStack());
    }
    //#endif

//    @Override
//    protected void updateNarratedWidget(@NotNull NarrationElementOutput narrationOutput) {
//        screen.updateNarratedWidget(new ModScreenNarrationOutput(narrationOutput));
//    }

    // ContainerEventHandler override
    //#if MC>=12109
    //$$  @Override
    //$$ public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean bl) {
    //$$     screen.mouseClicked(event.x(), event.y(), event.button());
    //$$     return false;
    //$$ }
    //$$
    //$$ @Override
    //$$ public boolean mouseReleased(@NotNull MouseButtonEvent event) {
    //$$     screen.mouseReleased(event.x(), event.y(), event.button());
    //$$     return false;
    //$$ }
    //$$
    //$$ @Override
    //$$ public boolean mouseDragged(@NotNull MouseButtonEvent event, double deltaX, double deltaY) {
    //$$     screen.mouseDragged(event.x(), event.y(), event.button(), deltaX, deltaY);
    //$$     return false;
    //$$ }
    //#else
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
        screen.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        return false;
    }
    //#endif

    @Override
    //#if MC>=12002
    //$$ public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double delta) {
    //#else
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    //#endif
        screen.mouseScrolled(mouseX, mouseY, delta);
        return false;
    }

    //#if MC>=12109
    //$$ @Override
    //$$ public boolean charTyped(@NotNull CharacterEvent event) {
    //$$     return screen.charTyped((char) event.codepoint(), event.modifiers());
    //$$ }
    //#else
    @Override
    public boolean charTyped(char typedChar, int modifiers) {
        return screen.charTyped(typedChar, modifiers);
    }
    //#endif

    //#if MC>=12109
    //$$ @Override
    //$$ public boolean keyPressed(@NotNull KeyEvent event) {
    //$$     return innerKeyPressed(event.key(), event.scancode(), event.modifiers());
    //$$ }
    //#else
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return innerKeyPressed(keyCode, scanCode, modifiers);
    }
    //#endif

    private boolean innerKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 0) {
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
            boolean shiftKeyDown = Inputs.hasShiftDown();

            if (!screen.changeFocus(shiftKeyDown)) {
                screen.changeFocus(shiftKeyDown);
            }
        }

        return false;
    }

    //#if MC>=12109
    //$$  @Override
    //$$  public boolean keyReleased(@NotNull KeyEvent event) {
    //$$      if (screen.keyReleased(event.key(), (char) 0, event.modifiers())) {
    //$$          return false;
    //$$      }
    //$$
    //$$      super.keyReleased(event);
    //$$      return false;
    //$$  }
    //#else
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (screen.keyReleased(keyCode, (char) 0, modifiers)) {
            return false;
        }

        super.keyReleased(keyCode, 0, modifiers);
        return false;
    }
    //#endif

    // MinecraftScreen impl
    @Override
    public boolean shouldCloseOnEsc() {
        return screen.shouldCloseOnEsc();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(null));
    }

    public void renderTooltipWrapped(
            @NotNull GuiRenderContext context,
            @NotNull List<McTextComponent> tooltip,
            int mouseX,
            int mouseY
    ) {
        //#if MC>=12106
        //$$ currentContext.setTooltipForNextFrame(
        //$$         Language.getInstance().getVisualOrder(
        //$$                 new ArrayList<>(
        //$$                         RenderUtil.getTextConverter().convert(tooltip)
        //$$                 )
        //$$         ),
        //$$         mouseX,
        //$$         mouseY
        //$$ );
        //#elseif MC>=12000
        //$$ setTooltipForNextRenderPass(
        //$$         Language.getInstance().getVisualOrder(
        //$$                 new ArrayList<>(
        //$$                         RenderUtil.getTextConverter().convert(tooltip)
        //$$                 )
        //$$         )
        //$$ );
        //#else
        renderComponentTooltip(
                context.getStack(),
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
