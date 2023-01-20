package su.plo.voice.api.client.audio.line;

import su.plo.voice.api.audio.line.PlayerMap;
import su.plo.voice.proto.data.audio.line.SourceLine;

public interface ClientSourceLine extends SourceLine, PlayerMap {

    void setVolume(double volume);

    double getVolume();
}
