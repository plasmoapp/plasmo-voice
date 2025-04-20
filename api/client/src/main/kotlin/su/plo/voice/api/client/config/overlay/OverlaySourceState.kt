package su.plo.voice.api.client.config.overlay

/**
 * State of the overlay source.
 */
enum class OverlaySourceState {

    OFF,
    ON,
    WHEN_TALKING,
    ALWAYS,
    NEVER;

    val isProximityOnly: Boolean
        get() = this == OFF || this == ON

    fun toBoolean(): Boolean =
        this == ON

    companion object {
        @JvmStatic
        fun fromBoolean(boolean: Boolean): OverlaySourceState =
            if (boolean) ON else OFF
    }
}
