package su.plo.lib.api.chat

abstract class TranslatableTextConverter<T> : MinecraftTextConverter<T> {

    protected open fun translate(
        language: Map<String?, String?>,
        hoverEvent: MinecraftTextHoverEvent?
    ): MinecraftTextHoverEvent? {
        if (hoverEvent == null) return null

        if (hoverEvent.action() == MinecraftTextHoverEvent.Action.SHOW_TEXT &&
            hoverEvent.value() is MinecraftTranslatableText
        ) {
            return MinecraftTextHoverEvent.showText(
                translate(
                    language,
                    (hoverEvent.value() as MinecraftTranslatableText)
                )
            )
        }

        return hoverEvent
    }

    protected fun translate(
        language: Map<String?, String?>,
        translatable: MinecraftTranslatableText
    ): MinecraftTextComponent {
        return if (translatable.args.any { it is MinecraftTextComponent }) {
            MinecraftTextComponent.translatable(
                language[translatable.key]?.replace("&", "§"),
                *translatable.args
            )
                .mergeWith(translatable)
                .hoverEvent(translate(language, translatable.hoverEvent()))
        } else {
            MinecraftTextComponent.literal(
                String.format(
                    language[translatable.key]!!,
                    *translatable.args
                ).replace("&", "§")
            )
                .mergeWith(translatable)
                .hoverEvent(translate(language, translatable.hoverEvent()))

        }
    }

    protected fun translateInner(
        language: Map<String?, String?>,
        text: MinecraftTextComponent
    ): MinecraftTextComponent {
        return if (text is MinecraftTranslatableText) {
            translateArguments(language, text)
        } else {
            text
        }.also { translateSiblings(language, it) }
    }

    private fun translateArguments(
        language: Map<String?, String?>,
        text: MinecraftTranslatableText
    ): MinecraftTranslatableText {
        for (index in text.args.indices) {
            val argument = text.args[index]

            if (argument !is MinecraftTextComponent)
                continue

            val translatedText = translateInner(language, argument)
            if (translatedText !is MinecraftTranslatableText ||
                !language.containsKey(translatedText.key)
            ) {
                text.args[index] = translatedText
                continue
            }

            text.args[index] = translate(language, translatedText)
        }

        return text
    }

    private fun translateSiblings(
        language: Map<String?, String?>,
        text: MinecraftTextComponent
    ): MinecraftTextComponent {
        for (index in text.siblings().indices) {
            val sibling = translateInner(
                language,
                text.siblings()[index]
            )

            text.siblings()[index] = if (sibling is MinecraftTranslatableText && language.containsKey(sibling.key)) {
                translate(language, sibling)
            } else {
                sibling
            }
        }
        return text
    }
}
