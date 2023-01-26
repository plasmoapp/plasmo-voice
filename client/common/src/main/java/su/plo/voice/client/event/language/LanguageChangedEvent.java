package su.plo.voice.client.event.language;

import lombok.Getter;
import lombok.NonNull;
import su.plo.voice.api.event.Event;

/**
 * This event is fires once the language was changed
 */
public final class LanguageChangedEvent implements Event {

    @Getter
    private final String language;

    public LanguageChangedEvent(@NonNull String language) {
        this.language = language;
    }
}
