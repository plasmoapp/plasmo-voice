package su.plo.voice.client.event.gui;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.client.gui.MicrophoneTestController;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fires when the microphone test is stopped
 */
public final class MicrophoneTestStoppedEvent implements Event {

    @Getter
    private final MicrophoneTestController controller;

    public MicrophoneTestStoppedEvent(@NotNull MicrophoneTestController controller) {
        this.controller = checkNotNull(controller, "controller");
    }
}
