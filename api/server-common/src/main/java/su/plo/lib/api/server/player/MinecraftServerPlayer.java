package su.plo.lib.api.server.player;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.voice.proto.data.player.MinecraftGameProfile;

import java.util.UUID;

/**
 * Represents "proxy" player to an actual server player object
 */
public interface MinecraftServerPlayer extends MinecraftCommandSource {

    /**
     * @return true if player is online
     */
    boolean isOnline();

    /**
     * @return creates a new instance of {@link MinecraftGameProfile} from player info
     */
    @NotNull MinecraftGameProfile getGameProfile();

    /**
     * @return player unique id
     */
    @NotNull UUID getUUID();

    /**
     * @return player name
     */
    @NotNull String getName();

    /**
     * Sends the packet to specified channel
     */
    void sendPacket(@NotNull String channel, byte[] data);

    /**
     * Kicks the player with specified reason
     */
    void kick(@NotNull MinecraftTextComponent reason);

    /**
     * Gets the backed entity object
     */
    <T> T getInstance();
}
