package gg.essential.universal

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.util.FormattedCharSink

class TextBuilder(private val isFormatted: Boolean) : FormattedCharSink {

    private val builder = StringBuilder()
    private var cachedStyle: Style? = null

    override fun accept(index: Int, style: Style, codePoint: Int): Boolean  {
        if (isFormatted && style != cachedStyle) {
            cachedStyle = style
            builder.append(formatString(style))
        }

        builder.append(codePoint.toChar())
        return true
    }

    fun getString() = builder.toString()

    private fun formatString(style: Style): String {
        val builder = StringBuilder("§r")

        when {
            style.isBold -> builder.append("§l")
            style.isItalic -> builder.append("§o")
            style.isUnderlined -> builder.append("§n")
            style.isStrikethrough -> builder.append("§m")
            style.isObfuscated -> builder.append("§k")
        }

        style.color?.let(colorToFormatChar::get)?.let {
            builder.append(it)
        }
        return builder.toString()
    }

    companion object {
        private val colorToFormatChar = ChatFormatting.values().mapNotNull { format ->
            TextColor.fromLegacyFormat(format)?.let { it to format }
        }.toMap()
    }
}
