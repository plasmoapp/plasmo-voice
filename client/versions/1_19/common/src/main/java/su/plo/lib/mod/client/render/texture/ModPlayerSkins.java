package su.plo.lib.mod.client.render.texture;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.client.render.texture.MinecraftPlayerSkins;
import su.plo.voice.proto.data.player.MinecraftGameProfile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class ModPlayerSkins implements MinecraftPlayerSkins {

    private final Minecraft minecraft = Minecraft.getInstance();

    private final Cache<String, ResourceLocation> skins = CacheBuilder
            .newBuilder()
            .expireAfterAccess(15L, TimeUnit.SECONDS)
            .build();

    @Override
    public synchronized void loadSkin(@NotNull UUID playerId,
                                                           @NotNull String nick,
                                                           @Nullable String fallback) {
        PlayerInfo playerInfo = minecraft.player.connection.getPlayerInfo(playerId);
        if (playerInfo != null) return;

        ResourceLocation skinLocation = skins.getIfPresent(nick);
        if (skinLocation != null) return;

        if (fallback != null) {
            RenderSystem.recordRenderCall(() -> {
                ResourceLocation fallbackIdentifier = new ResourceLocation(
                        "plasmovoice",
                        "skins/" + Hashing.sha1().hashUnencodedChars(nick.toLowerCase())
                );
                try {
                    minecraft.getTextureManager().register(
                            fallbackIdentifier,
                            new DynamicTexture(NativeImage.fromBase64(fallback))
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // fallback
                skins.put(nick, fallbackIdentifier);
            });
        }

        GameProfile profile = new GameProfile(playerId, nick);

        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = minecraft.getSkinManager()
                .getInsecureSkinInformation(profile);
        if (textures.isEmpty()) {
            minecraft.getSkinManager().registerSkins(
                    profile,
                    (type, identifier, texture) -> {
                        if (type.equals(MinecraftProfileTexture.Type.SKIN)) {
                            skins.put(profile.getName(), identifier);
                        }
                    }, false
            );
        } else {
            String hash = Hashing.sha1().hashUnencodedChars(textures.get(MinecraftProfileTexture.Type.SKIN).getHash()).toString();
            ResourceLocation identifier = new ResourceLocation("skins/" + hash);
            skins.put(profile.getName(), identifier);
        }
    }

    @Override
    public synchronized void loadSkin(@NotNull MinecraftGameProfile gameProfile) {
        PlayerInfo playerInfo = minecraft.player.connection.getPlayerInfo(gameProfile.getId());
        if (playerInfo != null) return;

        ResourceLocation skinLocation = skins.getIfPresent(gameProfile.getName());
        if (skinLocation != null) return;

        GameProfile profile = new GameProfile(
                gameProfile.getId(),
                gameProfile.getName()
        );
        gameProfile.getProperties().forEach((property) -> {
            profile.getProperties().put(property.getName(), new Property(
                    property.getName(),
                    property.getValue(),
                    property.getSignature()
            ));
        });

        skinLocation = minecraft.getSkinManager().getInsecureSkinLocation(profile);
        skins.put(gameProfile.getName(), skinLocation);
    }

    @Override
    public synchronized @NotNull String getSkin(@NotNull UUID playerId, @NotNull String nick) {
        PlayerInfo playerInfo = minecraft.player.connection.getPlayerInfo(playerId);
        if (playerInfo != null) {
            return playerInfo.getSkinLocation().toString();
        }

        ResourceLocation skinLocation = skins.getIfPresent(nick);
        if (skinLocation != null) return skinLocation.toString();

        return getDefaultSkin(playerId);
    }

    @Override
    public @NotNull String getDefaultSkin(@NotNull UUID playerId) {
        return DefaultPlayerSkin.getDefaultSkin(playerId).toString();
    }
}
