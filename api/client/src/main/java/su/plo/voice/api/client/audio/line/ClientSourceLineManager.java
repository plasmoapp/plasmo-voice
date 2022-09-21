package su.plo.voice.api.client.audio.line;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.line.ClientSourceLine;
import su.plo.voice.api.audio.line.SourceLineManager;
import su.plo.voice.proto.data.audio.line.SourceLine;

import java.util.Collection;

public interface ClientSourceLineManager extends SourceLineManager<ClientSourceLine> {

    @NotNull ClientSourceLine register(@NotNull ClientSourceLine line);

    @NotNull Collection<ClientSourceLine> register(@NotNull Collection<SourceLine> lines);
}
