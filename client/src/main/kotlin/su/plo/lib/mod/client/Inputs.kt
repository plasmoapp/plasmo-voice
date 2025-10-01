package su.plo.lib.mod.client

//#if MC>=12109
//$$ import net.minecraft.client.Minecraft
//$$ import net.minecraft.client.input.InputQuirks
//$$ import org.lwjgl.glfw.GLFW
//#else
import net.minecraft.client.gui.screens.Screen
//#endif

object Inputs {
    @JvmStatic
    @JvmOverloads
    fun hasShiftDown(modifiers: Int? = null): Boolean =
        //#if MC>=12109
        //$$ if (modifiers != null) {
        //$$     (modifiers and 1) != 0
        //$$ } else {
        //$$     Minecraft.getInstance().hasShiftDown()
        //$$ }
        //#else
        Screen.hasShiftDown()
        //#endif

    @JvmStatic
    fun hasControlDown(modifiers: Int): Boolean =
        //#if MC>=12109
        //$$ (modifiers and InputQuirks.EDIT_SHORTCUT_KEY_MODIFIER) != 0
        //#else
        Screen.hasControlDown()
        //#endif

    @JvmStatic
    fun hasAltDown(modifiers: Int): Boolean =
        //#if MC>=12109
        //$$ (modifiers and 4) != 0
        //#else
        Screen.hasAltDown()
        //#endif

    @JvmStatic
    fun isSelectAll(keyCode: Int, modifiers: Int): Boolean =
        //#if MC>=12109
        //$$ keyCode == GLFW.GLFW_KEY_A && hasControlDown(modifiers) && !hasShiftDown(modifiers) && !hasAltDown(modifiers)
        //#else
        Screen.isSelectAll(keyCode)
        //#endif

    @JvmStatic
    fun isCopy(keyCode: Int, modifiers: Int): Boolean =
        //#if MC>=12109
        //$$ keyCode == GLFW.GLFW_KEY_C && hasControlDown(modifiers) && !hasShiftDown(modifiers) && !hasAltDown(modifiers)
        //#else
        Screen.isCopy(keyCode)
        //#endif

    @JvmStatic
    fun isPaste(keyCode: Int, modifiers: Int): Boolean =
        //#if MC>=12109
        //$$ keyCode == GLFW.GLFW_KEY_V && hasControlDown(modifiers) && !hasShiftDown(modifiers) && !hasAltDown(modifiers)
        //#else
        Screen.isPaste(keyCode)
        //#endif

    @JvmStatic
    fun isCut(keyCode: Int, modifiers: Int): Boolean =
        //#if MC>=12109
        //$$ keyCode == GLFW.GLFW_KEY_X && hasControlDown(modifiers) && !hasShiftDown(modifiers) && !hasAltDown(modifiers)
        //#else
        Screen.isCut(keyCode)
        //#endif
}
