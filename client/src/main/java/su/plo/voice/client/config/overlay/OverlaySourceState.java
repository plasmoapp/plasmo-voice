package su.plo.voice.client.config.overlay;

public enum OverlaySourceState {

    OFF,
    ON,
    WHEN_TALKING,
    ALWAYS,
    NEVER;

    public boolean isProximityOnly() {
        return this == OFF || this == ON;
    }
}
