package su.plo.voice.client.connection;

import com.google.common.io.ByteStreams;
import lombok.RequiredArgsConstructor;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.logging.log4j.LogManager;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
public final class FabricClientChannelHandler implements ClientPlayNetworking.PlayChannelHandler {

    private final BaseVoiceClient voiceClient;

    private ModServerConnection connection;

    @Override
    public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        if (connection == null) {
            this.connection = new ModServerConnection(handler.getConnection(), voiceClient);
        }

        byte[] data = new byte[buf.readableBytes()];
        buf.duplicate().readBytes(data);

        try {
            PacketTcpCodec.decode(ByteStreams.newDataInput(data))
                    .ifPresent(packet -> {
                        LogManager.getLogger().info("Channel packet received {}", packet);
                        packet.handle(connection);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        this.connection = null;
    }

    public Optional<ServerConnection> getConnection() {
        return Optional.ofNullable(connection);
    }
}
