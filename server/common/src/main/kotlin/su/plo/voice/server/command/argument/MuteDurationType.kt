package su.plo.voice.server.command.argument

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.brigadier.CustomArgumentType
import su.plo.slib.api.command.brigadier.McTextMessage
import su.plo.voice.api.server.mute.MuteDurationUnit
import java.util.concurrent.CompletableFuture

private const val PERMANENT = "permanent"

private val DURATION_PATTERN = Regex("^([0-9]+)([mhdwsu])?$")
private val DIGITS_PATTERN = Regex("^[0-9]+$")

private val SUGGESTED_UNITS = listOf("s", "m", "h", "d", "w")

sealed interface MuteDuration {
    data object Permanent : MuteDuration

    data class Time(
        val duration: Long,
        val unit: MuteDurationUnit,
    ) : MuteDuration
}

class MuteDurationType : CustomArgumentType<MuteDuration, String> {
    private val invalidDuration = SimpleCommandExceptionType(
        McTextMessage.of(
            McTextComponent.translatable("pv.command.mute.invalid_duration")
        )
    )

    override val nativeType: ArgumentType<String> = StringArgumentType.word()

    override fun useNativeSuggestions(): Boolean = false

    override fun parse(reader: StringReader): MuteDuration {
        val input = reader.readUnquotedString()

        if (input == PERMANENT) return MuteDuration.Permanent

        val match = DURATION_PATTERN.find(input) ?: throw invalidDuration.createWithContext(reader)

        val duration = match.groupValues[1].toLong()
        val durationUnitString = match.groupValues[2]

        val durationUnit = parseDurationUnit(durationUnitString)

        return MuteDuration.Time(duration, durationUnit)
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        val input = builder.remaining

        if (PERMANENT.startsWith(input, ignoreCase = true)) {
            builder.suggest(PERMANENT)
        }

        if (DIGITS_PATTERN.matches(input)) {
            SUGGESTED_UNITS.forEach { builder.suggest(input + it) }
        }

        return builder.buildFuture()
    }
}

private fun parseDurationUnit(input: String): MuteDurationUnit =
    when (input) {
        "m" -> MuteDurationUnit.MINUTE
        "h" -> MuteDurationUnit.HOUR
        "d" -> MuteDurationUnit.DAY
        "w" -> MuteDurationUnit.WEEK
        "u" -> MuteDurationUnit.TIMESTAMP
        else -> MuteDurationUnit.SECOND
    }
