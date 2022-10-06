package su.plo.lib.client.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.chat.TextComponent;
import su.plo.lib.chat.TextConverter;
import su.plo.lib.entity.ModPlayer;

public final class ModClientPlayer extends ModPlayer implements MinecraftClientPlayer {

    private final Minecraft minecraft = Minecraft.getInstance();
    private final LocalPlayer player;
    private final TextConverter<Component> textConverter;

    public ModClientPlayer(@NotNull LocalPlayer player, @NotNull TextConverter<Component> textConverter) {
        super(player);

        this.player = player;
        this.textConverter = textConverter;
    }

    @Override
    public void sendChatMessage(@NotNull TextComponent text) {
        player.sendSystemMessage(textConverter.convert(text));
    }

    @Override
    public void sendActionbarMessage(@NotNull TextComponent text) {
        minecraft.gui.setOverlayMessage(textConverter.convert(text), false);
    }
}