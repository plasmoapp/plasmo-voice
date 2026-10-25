package su.plo.lib.mod.client.gui.widget

import net.minecraft.resources.ResourceLocation
import su.plo.lib.mod.client.ResourceLocationUtil

enum class GuiWidgetTexture(
    val location: ResourceLocation,
) {
    BUTTON_DISABLED(ResourceLocationUtil.parse("widget/button_disabled")),
    BUTTON_DEFAULT(ResourceLocationUtil.parse("widget/button")),
    BUTTON_ACTIVE(ResourceLocationUtil.parse("widget/button_highlighted")),
    TEXT_FIELD(ResourceLocationUtil.parse("widget/text_field")),
    TEXT_FIELD_ACTIVE(ResourceLocationUtil.parse("widget/text_field_highlighted")),
    SLIDER(ResourceLocationUtil.parse("widget/slider")),
    SLIDER_HANDLE(ResourceLocationUtil.parse("widget/slider_handle")),
    SLIDER_HANDLE_ACTIVE(ResourceLocationUtil.parse("widget/slider_handle_highlighted")),
}
