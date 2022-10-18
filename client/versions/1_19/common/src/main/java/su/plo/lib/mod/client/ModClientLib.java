package su.plo.lib.mod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.chat.MinecraftTextConverter;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.lib.api.client.MinecraftOptions;
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
import su.plo.lib.mod.client.connection.ModServerConnection;
import su.plo.lib.mod.client.gui.ModFontWrapper;
import su.plo.lib.mod.client.gui.ModScreenWrapper;
import su.plo.lib.mod.client.locale.ModLanguageWrapper;
import su.plo.lib.mod.client.render.ModTesselator;
import su.plo.lib.mod.client.render.ModWindow;
import su.plo.lib.mod.client.render.particle.ModSimpleParticles;
import su.plo.lib.mod.client.render.texture.ModPlayerSkins;
import su.plo.lib.mod.client.sound.ModSoundManager;
import su.plo.lib.mod.client.texture.ResourceCache;
import su.plo.lib.mod.client.world.ModClientWorld;
import su.plo.lib.mod.entity.ModClientPlayer;
import su.plo.voice.mod.chat.ComponentTextConverter;

import java.util.Optional;

public final class ModClientLib implements MinecraftClientLib {

    private final Minecraft minecraft = Minecraft.getInstance();

    @Getter
    private final MinecraftTextConverter<Component> textConverter = new ComponentTextConverter();
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

    private @Nullable MinecraftClientPlayer clientPlayer;
    private @Nullable ModClientWorld world;
    private @Nullable ModServerConnection connection;

    @Override
    public void onServerDisconnect() {
        this.clientPlayer = null;
        this.world = null;
        this.connection = null;
    }

    @Override
    public Optional<MinecraftClientPlayer> getClientPlayer() {
        if (clientPlayer == null || minecraft.player != clientPlayer.getInstance()) {
            if (minecraft.player == null) {
                this.clientPlayer = null;
            } else {
                this.clientPlayer = new ModClientPlayer(minecraft.player, textConverter);
            }
        }

        return Optional.ofNullable(clientPlayer);
    }

    @Override
    public Optional<MinecraftClientWorld> getWorld() {
        if (world == null || minecraft.level != world.getLevel()) {
            if (minecraft.level == null) {
                this.world = null;
            } else {
                this.world = new ModClientWorld(minecraft.level);
            }
        }

        return Optional.ofNullable(world);
    }

    @Override
    public Optional<MinecraftServerConnection> getConnection() {
        if (connection == null || minecraft.getConnection() != connection.getConnection()) {
            if (minecraft.level == null) {
                this.connection = null;
            } else {
                this.connection = new ModServerConnection(minecraft.getConnection());
            }
        }

        return Optional.ofNullable(connection);
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
