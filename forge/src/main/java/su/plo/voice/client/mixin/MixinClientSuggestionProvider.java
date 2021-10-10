package su.plo.voice.client.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import su.plo.voice.client.VoiceClientForge;
import su.plo.voice.client.commands.ForgeClientCommandSource;

@Mixin(ClientSuggestionProvider.class)
public abstract class MixinClientSuggestionProvider implements ForgeClientCommandSource {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Override
    public void sendFeedback(Component message) {
        minecraft.gui.handleChat(ChatType.SYSTEM, message, VoiceClientForge.NIL_UUID);
    }

    @Override
    public void sendError(Component message) {
        minecraft.gui.handleChat(ChatType.SYSTEM, new TextComponent("").append(message).withStyle(ChatFormatting.RED), VoiceClientForge.NIL_UUID);
    }

    @Override
    public Minecraft getMinecraft() {
        return minecraft;
    }

    @Override
    public LocalPlayer getPlayer() {
        return minecraft.player;
    }

    @Override
    public Level getWorld() {
        return minecraft.level;
    }
}