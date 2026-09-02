package su.plo.lib.mod.extensions

import com.mojang.blaze3d.platform.InputConstants
import su.plo.voice.api.client.config.hotkey.Hotkey

fun Hotkey.Key.toMinecraft(): InputConstants.Key? =
    when (type) {
        Hotkey.Type.MOUSE -> InputConstants.Type.MOUSE.getOrCreate(code)
        Hotkey.Type.KEYSYM -> InputConstants.Type.KEYSYM.getOrCreate(code)
        //#if MC<26.3
        Hotkey.Type.SCANCODE -> InputConstants.Type.SCANCODE.getOrCreate(code)
        //#else
        //$$ else -> null
        //#endif
    }

fun Hotkey.Key.serializedName(): String? =
    toMinecraft()?.name

fun InputConstants.Key.toHotkey(): Hotkey.Key? =
    when (type) {
        InputConstants.Type.MOUSE -> Hotkey.Type.MOUSE.getOrCreate(value)
        InputConstants.Type.KEYSYM -> Hotkey.Type.KEYSYM.getOrCreate(value)
        //#if MC<26.3
        InputConstants.Type.SCANCODE -> Hotkey.Type.SCANCODE.getOrCreate(value)
        //#endif
        else -> null
    }

fun String.toHotkeyKey(): Hotkey.Key? =
    try {
        InputConstants.getKey(this).toHotkey()
    } catch (_: IllegalArgumentException) {
        null
    }
