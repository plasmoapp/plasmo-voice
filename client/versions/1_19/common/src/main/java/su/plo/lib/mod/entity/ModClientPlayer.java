package su.plo.lib.mod.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.chat.MinecraftTextConverter;
import su.plo.lib.api.client.entity.MinecraftClientPlayer;

public final class ModClientPlayer extends ModPlayer<LocalPlayer> implements MinecraftClientPlayer {

    private final Minecraft minecraft = Minecraft.getInstance();
    private final LocalPlayer player;
    private final MinecraftTextConverter<Component> textConverter;

    public ModClientPlayer(@NotNull LocalPlayer player, @NotNull MinecraftTextConverter<Component> textConverter) {
        super(player);

        this.player = player;
        this.textConverter = textConverter;
    }

    @Override
    public void sendChatMessage(@NotNull MinecraftTextComponent text) {
        player.sendSystemMessage(textConverter.convert(text));
    }

    @Override
    public void sendActionbarMessage(@NotNull MinecraftTextComponent text) {
        minecraft.gui.setOverlayMessage(textConverter.convert(text), false);
    }
}
