package su.plo.lib.api.server.world;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.entity.MinecraftServerEntity;

public interface MinecraftServerWorld {

    /**
     * @return world key
     */
    @NotNull String getKey();

    /**
     * Sends game event to the world
     *
     * <p>
     *     If specified game event is invalid, minecraft:step will be sent by default
     * </p>
     *
     * @since minecraft 1.19?
     *
     * @param gameEvent <a href="https://minecraft.fandom.com/wiki/Sculk_Sensor#Redstone_emission">Minecraft Game Event</a>,
     *                 e.g <b>minecraft:step</b>
     **/
    void sendGameEvent(@NotNull MinecraftServerEntity entity, @NotNull String gameEvent);

    /**
     * Gets the server's implementation instance
     * <ul>
     * <li>{@code org.bukkit.World} for bukkit</li>
     * <li>{@code net.minecraft.server.level.ServerLevel} for mods (fabric/forge)</li>
     * </ul>
     *
     * @return server's implementation object
     */
    <T> T getInstance();
}
