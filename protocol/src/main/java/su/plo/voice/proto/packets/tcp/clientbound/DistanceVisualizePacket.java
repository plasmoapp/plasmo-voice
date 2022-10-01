package su.plo.voice.proto.packets.tcp.clientbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.packets.Packet;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
@ToString
public final class DistanceVisualizePacket implements Packet<ClientPacketTcpHandler> {

    @Getter
    private int radius;
    @Getter
    private int hexColor;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.radius = in.readInt();
        this.hexColor = in.readInt();
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        out.writeInt(radius);
        out.writeInt(hexColor);
    }

    @Override
    public void handle(ClientPacketTcpHandler handler) {
        handler.handle(this);
    }
}
