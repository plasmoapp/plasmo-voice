package su.plo.voice.client;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import su.plo.voice.chat.ComponentTextConverter;
import su.plo.voice.chat.TextConverter;
import su.plo.voice.client.gui.ScreenContainer;
import su.plo.voice.client.player.ClientPlayer;
import su.plo.voice.client.player.ModClientPlayer;

import java.util.Optional;

public final class ModMinecraftLib implements MinecraftClientLib {

    private final Minecraft minecraft = Minecraft.getInstance();
    @Getter
    private final TextConverter<Component> textConverter = new ComponentTextConverter();

    @Override
    public Optional<ClientPlayer> getClientPlayer() {
        if (minecraft.player == null) return Optional.empty();

        return Optional.of(new ModClientPlayer(minecraft.player, textConverter));
    }

    @Override
    public Optional<ScreenContainer> getScreen() {
        if (minecraft.screen == null)
            return Optional.empty();

        return Optional.of(new ScreenContainer(minecraft.screen));
    }
}
