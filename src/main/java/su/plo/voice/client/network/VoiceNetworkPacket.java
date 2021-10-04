package su.plo.voice.client.network;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.minecraft.network.FriendlyByteBuf;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;

public class VoiceNetworkPacket {
    public static void writeToBuf(Packet packet, FriendlyByteBuf buf) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            packet.write(out);
            buf.writeBytes(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Packet readFromBuf(FriendlyByteBuf buf, Packet packet) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        try {
            packet.read(ByteStreams.newDataInput(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return packet;
    }
}
