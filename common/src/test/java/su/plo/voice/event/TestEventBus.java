package su.plo.voice.event;

import org.junit.jupiter.api.Test;
import su.plo.voice.addon.TestAddon;
import su.plo.voice.api.event.EventBus;
import su.plo.voice.api.event.EventHandler;
import su.plo.voice.api.event.EventPriority;

public class TestEventBus {
    @Test
    void testEventBus() {
        EventBus bus = new VoiceEventBus();

        TestAddon addon = new TestAddon();

        TestEventListener eventListener = new TestEventListener();
        bus.register(addon, eventListener);

        EventHandler<TestEvent> eventHandler = (event) -> {
            System.out.println("event called");
        };

        bus.register(addon, TestEvent.class, EventPriority.HIGHEST, eventHandler);

        bus.call(new TestEvent());
    }


}
