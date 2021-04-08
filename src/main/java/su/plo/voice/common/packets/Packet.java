package su.plo.voice.common.packets;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.io.IOException;

public interface Packet {

    void read(ByteArrayDataInput buf) throws IOException;

    void write(ByteArrayDataOutput buf) throws IOException;

}