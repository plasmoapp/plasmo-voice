package su.plo.voice.api.client.config

import su.plo.slib.api.chat.component.McTextComponent

enum class OverlappingSourceTypes(
    val text: McTextComponent,
) {
    MUTE_DIRECT(McTextComponent.translatable("gui.plasmovoice.advanced.source_types_overlap.mute_direct")),
    MUTE_PROXIMITY(McTextComponent.translatable("gui.plasmovoice.advanced.source_types_overlap.mute_proximity")),
    OFF((McTextComponent.translatable("gui.plasmovoice.advanced.source_types_overlap.off")));

    companion object {
        @JvmStatic
        val textElements = entries.map { it.text }
    }
}
