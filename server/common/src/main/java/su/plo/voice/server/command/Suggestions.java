package su.plo.voice.server.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.slib.api.command.McCommandSource;
import su.plo.slib.api.server.McServerLib;
import su.plo.slib.api.server.entity.player.McServerPlayer;

import java.util.List;
import java.util.stream.Collectors;

public final class Suggestions {

    public static List<String> players(
            @NotNull McServerLib minecraftServer,
            @Nullable McCommandSource source,
            @NotNull String argument
    ) {
        return minecraftServer.getPlayers()
                .stream()
                .filter(player -> source != null
                        && (!(source instanceof McServerPlayer) || ((McServerPlayer) source).canSee(player))
                        && player.getName().regionMatches(true, 0, argument, 0, argument.length()))
                .map(McServerPlayer::getName)
                .collect(Collectors.toList());
    }

    public Suggestions() {
    }
}
