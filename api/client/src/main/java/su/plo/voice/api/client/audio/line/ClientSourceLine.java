package su.plo.voice.api.client.audio.line;

import su.plo.voice.api.audio.line.PlayersSourceLine;
import su.plo.voice.proto.data.audio.line.SourceLine;

public interface ClientSourceLine extends SourceLine, PlayersSourceLine {

    void setVolume(double volume);

    double getVolume();
}
