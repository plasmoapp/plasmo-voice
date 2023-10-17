package su.plo.voice.api.server.mute;

import su.plo.slib.api.chat.component.McTextComponent;

public enum MuteDurationUnit {

    SECOND(1_000L, "pv.mutes.durations.seconds"),
    MINUTE(SECOND.msDuration * 60, "pv.mutes.durations.minutes"),
    HOUR(MINUTE.msDuration * 60, "pv.mutes.durations.hours"),
    DAY(HOUR.msDuration * 24, "pv.mutes.durations.days"),
    WEEK(DAY.msDuration * 7, "pv.mutes.durations.weeks"),
    TIMESTAMP(1L, "pv.mutes.durations.seconds");

    private final long msDuration;
    private final String translationKey;

    MuteDurationUnit(long msDuration, String translationKey) {
        this.msDuration = msDuration;
        this.translationKey = translationKey;
    }

    public long multiply(long duration) {
        return duration * msDuration;
    }

    public McTextComponent translate(long duration) {
        if (this == TIMESTAMP) {
            long diff = duration - System.currentTimeMillis();
            if (diff <= 0L) {
                throw new IllegalArgumentException("TIMESTAMP duration should be in the future");
            }

            return McTextComponent.translatable(translationKey, diff / 1000L);
        }

        return McTextComponent.translatable(translationKey, duration);
    }
}
