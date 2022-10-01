package su.plo.voice.lib.client;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.MinecraftOptions;
import su.plo.lib.client.entity.MinecraftClientPlayer;
import su.plo.lib.client.gui.MinecraftFont;
import su.plo.lib.client.gui.screen.GuiScreen;
import su.plo.lib.client.locale.MinecraftLanguage;
import su.plo.lib.client.render.MinecraftTesselator;
import su.plo.lib.client.render.MinecraftWindow;
import su.plo.lib.client.render.particle.MinecraftParticles;
import su.plo.lib.client.render.texture.MinecraftPlayerSkins;
import su.plo.lib.client.sound.MinecraftSoundManager;
import su.plo.voice.chat.ComponentTextConverter;
import su.plo.voice.chat.TextConverter;
import su.plo.voice.client.entity.ModClientPlayer;
import su.plo.voice.client.gui.ScreenContainer;
import su.plo.voice.lib.client.gui.ModFontWrapper;
import su.plo.voice.lib.client.gui.ModScreenWrapper;
import su.plo.voice.lib.client.locale.ModLanguageWrapper;
import su.plo.voice.lib.client.render.ModTesselator;
import su.plo.voice.lib.client.render.ModWindow;
import su.plo.voice.lib.client.render.particle.ModSimpleParticles;
import su.plo.voice.lib.client.render.texture.ModPlayerSkins;
import su.plo.voice.lib.client.sound.ModSoundManager;
import su.plo.voice.lib.client.texture.ResourceCache;

import java.util.Optional;

public final class ModClientLib implements MinecraftClientLib {

    private final Minecraft minecraft = Minecraft.getInstance();

    @Getter
    private final TextConverter<Component> textConverter = new ComponentTextConverter();
    @Getter
    private final MinecraftFont font = new ModFontWrapper(textConverter);
    @Getter
    private final MinecraftLanguage language = new ModLanguageWrapper();
    @Getter
    private final MinecraftSoundManager soundManager = new ModSoundManager();
    @Getter
    private final MinecraftTesselator tesselator = new ModTesselator();
    @Getter
    private final MinecraftWindow window = new ModWindow();
    @Getter
    private final MinecraftParticles simpleParticles = new ModSimpleParticles();
    @Getter
    private final MinecraftPlayerSkins playerSkins = new ModPlayerSkins();
    @Getter
    private final MinecraftOptions options = new ModOptions();

    @Getter
    private final ResourceCache resources = new ResourceCache();

    @Override
    public Optional<MinecraftClientPlayer> getClientPlayer() {
        if (minecraft.player == null) return Optional.empty();

        return Optional.of(new ModClientPlayer(minecraft.player, textConverter));
    }

    @Override
    public Optional<ScreenContainer> getScreen() {
        if (minecraft.screen == null)
            return Optional.empty();

        return Optional.of(new ScreenContainer(minecraft.screen));
    }

    @Override
    public void setScreen(@Nullable GuiScreen screen) {
        if (RenderSystem.isOnRenderThread()) {
            setScreenSync(screen);
        } else {
            minecraft.execute(() -> setScreenSync(screen));
        }
    }

    private void setScreenSync(@Nullable GuiScreen screen) {
        if (screen == null) {
            minecraft.setScreen(null);
            return;
        }

        screen.setMinecraftScreen(new ModScreenWrapper(this, screen));

        minecraft.setScreen((Screen) screen.getMinecraftScreen());
    }
}
