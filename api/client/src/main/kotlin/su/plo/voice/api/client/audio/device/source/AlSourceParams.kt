package su.plo.voice.api.client.audio.device.source

import kotlin.jvm.internal.DefaultConstructorMarker

/**
 * Params for [AlSource].
 */
data class AlSourceParams(
    val numBuffers: Int?,
) : DeviceSourceParams {

    constructor() : this(null)

    @Deprecated("Binary compatibility", level = DeprecationLevel.HIDDEN)
    constructor(
        numBuffers: Int?,
        defaultsMask: Int,
        marker: DefaultConstructorMarker?
    ) : this(if (defaultsMask and 0x1 != 0) null else numBuffers)

    companion object {

        @JvmField
        val DEFAULT = AlSourceParams()
    }
}
