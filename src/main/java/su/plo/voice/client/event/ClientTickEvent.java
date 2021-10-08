package su.plo.voice.client.event;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import su.plo.voice.client.VoiceClient;

public class ClientTickEvent {
    @SubscribeEvent
    public void onTick(TickEvent e) {
        VoiceClient.tick();
    }
}
