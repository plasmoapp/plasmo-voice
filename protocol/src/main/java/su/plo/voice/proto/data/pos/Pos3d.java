package su.plo.voice.proto.data.pos;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
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

    public double distanceSquared(@NotNull Pos3d o) {
        double xDiff = x - o.x;
        double yDiff = y - o.y;
        double zDiff = z - o.z;

        return (xDiff * xDiff) + (yDiff * yDiff) + (zDiff * zDiff);
    }
}
