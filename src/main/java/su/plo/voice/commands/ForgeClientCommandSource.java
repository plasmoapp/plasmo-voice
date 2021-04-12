package su.plo.voice.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.ITextComponent;

public interface ForgeClientCommandSource extends ISuggestionProvider {
    /**
     * Sends a feedback message to the player.
     *
     * @param message the feedback message
     */
    void sendFeedback(ITextComponent message);

    /**
     * Sends an error message to the player.
     *
     * @param message the error message
     */
    void sendError(ITextComponent message);

    /**
     * Gets the client instance used to run the command.
     *
     * @return the client
     */
    Minecraft getMinecraft();

    /**
     * Gets the player that used the command.
     *
     * @return the player
     */
    ClientPlayerEntity getPlayer();

    /**
     * Gets the world where the player used the command.
     *
     * @return the world
     */
    ClientWorld getWorld();
}
