package su.plo.voice.api.audio.codec;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.util.Params;
import su.plo.voice.proto.data.audio.codec.CodecInfo;

public interface CodecSupplier<Encoder extends AudioEncoder, Decoder extends AudioDecoder> {

    @NotNull Encoder createEncoder(int sampleRate,
                                   boolean stereo,
                                   int bufferSize,
                                   int mtuSize,
                                   @NotNull CodecInfo codecInfo);

    @NotNull Decoder createDecoder(int sampleRate,
                                   boolean stereo,
                                   int bufferSize,
                                   int mtuSize,
                                   @NotNull CodecInfo codecInfo);

    @NotNull String getName();
}
