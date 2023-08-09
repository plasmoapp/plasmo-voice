package su.plo.voice.audio.codec.opus;

import su.plo.voice.api.audio.codec.AudioEncoder;

interface BaseOpusEncoder extends AudioEncoder {

    void setBitrate(int bitrate);

    int getBitrate();
}
