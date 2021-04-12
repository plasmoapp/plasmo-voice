package su.plo.voice.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import su.plo.voice.commands.ForgeClientCommandSource;

import java.util.UUID;

@Mixin(ClientSuggestionProvider.class)
public abstract class MixinCommandSource implements ForgeClientCommandSource {
    @Shadow @Final private Minecraft minecraft;

    @Override
    public void sendFeedback(ITextComponent message) {
        minecraft.gui.handleChat(ChatType.SYSTEM, message, new UUID(0L, 0L));
    }

    @Override
    public void sendError(ITextComponent message) {
        minecraft.gui.handleChat(ChatType.SYSTEM, new StringTextComponent("").append(message).withStyle(TextFormatting.RED), new UUID(0L, 0L));
    }

    @Override
    public Minecraft getMinecraft() {
        return minecraft;
    }

    @Override
    public ClientPlayerEntity getPlayer() {
        return minecraft.player;
    }

    @Override
    public ClientWorld getWorld() {
        return minecraft.level;
    }
}
