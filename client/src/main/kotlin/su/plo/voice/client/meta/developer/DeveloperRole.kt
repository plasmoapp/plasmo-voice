package su.plo.voice.client.meta.developer

import su.plo.lib.api.chat.MinecraftTextComponent
import su.plo.lib.api.chat.MinecraftTranslatableText

enum class DeveloperRole(val translatable: MinecraftTranslatableText) {

    HUIX(MinecraftTextComponent.translatable("gui.plasmovoice.about.huix")),
    PROGRAMMING(MinecraftTextComponent.translatable("gui.plasmovoice.about.programming")),
    ARTIST(MinecraftTextComponent.translatable("gui.plasmovoice.about.artist"))
}
