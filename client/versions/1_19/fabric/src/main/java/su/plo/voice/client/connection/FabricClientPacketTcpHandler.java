package su.plo.voice.client.connection;

import com.google.common.io.ByteStreams;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.logging.log4j.LogManager;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;

import java.io.IOException;

public final class FabricClientPacketTcpHandler extends BaseClientPacketTcpHandler implements ClientPlayNetworking.PlayChannelHandler {

    public FabricClientPacketTcpHandler(BaseVoiceClient voiceClient) {
        super(voiceClient);
    }

    @Override
    public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        byte[] data = new byte[buf.readableBytes()];
        buf.duplicate().readBytes(data);

        try {
            PacketTcpCodec.decode(ByteStreams.newDataInput(data))
                            .ifPresent(packet -> {
                                LogManager.getLogger().info("packet received {}", packet);
                                packet.handle(this);
                            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
