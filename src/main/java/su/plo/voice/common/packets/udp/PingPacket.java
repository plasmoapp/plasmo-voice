package su.plo.voice.common.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;

public class PingPacket implements Packet {
    public PingPacket() {}

    @Override
    public void read(ByteArrayDataInput buf) throws IOException {}

    @Override
    public void write(ByteArrayDataOutput buf) throws IOException {}
}