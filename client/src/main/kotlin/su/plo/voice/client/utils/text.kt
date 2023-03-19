package su.plo.voice.client.utils

import gg.essential.universal.ChatColor
import gg.essential.universal.UGraphics
import su.plo.lib.api.chat.MinecraftTextComponent
import su.plo.lib.mod.client.render.RenderUtil

fun Char.width() = UGraphics.getCharWidth(this)

fun String.width() = UGraphics.getStringWidth(this)

fun MinecraftTextComponent.width() = RenderUtil.getTextWidth(this)

fun getTruncatedString(
    text: String,
    maxWidth: Float,
    trimmedTextSuffix: String = "..."
) : String {
    if (text.width() <= maxWidth) return text

    var length = text.length
    while (length > 0 && (text.substring(0, length) + trimmedTextSuffix).width() > maxWidth)
        length--

    return text.substring(0, length) + trimmedTextSuffix
}

// https://github.com/EssentialGG/Elementa/blob/master/src/main/kotlin/gg/essential/elementa/utils/text.kt
fun splitStringToWidthTruncated(
    text: String,
    maxLineWidth: Float,
    maxLines: Int,
    ensureSpaceAtEndOfLines: Boolean = true,
    processColorCodes: Boolean = true,
    trimmedTextSuffix: String = "..."
): List<String> {
    val lines = getStringSplitToWidth(text, maxLineWidth, ensureSpaceAtEndOfLines, processColorCodes)
    if (lines.size <= maxLines)
        return lines

    return lines.subList(0, maxLines).mapIndexed { index, contents ->
        if (index == maxLines - 1) {
            var length = contents.length
            while (length > 0 && (contents.substring(0, length) + trimmedTextSuffix).width() > maxLineWidth)
                length--
            contents.substring(0, length) + trimmedTextSuffix
        } else contents
    }
}

fun getStringSplitToWidth(
    text: String,
    maxLineWidth: Float,
    ensureSpaceAtEndOfLines: Boolean = true,
    processColorCodes: Boolean = true
): List<String> {
    val spaceWidth = ' '.width()
    val maxLineWidthSpace = maxLineWidth - if (ensureSpaceAtEndOfLines) spaceWidth else 0f
    val lineList = mutableListOf<String>()
    val currLine = StringBuilder()
    var textPos = 0
    var currChatColor: ChatColor? = null
    var currChatFormatting: ChatColor? = null

    fun pushLine() {
        lineList.add(currLine.toString().trim())
        currLine.clear()
        if (processColorCodes) {
            currChatColor?.also { currLine.append("§${it.char}") }
            currChatFormatting?.also { currLine.append("§${it.char}") }
        }
    }

    while (textPos < text.length) {
        val builder = StringBuilder()

        while (textPos < text.length && text[textPos].let { it != ' ' && it != '\n'}) {
            val ch = text[textPos]
            if (processColorCodes && (ch == '§' || ch == '&') && textPos + 1 < text.length) {
                val colorCh = text[textPos + 1]
                val nextColor = ChatColor.values().firstOrNull { it.char == colorCh }
                if (nextColor != null) {
                    builder.append('§')
                    builder.append(colorCh)

                    if (nextColor.isFormat) {
                        currChatFormatting = nextColor
                    } else {
                        currChatColor = nextColor
                    }

                    textPos += 2
                    continue
                }
            }

            builder.append(ch)
            textPos++
        }

        val newline = textPos < text.length && text[textPos] == '\n'
        val word = builder.toString()
        val wordWidth = word.width()

        if (processColorCodes && newline) {
            currChatColor = null
            currChatFormatting = null
        }

        if ((currLine.toString() + word).width() > maxLineWidthSpace) {
            if (wordWidth > maxLineWidthSpace) {
                // Split up the word into it's own lines
                if (currLine.toString().width() > 0)
                    pushLine()

                for (char in word.toCharArray()) {
                    if ((currLine.toString() + char).width() > maxLineWidthSpace)
                        pushLine()
                    currLine.append(char)
                }
            } else {
                pushLine()
                currLine.append(word)
            }

            // Check if we have a space, and if so, append it to the new line
            if (textPos < text.length) {
                if (!newline) {
                    if (currLine.toString().width() + spaceWidth > maxLineWidthSpace)
                        pushLine()
                    currLine.append(' ')
                    textPos++
                } else {
                    pushLine()
                    textPos++
                }
            }
        } else {
            currLine.append(word)

            // Check if we have a space, and if so, append it to a line
            if (!newline && textPos < text.length) {
                textPos++
                currLine.append(' ')
            } else if (newline) {
                pushLine()
                textPos++
            }
        }
    }

    lineList.add(currLine.toString())

    return lineList
}
