package su.plo.voice.proto.data.pos;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Data;
import lombok.NoArgsConstructor;
import su.plo.voice.proto.packets.PacketSerializable;

@NoArgsConstructor
@Data
public class Pos3d implements PacketSerializable {

    private double x;
    private double y;
    private double z;

    public Pos3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void deserialize(ByteArrayDataInput in) {
        this.x = in.readDouble();
        this.y = in.readDouble();
        this.z = in.readDouble();
    }

    @Override
    public void serialize(ByteArrayDataOutput out) {
        out.writeDouble(x);
        out.writeDouble(y);
        out.writeDouble(z);
    }
}
