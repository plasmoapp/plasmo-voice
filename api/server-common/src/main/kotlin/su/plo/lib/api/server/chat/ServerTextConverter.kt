package su.plo.lib.api.server.chat

import su.plo.lib.api.chat.MinecraftTextComponent
import su.plo.lib.api.chat.MinecraftTranslatableText
import su.plo.lib.api.chat.TranslatableTextConverter
import su.plo.lib.api.server.command.MinecraftCommandSource
import su.plo.voice.api.server.config.ServerLanguages
import java.util.function.Supplier

abstract class ServerTextConverter<T> constructor(
    private val languagesSupplier: Supplier<ServerLanguages?>
) : TranslatableTextConverter<T>() {

    fun convertToJson(source: MinecraftCommandSource, text: MinecraftTextComponent): String {
        val languages = languagesSupplier.get() ?: return convertToJson(text)
        val language = languages.getServerLanguage(source)

        val convertedText = translateInner(language, text)
        if (convertedText !is MinecraftTranslatableText)
            return convertToJson(text)

        if (!language.containsKey(convertedText.key))
            return convertToJson(text)

        return convertToJson(translate(
            language,
            convertedText
        ))
    }

    fun convert(source: MinecraftCommandSource, text: MinecraftTextComponent): T {
        val languages = languagesSupplier.get() ?: return convert(text)
        val language = languages.getServerLanguage(source)

        val convertedText = translateInner(language, text)
        if (convertedText !is MinecraftTranslatableText)
            return convert(text)

        if (!language.containsKey(convertedText.key))
            return convert(text)

        return convert(translate(
            language,
            convertedText
        ))
    }
}
