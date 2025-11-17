package su.plo.voice.event;

import su.plo.voice.api.event.EventPriority;
import su.plo.voice.api.event.EventSubscribe;

import java.util.ArrayList;
import java.util.List;

public class TestEventListener {
    public final List<String> calls = new ArrayList<>();

    @EventSubscribe
    public void onTestEvent(TestEvent event) {
        calls.add("normal");
    }

    @EventSubscribe(priority = EventPriority.HIGHEST)
    public void onTestEventHigh(TestEvent event) {
        calls.add("highest");
    }
}
