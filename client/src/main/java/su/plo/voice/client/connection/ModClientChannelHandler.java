package su.plo.voice.client.connection;

import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;

//#if FABRIC
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
//#else
//$$ import net.minecraftforge.network.NetworkDirection;
//$$ import net.minecraftforge.network.NetworkEvent;
//#endif

import java.io.IOException;
import java.util.Optional;

public final class ModClientChannelHandler
    //#if FABRIC
    implements ClientPlayNetworking.PlayChannelHandler
    //#endif
{

    private final BaseVoiceClient voiceClient;

    private ModServerConnection connection;

    public ModClientChannelHandler(@NotNull BaseVoiceClient voiceClient) {
        this.voiceClient = voiceClient;
    }

    public void close() {
        if (connection != null) {
            voiceClient.getEventBus().unregister(voiceClient, connection);
            this.connection = null;
        }
    }

    public Optional<ServerConnection> getConnection() {
        return Optional.ofNullable(connection);
    }

    //#if FABRIC
    @Override
    public void receive(Minecraft client,
                        ClientPacketListener handler,
                        FriendlyByteBuf buf,
                        PacketSender responseSender) {
        receive(handler.getConnection(), buf);
    }
    //#else
    //$$ public void receive(@NotNull NetworkEvent event) {
    //#if MC>=12002
    //$$     CustomPayloadEvent.Context context = event.getSource();
    //#else
    //$$     NetworkEvent.Context context = event.getSource().get();
    //#endif
    //$$     if (context.getDirection() != NetworkDirection.PLAY_TO_CLIENT || event.getPayload() == null) return;
    //#if MC>=12002
    //$$     receive(context.getConnection(), event.getPayload());
    //#else
    //$$     receive(context.getNetworkManager(), event.getPayload());
    //#endif
    //$$     context.setPacketHandled(true);
    //$$ }
    //#endif

    private void receive(Connection connection, FriendlyByteBuf buf) {
        if (this.connection == null || connection != this.connection.getConnection()) {
            if (this.connection != null) close();
            try {
                this.connection = new ModServerConnection(voiceClient, connection);
                this.connection.generateKeyPair();
                voiceClient.getEventBus().register(voiceClient, this.connection);
            } catch (Exception e) {
                BaseVoice.LOGGER.error("Failed to initialize server connection: {}", e.toString());
                e.printStackTrace();
                return;
            }
        }

        byte[] data = ByteBufUtil.getBytes(buf.duplicate());

        try {
            PacketTcpCodec.decode(ByteStreams.newDataInput(data))
                    .ifPresent(this.connection::handle);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
