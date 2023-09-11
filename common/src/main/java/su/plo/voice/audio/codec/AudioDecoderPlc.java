package su.plo.voice.audio.codec;

import su.plo.voice.api.audio.codec.CodecException;

public interface AudioDecoderPlc {

    short[] decodePLC() throws CodecException;
}
