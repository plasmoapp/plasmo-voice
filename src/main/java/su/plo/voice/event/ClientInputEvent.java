package su.plo.voice.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import su.plo.voice.Voice;
import su.plo.voice.gui.settings.VoiceNotAvailableScreen;
import su.plo.voice.gui.settings.VoiceSettingsScreen;

public class ClientInputEvent {
    public static boolean pushToTalkPressed = false;
    public static boolean priorityPushToTalkPressed = false;

    private final Minecraft minecraft;
    public ClientInputEvent() {
        this.minecraft = Minecraft.getInstance();
    }

    @SubscribeEvent
    public void onInput(InputEvent.KeyInputEvent event) {
        final PlayerEntity player = minecraft.player;
        if(player == null) {
            return;
        }

        InputMappings.Input boundKey = Voice.pushToTalk.getKey();
        if (!boundKey.getType().equals(InputMappings.Type.MOUSE)) {
            pushToTalkPressed = InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), boundKey.getValue());
        }

        boundKey = Voice.priorityPushToTalk.getKey();
        if(boundKey.getValue() >= 0) {
            if (!boundKey.getType().equals(InputMappings.Type.MOUSE)) {
                priorityPushToTalkPressed = InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), boundKey.getValue());
            }
        }

        if(Voice.socketUDP == null || Voice.serverConfig == null) {
            if(Voice.menuKey.consumeClick()) {
                minecraft.setScreen(new VoiceNotAvailableScreen(new TranslationTextComponent("gui.plasmo_voice.not_available"), minecraft));
            }

            return;
        }

        if(Voice.menuKey.consumeClick()) {
            minecraft.setScreen(new VoiceSettingsScreen());
        }
    }

    @SubscribeEvent
    public void onMousePushToTalk(InputEvent.RawMouseEvent event) {
        InputMappings.Input boundKey = Voice.pushToTalk.getKey();
        if (!boundKey.getType().equals(InputMappings.Type.MOUSE)) {
            return;
        }
        if (boundKey.getValue() != event.getButton()) {
            return;
        }
        pushToTalkPressed = event.getAction() != 0;
    }

    @SubscribeEvent
    public void onMousePriorityPushToTalk(InputEvent.RawMouseEvent event) {
        InputMappings.Input boundKey = Voice.priorityPushToTalk.getKey();
        if (!boundKey.getType().equals(InputMappings.Type.MOUSE)) {
            return;
        }
        if (boundKey.getValue() != event.getButton()) {
            return;
        }
        priorityPushToTalkPressed = event.getAction() != 0;
    }
}
