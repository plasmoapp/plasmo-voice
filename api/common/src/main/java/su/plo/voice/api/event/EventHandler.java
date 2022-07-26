package su.plo.voice.api.event;

@FunctionalInterface
public interface EventHandler<E extends Event> {

    void execute(E event);
}
