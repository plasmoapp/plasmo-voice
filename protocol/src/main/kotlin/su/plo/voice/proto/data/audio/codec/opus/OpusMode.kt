package su.plo.voice.proto.data.audio.codec.opus

enum class OpusMode(val application: Int) {

    VOIP(2048),
    AUDIO(2049),
    RESTRICTED_LOWDELAY(2051);
}
