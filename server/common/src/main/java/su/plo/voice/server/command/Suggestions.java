package su.plo.voice.server.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.entity.MinecraftPlayerEntity;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.lib.api.server.entity.MinecraftServerPlayerEntity;

import java.util.List;
import java.util.stream.Collectors;

public final class Suggestions {

    public static List<String> players(@NotNull MinecraftServerLib minecraftServer,
                                       @Nullable MinecraftCommandSource source,
                                       @NotNull String argument) {
        return minecraftServer.getPlayers()
                .stream()
                .filter(player -> source != null
                        && (!(source instanceof MinecraftServerPlayerEntity) || ((MinecraftServerPlayerEntity) source).canSee(player))
                        && player.getName().regionMatches(true, 0, argument, 0, argument.length()))
                .map(MinecraftPlayerEntity::getName)
                .collect(Collectors.toList());
    }

    public Suggestions() {
    }
}
