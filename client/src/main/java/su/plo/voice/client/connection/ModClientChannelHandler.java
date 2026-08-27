package su.plo.voice.client.connection;

import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketDirection;
import su.plo.voice.proto.packets.PacketHandler;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;

import java.util.Optional;

//#if FABRIC

import net.minecraft.client.multiplayer.ClientPacketListener;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import su.plo.slib.mod.channel.ByteArrayPayload;

//#elseif NEOFORGE
//$$
//$$ import net.neoforged.neoforge.network.handling.IPayloadContext;
//$$ import net.neoforged.neoforge.network.handling.IPayloadHandler;
//$$ import su.plo.slib.mod.channel.ByteArrayPayload;
//$$
//#endif

public final class ModClientChannelHandler
        //#if FABRIC
        implements ClientPlayNetworking.PlayPayloadHandler<ByteArrayPayload>
        //#endif

        //#if NEOFORGE
        //$$ implements IPayloadHandler<ByteArrayPayload>
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
    public void receive(ByteArrayPayload payload, ClientPlayNetworking.Context context) {
        ClientPacketListener listener = context.client().getConnection();
        if (listener == null) return;
        receive(listener.getConnection(), payload.getData());
    }

    //#elseif NEOFORGE
    //$$
    //$$ @Override
    //$$ public void handle(@NotNull ByteArrayPayload payload, @NotNull IPayloadContext context) {
    //$$     receive(context.connection(), payload.getData());
    //$$ }
    //$$
    //#endif

    private void receive(Connection connection, Packet<PacketHandler> packet) {
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

        this.connection.handle(packet);
    }

    private void receive(Connection connection, FriendlyByteBuf buf) {
        receive(connection, ByteBufUtil.getBytes(buf.duplicate()));
    }

    private void receive(Connection connection, byte[] data) {
        try {
            PacketTcpCodec.decode(ByteStreams.newDataInput(data), PacketDirection.CLIENT)
                    .ifPresent(packet -> receive(connection, packet));
        } catch (Throwable e) {
            BaseVoice.LOGGER.warn("Failed to decode packet", e);
        }
    }
}
