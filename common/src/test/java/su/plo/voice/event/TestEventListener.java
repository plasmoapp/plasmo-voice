package su.plo.voice.event;

import su.plo.voice.api.event.EventPriority;
import su.plo.voice.api.event.EventSubscribe;

public class TestEventListener {
    @EventSubscribe
    public void onTestEvent(TestEvent event) {
        System.out.println("test event called normal");
    }

    @EventSubscribe(priority = EventPriority.HIGHEST)
    public void onTestEventHigh(TestEvent event) {
        System.out.println("test event called highest");
    }
}
