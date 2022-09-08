package su.plo.voice.api.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class CircularBuffer<T> {

    private final List<T> buffer;
    private int index;

    public CircularBuffer(int capacity, T defaultValue) {
        this.buffer = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; i++) {
            buffer.add(defaultValue);
        }
    }

    public Collection<T> getCollection() {
        return buffer;
    }

    public void put(T value) {
        buffer.set(index, value);
        this.index = (index + 1) % buffer.size();
    }
}
