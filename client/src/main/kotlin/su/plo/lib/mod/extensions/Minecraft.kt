package su.plo.lib.mod.extensions

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen

fun Minecraft.currentScreen(): Screen? =
    //#if MC>=26.2
    //$$ this.gui.screen()
    //#else
    this.screen
    //#endif

fun Minecraft.setCurrentScreen(screen: Screen?) {
    //#if MC>=26.2
    //$$ this.gui.setScreen(screen)
    //#else
    this.setScreen(screen)
    //#endif
}

fun Minecraft.isHudHidden(): Boolean =
    //#if MC>=26.2
    //$$ this.gui.hud.isHidden
    //#else
    this.options.hideGui
    //#endif
