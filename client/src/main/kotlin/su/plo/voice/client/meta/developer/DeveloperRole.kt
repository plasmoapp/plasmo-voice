package su.plo.voice.client.meta.developer

import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.component.McTranslatableText

enum class DeveloperRole(val translatable: McTranslatableText) {

    HUIX(McTextComponent.translatable("gui.plasmovoice.about.huix")),
    PROGRAMMING(McTextComponent.translatable("gui.plasmovoice.about.programming")),
    ARTIST(McTextComponent.translatable("gui.plasmovoice.about.artist"))
}
