package su.plo.voice.client.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public interface ForgeClientCommandSource extends SharedSuggestionProvider {
    /**
     * Sends a feedback message to the player.
     *
     * @param message the feedback message
     */
    void sendFeedback(Component message);

    /**
     * Sends an error message to the player.
     *
     * @param message the error message
     */
    void sendError(Component message);

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
    LocalPlayer getPlayer();

    /**
     * Gets the world where the player used the command.
     *
     * @return the world
     */
    Level getWorld();
}
