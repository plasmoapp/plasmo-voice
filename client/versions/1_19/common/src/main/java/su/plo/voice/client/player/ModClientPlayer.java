package su.plo.voice.client.player;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.chat.Text;
import su.plo.voice.chat.TextConverter;

@RequiredArgsConstructor
public final class ModClientPlayer implements ClientPlayer {

    private final Minecraft minecraft = Minecraft.getInstance();
    private final LocalPlayer player;
    private final TextConverter<Component> textConverter;

    @Override
    public void sendChatMessage(@NotNull Text text) {
        player.sendSystemMessage(textConverter.convert(text));
    }

    @Override
    public void sendActionbarMessage(@NotNull Text text) {
        minecraft.gui.setOverlayMessage(textConverter.convert(text), false);
    }
}
