package su.plo.lib.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.client.gui.MinecraftFont;
import su.plo.lib.client.gui.screen.GuiScreen;
import su.plo.lib.client.locale.MinecraftLanguage;
import su.plo.lib.client.render.MinecraftTesselator;
import su.plo.lib.client.render.MinecraftWindow;
import su.plo.lib.client.render.particle.MinecraftParticles;
import su.plo.lib.client.render.texture.MinecraftPlayerSkins;
import su.plo.lib.client.sound.MinecraftSoundManager;
import su.plo.voice.chat.TextConverter;
import su.plo.voice.client.gui.ScreenContainer;
import su.plo.voice.client.player.ClientPlayer;

import java.util.Optional;

public interface MinecraftClientLib {

    TextConverter<?> getTextConverter();

    Optional<ClientPlayer> getClientPlayer();

    Optional<ScreenContainer> getScreen();

    void setScreen(@Nullable GuiScreen screen);

    @NotNull MinecraftFont getFont();

    @NotNull MinecraftLanguage getLanguage();

    @NotNull MinecraftSoundManager getSoundManager();

    @NotNull MinecraftTesselator getTesselator();

    @NotNull MinecraftWindow getWindow();

    @NotNull MinecraftParticles getSimpleParticles();

    @NotNull MinecraftPlayerSkins getPlayerSkins();
}
