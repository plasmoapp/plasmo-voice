package su.plo.voice.client.connection;

import io.netty.buffer.Unpooled;
import lombok.Getter;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.client.ModVoiceClient;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;

public final class ModServerConnection extends BaseServerConnection {

    @Getter
    private final Connection handler;

    public ModServerConnection(@NotNull BaseVoiceClient voiceClient,
                               @NotNull MinecraftClientLib minecraft,
                               @NotNull Connection handler) throws Exception {
        super(voiceClient, minecraft);

        this.handler = handler;
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        if (!handler.isConnected()) return;

        byte[] encoded = PacketTcpCodec.encode(packet);
        if (encoded == null) return;

        handler.send(new ServerboundCustomPayloadPacket(
                ModVoiceClient.CHANNEL,
                new FriendlyByteBuf(Unpooled.wrappedBuffer(encoded))
        ));
    }
}
