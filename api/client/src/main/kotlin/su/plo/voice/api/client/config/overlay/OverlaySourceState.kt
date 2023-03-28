package su.plo.voice.api.client.config.overlay

enum class OverlaySourceState {

    OFF,
    ON,
    WHEN_TALKING,
    ALWAYS,
    NEVER;

    val isProximityOnly: Boolean
        get() = this == OFF || this == ON
}
