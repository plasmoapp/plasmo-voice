package su.plo.voice.api.client.event.audio.source;

import lombok.Getter;
import su.plo.voice.api.client.audio.source.ClientAudioSource;
import su.plo.voice.api.event.Event;

/**
 * This event is fired once the {@link ClientAudioSource} has been initialized
 */
public class AudioSourceInitializedEvent implements Event {

    @Getter
    protected final ClientAudioSource<?> source;

    public AudioSourceInitializedEvent(ClientAudioSource<?> source) {
        this.source = source;
    }
}
