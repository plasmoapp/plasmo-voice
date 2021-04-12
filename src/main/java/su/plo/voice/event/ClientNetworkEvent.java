package su.plo.voice.event;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import su.plo.voice.Voice;

import java.nio.charset.StandardCharsets;

public class ClientNetworkEvent {
    private final Minecraft minecraft;

    public ClientNetworkEvent() {
        this.minecraft = Minecraft.getInstance();
    }

    @SubscribeEvent
    public void connectEvent(ClientPlayerNetworkEvent.LoggedInEvent event) {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeBytes(("plasmo:voice").getBytes(StandardCharsets.US_ASCII));
        event.getPlayer().connection.send(new CCustomPayloadPacket(new ResourceLocation("register"), buf));
    }

    @SubscribeEvent
    public void disconnectEvent(WorldEvent.Unload event) {
        // Not just changing the world - Disconnecting
        if (minecraft.gameMode == null) {
            Voice.disconnect();
        }
    }
}
