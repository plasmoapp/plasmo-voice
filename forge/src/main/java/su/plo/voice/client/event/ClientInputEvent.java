package su.plo.voice.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import su.plo.voice.client.VoiceClientForge;
import su.plo.voice.client.gui.VoiceNotAvailableScreen;
import su.plo.voice.client.gui.VoiceSettingsScreen;

public class ClientInputEvent {
    private final Minecraft client = Minecraft.getInstance();

    @SubscribeEvent
    public void onKeyMappingsRegister(RegisterKeyMappingsEvent event) {
        event.register(VoiceClientForge.menuKey);
    }

    @SubscribeEvent
    public void onInput(InputEvent.Key event) {
        final LocalPlayer player = client.player;
        if (player == null) {
            return;
        }

        if (!VoiceClientForge.isConnected()) {
            // Voice not available
            if (VoiceClientForge.menuKey.consumeClick()) {
                VoiceNotAvailableScreen screen = new VoiceNotAvailableScreen();
                if (VoiceClientForge.socketUDP != null) {
                    if (VoiceClientForge.socketUDP.isTimedOut()) {
                        screen.setConnecting();
                    } else if (!VoiceClientForge.socketUDP.isAuthorized()) {
                        screen.setConnecting();
                    } else {
                        screen.setCannotConnect();
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
