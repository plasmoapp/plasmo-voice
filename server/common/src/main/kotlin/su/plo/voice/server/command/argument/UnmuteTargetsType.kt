package su.plo.voice.server.command.argument

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import su.plo.slib.api.command.brigadier.ArgumentResolver
import su.plo.slib.api.command.brigadier.CustomArgumentType
import su.plo.slib.api.entity.player.McGameProfile
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.server.command.brigadier.McArgumentTypes
import su.plo.slib.api.server.command.brigadier.McGameProfilesArgumentResolver
import su.plo.voice.api.server.mute.MuteManager
import su.plo.voice.util.CoroutineScopes
import java.util.UUID
import java.util.concurrent.CompletableFuture

class UnmuteTargetsType(
    private val muteManager: () -> MuteManager,
    private val minecraftServer: () -> McServerLib,
) : CustomArgumentType<UnmuteTargetsResolver, McGameProfilesArgumentResolver> {
    override val nativeType: ArgumentType<McGameProfilesArgumentResolver> =
        McArgumentTypes.gameProfiles()

    override fun useNativeSuggestions(): Boolean = false

    override fun parse(reader: StringReader): UnmuteTargetsResolver {
        if (reader.canRead() && reader.peek() == '@') {
            val selector = nativeType.parse(reader)

            return UnmuteTargetsResolver { source -> selector.resolve(source) }
        }

        val start = reader.cursor
        while (reader.canRead() && reader.peek() != ' ') reader.skip()
        val input = reader.string.substring(start, reader.cursor)

        return UnmuteTargetsResolver { lookupProfiles(input) }
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> =
        CoroutineScopes.DefaultSupervisor.future(Dispatchers.IO) {
            val suggestions =
                muteManager()
                    .muteStorage
                    .mutedPlayers
                    .map {
                        minecraftServer().getGameProfile(it.playerUUID)
                            ?.name
                            ?: it.playerUUID.toString()
                    }
                    .filter { it.startsWith(builder.remaining, ignoreCase = true) }

            if (suggestions.isEmpty()) {
                val newBuilder = builder.restart()
                nativeType.listSuggestions(context, builder).await()
                    .list
                    .filter { it.text.startsWith("@") }
                    .forEach { newBuilder.suggest(it.text, it.tooltip) }

                newBuilder.build()
            } else {
                suggestions.forEach(builder::suggest)

                builder.build()
            }
        }

    private fun lookupProfiles(input: String): List<McGameProfile> {
        val uuid = runCatching { UUID.fromString(input) }.getOrNull()
        if (uuid != null) {
            val byUuid = minecraftServer().getGameProfile(uuid)
            val profile = byUuid ?: McGameProfile(uuid, "unknown", emptyList())

            return listOf(profile)
        }

        val byName = minecraftServer().getGameProfile(input)
        if (byName != null) {
            return listOf(byName)
        }

        return emptyList()
    }
}

fun interface UnmuteTargetsResolver : ArgumentResolver<List<McGameProfile>>
