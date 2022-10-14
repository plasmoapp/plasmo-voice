package su.plo.voice.api.server.mute;

public enum MuteDurationUnit {

    SECOND(1_000L),
    MINUTE(SECOND.msDuration * 60),
    HOUR(MINUTE.msDuration * 60),
    DAY(HOUR.msDuration * 24),
    WEEK(DAY.msDuration * 7),
    TIMESTAMP(1L);

    private final long msDuration;

    MuteDurationUnit(long msDuration) {
        this.msDuration = msDuration;
    }

    public long multiply(long duration) {
        return duration * msDuration;
    }
}
