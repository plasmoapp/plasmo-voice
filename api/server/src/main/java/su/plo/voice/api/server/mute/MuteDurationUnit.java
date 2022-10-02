package su.plo.voice.api.server.mute;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public enum MuteDurationUnit {

    SECOND(1_000L, "message.plasmovoice.mute.duration.seconds"),
    MINUTE(SECOND.msDuration * 60, "message.plasmovoice.mute.duration.minutes"),
    HOUR(MINUTE.msDuration * 60, "message.plasmovoice.mute.duration.hours"),
    DAY(HOUR.msDuration * 24, "message.plasmovoice.mute.duration.days"),
    WEEK(DAY.msDuration * 7, "message.plasmovoice.mute.duration.weeks"),
    TIMESTAMP(1L, "message.plasmovoice.mute.duration.seconds");

    private final long msDuration;
    @Getter
    private final String translation;

    MuteDurationUnit(long msDuration, @NotNull String translation) {
        this.msDuration = msDuration;
        this.translation = translation;
    }

    public long multiply(long duration) {
        return duration * msDuration;
    }
}
