package su.plo.lib.api.server;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.chat.MinecraftTranslatableText;
import su.plo.lib.api.server.chat.ServerTextConverter;
import su.plo.lib.api.server.command.MinecraftCommandManager;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.lib.api.server.permission.PermissionsManager;
import su.plo.voice.api.server.config.ServerLanguages;

public interface MinecraftCommonServerLib {

    /**
     * Gets the text converter
     *
     * <p>Text converter used to convert {@link MinecraftTextComponent} to server's specific text component</p>
     *
     * <p>
     *     {@link ServerTextConverter} can translate {@link MinecraftTranslatableText} by using
     *     {@link ServerLanguages} ({@link ServerTextConverter#convert(MinecraftCommandSource, MinecraftTextComponent)})
     * </p>
     *
     * @return {@link ServerTextConverter}
     */
    @NotNull ServerTextConverter<?> getTextConverter();

    /**
     * @see MinecraftCommandManager
     */
    @NotNull MinecraftCommandManager<?> getCommandManager();

    /**
     * @see PermissionsManager
     */
    @NotNull PermissionsManager getPermissionsManager();
}
