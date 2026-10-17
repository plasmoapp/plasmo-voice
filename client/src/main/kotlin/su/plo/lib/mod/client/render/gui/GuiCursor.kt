package su.plo.lib.mod.client.render.gui

//#if MC>=12109
//$$ import com.mojang.blaze3d.platform.cursor.CursorType
//$$ import com.mojang.blaze3d.platform.cursor.CursorTypes
//#endif

enum class GuiCursor {
    ARROW,
    IBEAM,
    CROSSHAIR,
    POINTING_HAND,
    RESIZE_NS,
    RESIZE_EW,
    RESIZE_ALL,
    NOT_ALLOWED;

    //#if MC>=12109
    //$$ fun mc(): CursorType =
    //$$     when (this) {
    //$$         ARROW -> CursorTypes.ARROW
    //$$         IBEAM -> CursorTypes.IBEAM
    //$$         CROSSHAIR -> CursorTypes.CROSSHAIR
    //$$         POINTING_HAND -> CursorTypes.POINTING_HAND
    //$$         RESIZE_NS -> CursorTypes.RESIZE_NS
    //$$         RESIZE_EW -> CursorTypes.RESIZE_EW
    //$$         RESIZE_ALL -> CursorTypes.RESIZE_ALL
    //$$         NOT_ALLOWED -> CursorTypes.NOT_ALLOWED
    //$$     }
    //#endif
}
