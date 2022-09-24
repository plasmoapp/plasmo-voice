package su.plo.voice.client.audio.codec;

import su.plo.voice.api.audio.codec.CodecException;

public interface AudioDecoderPlc {

    short[] decodePLC() throws CodecException;
}
