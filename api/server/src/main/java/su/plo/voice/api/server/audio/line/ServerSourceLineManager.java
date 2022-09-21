package su.plo.voice.api.server.audio.line;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.line.SourceLineManager;

public interface ServerSourceLineManager extends SourceLineManager<ServerSourceLine> {

    @NotNull ServerSourceLine register(@NotNull Object addonObject,
                                       @NotNull String name,
                                       @NotNull String translation,
                                       @NotNull String icon,
                                       int weight);

    @NotNull ServerPlayersSourceLine registerPlayers(@NotNull Object addonObject,
                                                     @NotNull String name,
                                                     @NotNull String translation,
                                                     @NotNull String icon,
                                                     int weight);
}
