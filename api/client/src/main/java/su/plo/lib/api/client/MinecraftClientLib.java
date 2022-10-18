package su.plo.lib.api.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.chat.MinecraftTextConverter;
import su.plo.lib.api.client.connection.MinecraftServerConnection;
import su.plo.lib.api.client.entity.MinecraftClientPlayer;
import su.plo.lib.api.client.gui.MinecraftFont;
import su.plo.lib.api.client.gui.ScreenContainer;
import su.plo.lib.api.client.gui.screen.GuiScreen;
import su.plo.lib.api.client.locale.MinecraftLanguage;
import su.plo.lib.api.client.render.MinecraftTesselator;
import su.plo.lib.api.client.render.MinecraftWindow;
import su.plo.lib.api.client.render.particle.MinecraftParticles;
import su.plo.lib.api.client.render.texture.MinecraftPlayerSkins;
import su.plo.lib.api.client.sound.MinecraftSoundManager;
import su.plo.lib.api.client.world.MinecraftClientWorld;

import java.util.Optional;

public interface MinecraftClientLib {

    default void onInitialize() {
    }

    default void onShutdown() {
    }

    default void onServerDisconnect() {
    }

    MinecraftTextConverter<?> getTextConverter();

    Optional<MinecraftClientPlayer> getClientPlayer();

    Optional<MinecraftClientWorld> getWorld();

    Optional<MinecraftServerConnection> getConnection();

    Optional<ScreenContainer> getScreen();

    void setScreen(@Nullable GuiScreen screen);

    @NotNull MinecraftFont getFont();

    @NotNull MinecraftLanguage getLanguage();

    @NotNull MinecraftSoundManager getSoundManager();

    @NotNull MinecraftTesselator getTesselator();

    @NotNull MinecraftWindow getWindow();

    @NotNull MinecraftParticles getSimpleParticles();

    @NotNull MinecraftPlayerSkins getPlayerSkins();

    @NotNull MinecraftOptions getOptions();
}
