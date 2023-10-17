package su.plo.voice.api.client.audio.device.source

/**
 * Params for [AlSource].
 */
data class AlSourceParams(
    val numBuffers: Int? = null
) : DeviceSourceParams {

    companion object {

        @JvmField
        val DEFAULT = AlSourceParams()
    }
}
