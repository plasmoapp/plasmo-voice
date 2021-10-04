package su.plo.voice.client.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import su.plo.voice.client.VoiceClientForge;

public class ClientNetworkEvent {
    private final Minecraft minecraft;

    public ClientNetworkEvent() {
        this.minecraft = Minecraft.getInstance();
    }

    @SubscribeEvent
    public void disconnectEvent(WorldEvent.Unload event) {
        // Not just changing the world - Disconnecting
        if (minecraft.gameMode == null) {
            VoiceClientForge.disconnect();
        }
    }
}
