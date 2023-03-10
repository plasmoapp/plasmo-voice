package su.plo.voice.proto.packets.tcp.clientbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.proto.data.pos.Pos3d;
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
    @Getter
    private @Nullable Pos3d position;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.radius = in.readInt();
        this.hexColor = in.readInt();

        if (in.readBoolean()) {
            this.position = new Pos3d();
            position.deserialize(in);
        }
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        out.writeInt(radius);
        out.writeInt(hexColor);

        out.writeBoolean(position != null);
        if (position != null) position.serialize(out);
    }

    @Override
    public void handle(ClientPacketTcpHandler handler) {
        handler.handle(this);
    }
}
