package su.plo.voice.server.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.entity.MinecraftPlayer;
import su.plo.lib.server.MinecraftServerLib;
import su.plo.lib.server.command.MinecraftCommandSource;
import su.plo.lib.server.entity.MinecraftServerPlayer;

import java.util.List;
import java.util.stream.Collectors;

public final class Suggestions {

    public static List<String> players(@NotNull MinecraftServerLib minecraftServer,
                                       @Nullable MinecraftCommandSource source,
                                       @NotNull String argument) {
        return minecraftServer.getPlayers()
                .stream()
                .filter(player -> source != null
                        && (!(source instanceof MinecraftServerPlayer) || ((MinecraftServerPlayer) source).canSee(player))
                        && player.getName().regionMatches(true, 0, argument, 0, argument.length()))
                .map(MinecraftPlayer::getName)
                .collect(Collectors.toList());
    }

    public Suggestions() {
    }
}
