package su.plo.voice.proto.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketHandler;

import java.io.IOException;
import java.util.UUID;

@ToString(exclude = {"packet", "input", "read"})
public class PacketUdp {

    @Getter
    private final UUID secret;
    @Getter
    private final long timestamp;
    private final Packet<?> packet;

    @Getter
    private ByteArrayDataInput input;
    @Getter
    private boolean read;

    public PacketUdp(@NotNull UUID secret,
                     long timestamp,
                     @NotNull Packet<?> packet,
                     @NotNull ByteArrayDataInput input) {
        this.secret = secret;
        this.timestamp = timestamp;
        this.packet = packet;
        this.input = input;
    }

    public <T extends PacketHandler> Packet<T> getPacket() throws IOException {
        if (!read) readPacket();

        return (Packet<T>) packet;
    }

    private synchronized void readPacket() throws IOException {
        if (input == null) return;

        this.read = true;
        packet.read(input);
        this.input = null;
    }
}
