package su.plo.voice.common.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;

public class AuthPacketAck implements Packet {
    public AuthPacketAck() {}

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {}

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {}
}
