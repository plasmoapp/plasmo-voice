package su.plo.lib.mod.client.gui.widget

import net.minecraft.resources.ResourceLocation

enum class GuiWidgetTexture(
    val location: ResourceLocation,
    val u: Int,
    val v: Int,
    val spriteWidth: Int,
    val spriteHeight: Int,
    val textureWidth: Int,
    val textureHeight: Int
) {
    //#if MC>=12002
    //$$ BUTTON_DISABLED(
    //$$     ResourceLocation.tryParse("textures/gui/sprites/widget/button_disabled.png")!!,
    //$$     0,
    //$$     0,
    //$$     200,
    //$$     20,
    //$$     200,
    //$$     20
    //$$ ),
    //$$ BUTTON_DEFAULT(
    //$$     ResourceLocation.tryParse("textures/gui/sprites/widget/button.png")!!,
    //$$     0,
    //$$     0,
    //$$     200,
    //$$     20,
    //$$     200,
    //$$     20
    //$$ ),
    //$$ BUTTON_ACTIVE(
    //$$     ResourceLocation.tryParse("textures/gui/sprites/widget/button_highlighted.png")!!,
    //$$     0,
    //$$     0,
    //$$     200,
    //$$     20,
    //$$     200,
    //$$     20
    //$$ ),
    //$$ TEXT_FIELD(
    //$$     ResourceLocation.tryParse("textures/gui/sprites/widget/text_field.png")!!,
    //$$     0,
    //$$     0,
    //$$     200,
    //$$     20,
    //$$     200,
    //$$     20
    //$$ ),
    //$$ TEXT_FIELD_ACTIVE(
    //$$     ResourceLocation.tryParse("textures/gui/sprites/widget/text_field_highlighted.png")!!,
    //$$     0,
    //$$     0,
    //$$     200,
    //$$     20,
    //$$     200,
    //$$     20
    //$$ ),
    //$$ SLIDER(
    //$$     ResourceLocation.tryParse("textures/gui/sprites/widget/slider.png")!!,
    //$$     0,
    //$$     0,
    //$$     200,
    //$$     20,
    //$$     200,
    //$$     20
    //$$ ),
    //$$ SLIDER_HANDLE(
    //$$     ResourceLocation.tryParse("textures/gui/sprites/widget/slider_handle.png")!!,
    //$$     0,
    //$$     0,
    //$$     8,
    //$$     20,
    //$$     8,
    //$$     20
    //$$ ),
    //$$ SLIDER_HANDLE_ACTIVE(
    //$$     ResourceLocation.tryParse("textures/gui/sprites/widget/slider_handle_highlighted.png")!!,
    //$$     0,
    //$$     0,
    //$$     8,
    //$$     20,
    //$$     8,
    //$$     20
    //$$ ),
    //#else
    BUTTON_DISABLED(
        ResourceLocation.tryParse("textures/gui/widgets.png")!!,
        0,
        46,
        200,
        20,
        256,
        256
    ),
    BUTTON_DEFAULT(
        ResourceLocation.tryParse("textures/gui/widgets.png")!!,
        0,
        46 + 20,
        200,
        20,
        256,
        256
    ),
    BUTTON_ACTIVE(
        ResourceLocation.tryParse("textures/gui/widgets.png")!!,
        0,
        46 + 40,
        200,
        20,
        256,
        256
    ),
    SLIDER(
        ResourceLocation.tryParse("textures/gui/widgets.png")!!,
        0,
        46,
        200,
        20,
        256,
        256
    ),
    //#endif
}
