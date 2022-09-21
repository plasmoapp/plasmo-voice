package su.plo.voice.api.audio.line;

import su.plo.voice.proto.data.audio.line.SourceLine;

public interface ClientSourceLine extends SourceLine {

    void setVolume(double volume);

    double getVolume();
}
