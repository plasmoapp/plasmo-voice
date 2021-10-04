package su.plo.voice.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import su.plo.voice.client.VoiceClientForge;
import su.plo.voice.client.gui.VoiceNotAvailableScreen;
import su.plo.voice.client.gui.VoiceSettingsScreen;

public class ClientInputEvent {
    private final Minecraft client = Minecraft.getInstance();

    @SubscribeEvent
    public void onInput(InputEvent.KeyInputEvent event) {
        final LocalPlayer player = client.player;
        if (player == null) {
            return;
        }

        if (!VoiceClientForge.isConnected()) {
            // Voice not available
            if (VoiceClientForge.menuKey.consumeClick()) {
                VoiceNotAvailableScreen screen = new VoiceNotAvailableScreen();
                if (VoiceClientForge.socketUDP != null) {
                    if (VoiceClientForge.socketUDP.ping.timedOut) {
                        screen.setCannotConnect();
                    } else if (!VoiceClientForge.socketUDP.authorized) {
                        screen.setConnecting();
                    }
                }
                client.setScreen(screen);
            }

            return;
        }

        if (VoiceClientForge.menuKey.consumeClick()) {
            if (client.screen instanceof VoiceSettingsScreen) {
                client.setScreen(null);
            } else {
                client.setScreen(new VoiceSettingsScreen());
            }
        }
    }
}
