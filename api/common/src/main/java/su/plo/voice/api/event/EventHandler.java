package su.plo.voice.api.event;

@FunctionalInterface
public interface EventHandler<E> {

    void execute(E event);
}
