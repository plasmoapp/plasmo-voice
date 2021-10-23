package su.plo.voice.client.utils;

import lombok.Getter;

public class CircularFloatBuffer {
    @Getter
    private final float[] buffer;
    private int i;

    public CircularFloatBuffer(int capacity, float defaultValue) {
        this.buffer = new float[capacity];
        for (int j = 0; j < capacity; j++) {
            buffer[j] = defaultValue;
        }
    }

    public void put(float f) {
        buffer[i] = f;
        i = (i + 1) % buffer.length;
    }
}
