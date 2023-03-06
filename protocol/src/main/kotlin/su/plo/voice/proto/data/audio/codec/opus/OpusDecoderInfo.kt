package su.plo.voice.proto.data.audio.codec.opus

import com.google.common.io.ByteArrayDataOutput
import su.plo.voice.proto.data.audio.codec.CodecInfo
import java.io.IOException

class OpusDecoderInfo() : CodecInfo() {

    @Throws(IOException::class)
    constructor(codecInfo: CodecInfo) : this() {
        if (codecInfo.name != "opus") throw IOException("name is not opus")
    }

    init {
        name = "opus"
    }

    override fun serialize(out: ByteArrayDataOutput) {
        out.writeUTF("opus")

        out.writeInt(0)
    }
}
