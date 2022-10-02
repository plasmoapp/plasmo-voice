package su.plo.lib.server.world;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.pos.Pos3d;

import static com.google.common.base.Preconditions.checkNotNull;

@NoArgsConstructor
@Data
public class ServerPos3d {

    private MinecraftServerWorld world;

    private double x;
    private double y;
    private double z;

    private float yaw;
    private float pitch;

    public ServerPos3d(@NotNull MinecraftServerWorld world, double x, double y, double z) {
        this(world, x, y, z, 0, 0);
    }

    public ServerPos3d(@NotNull MinecraftServerWorld world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;

        this.x = x;
        this.y = y;
        this.z = z;

        this.yaw = yaw;
        this.pitch = pitch;
    }

    public double distanceSquared(@NotNull ServerPos3d o) {
        checkNotNull(o.getWorld(), "Cannot measure distance to a null world");
        checkNotNull(getWorld(), "Cannot measure distance to a null world");

        if (!o.getWorld().equals(world)) {
            throw new IllegalArgumentException("Cannot measure distance between worlds");
        }

        double xDiff = x - o.x;
        double yDiff = y - o.y;
        double zDiff = z - o.z;

        return (xDiff * xDiff) + (yDiff * yDiff) + (zDiff * zDiff);
    }

    public Pos3d toPosition() {
        return new Pos3d(x, y, z);
    }

    public Pos3d getLookAngle() {
        Pos3d pos = new Pos3d();

        double rotX = this.getYaw();
        double rotY = this.getPitch();

        pos.setY(-Math.sin(Math.toRadians(rotY)));

        double xz = Math.cos(Math.toRadians(rotY));

        pos.setX(-xz * Math.sin(Math.toRadians(rotX)));
        pos.setZ(xz * Math.cos(Math.toRadians(rotX)));

        return pos;
    }
}
