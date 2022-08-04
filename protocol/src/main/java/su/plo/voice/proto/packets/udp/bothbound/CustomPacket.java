package su.plo.voice.proto.packets.udp.bothbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.PacketUdpHandler;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

@NoArgsConstructor
@ToString
public class CustomPacket implements Packet<PacketUdpHandler> {

    @Getter
    private String addonId;

    @Getter
    @Setter
    private byte[] payload;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.addonId = in.readUTF();
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        checkNotNull(addonId, "addonId cannot be null");
        checkNotNull(payload, "payload cannot be null");

        out.writeUTF(addonId);
        out.writeInt(payload.length);
        out.write(payload);
    }

    @Override
    public void handle(PacketUdpHandler handler) {
        handler.handle(this);
    }
}
