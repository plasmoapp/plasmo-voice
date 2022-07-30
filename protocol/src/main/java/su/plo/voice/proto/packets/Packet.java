package su.plo.voice.proto.packets;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.io.IOException;

public interface Packet<T> {

    void read(ByteArrayDataInput in) throws IOException;

    void write(ByteArrayDataOutput out) throws IOException;

    void handle(T listener);
}
