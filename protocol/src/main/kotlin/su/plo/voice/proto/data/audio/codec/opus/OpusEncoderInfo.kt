package su.plo.voice.proto.data.audio.codec.opus

import su.plo.voice.proto.data.audio.codec.CodecInfo
import java.io.IOException
import kotlin.properties.Delegates

/**
 * About opus modes:
 * [opus_encoder_create](https://www.opus-codec.org/docs/html_api/group__opusencoder.html#gaa89264fd93c9da70362a0c9b96b9ca88)
 *
 * About bitrates:
 * [OPUS_SET_BITRATE](https://www.opus-codec.org/docs/html_api/group__encoderctls.html#ga0bb51947e355b33d0cb358463b5101a7)
 */
class OpusEncoderInfo : CodecInfo {

    constructor(mode: OpusMode, bitrate: Int) : super() {
        this.mode = mode
        this.bitrate = validateBitrate(bitrate)
        params = hashMapOf(
            "mode" to mode.toString(),
            "bitrate" to bitrate.toString()
        )
    }

    /**
     * Creates OpusEncoderInfo from CodecInfo
     *
     * @throws IOException if CodecInfo is not OpusEncoderInfo
     */
    @Throws(IOException::class)
    constructor(codecInfo: CodecInfo) : super() {
        if (codecInfo.name != "opus") throw IOException("name is not opus")
        mode = codecInfo.params["mode"]?.let { OpusMode.valueOf(it) } ?: throw IOException("mode not found in params")
        bitrate =
            codecInfo.params["bitrate"]?.let { validateStringBitrate(it) } ?: throw IOException("bad opus bitrate")
        params = codecInfo.params
    }

    var mode: OpusMode
    var bitrate by Delegates.notNull<Int>()

    init {
        name = "opus"
    }

    private fun validateStringBitrate(rawBitrate: String): Int {
        return try {
            var bitrate = rawBitrate.toInt()
            if (bitrate < 0) {
                if (bitrate != -1 && bitrate != -1000) bitrate = -1000
            } else if (bitrate > 512000) bitrate = 512000
            bitrate
        } catch (ignored: NumberFormatException) {
            -1000
        }
    }

    private fun validateBitrate(bitrate: Int): Int {
        if (bitrate < 0) {
            if (bitrate != -1 && bitrate != -1000)
                return -1000
        } else if (bitrate > 512000)
            return 512000

        return bitrate
    }
}
