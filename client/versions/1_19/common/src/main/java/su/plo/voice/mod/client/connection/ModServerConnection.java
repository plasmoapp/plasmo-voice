package su.plo.voice.mod.client.connection;

import io.netty.buffer.Unpooled;
import lombok.Getter;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.client.connection.BaseServerConnection;
import su.plo.voice.mod.client.ModVoiceClient;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;

public final class ModServerConnection extends BaseServerConnection {

    @Getter
    private final Connection connection;

    public ModServerConnection(@NotNull BaseVoiceClient voiceClient,
                               @NotNull MinecraftClientLib minecraft,
                               @NotNull Connection connection) {
        super(voiceClient, minecraft);

        this.connection = connection;
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        if (!connection.isConnected()) return;

        byte[] encoded = PacketTcpCodec.encode(packet);
        if (encoded == null) return;

        connection.send(new ServerboundCustomPayloadPacket(
                ModVoiceClient.CHANNEL,
                new FriendlyByteBuf(Unpooled.wrappedBuffer(encoded))
        ));
    }
}
