package su.plo.voice.mod.client.connection;

import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;

import java.io.IOException;
import java.util.Optional;

public abstract class ModClientChannelHandler {

    protected final BaseVoiceClient voiceClient;
    protected final MinecraftClientLib minecraft;

    protected ModServerConnection connection;

    public ModClientChannelHandler(@NotNull BaseVoiceClient voiceClient,
                                     @NotNull MinecraftClientLib minecraft) {
        this.voiceClient = voiceClient;
        this.minecraft = minecraft;
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

    protected void receive(Connection connection, FriendlyByteBuf buf) {
        if (this.connection == null || connection != this.connection.getConnection()) {
            if (this.connection != null) close();
            try {
                this.connection = new ModServerConnection(voiceClient, minecraft, connection);
                this.connection.generateKeyPair();
                voiceClient.getEventBus().register(voiceClient, this.connection);
            } catch (Exception e) {
                LogManager.getLogger().error("Failed to initialize server connection: {}", e.toString());
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
