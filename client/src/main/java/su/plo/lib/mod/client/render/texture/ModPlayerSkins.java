package su.plo.lib.mod.client.render.texture;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.slib.api.entity.player.McGameProfile;
import gg.essential.universal.UMinecraft;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static su.plo.lib.mod.client.render.TextureUtilKt.registerBase64Texture;

public final class ModPlayerSkins {

    private static final Cache<String, ResourceLocation> skins = CacheBuilder
            .newBuilder()
            .expireAfterAccess(15L, TimeUnit.SECONDS)
            .build();

    public static synchronized void loadSkin(@NotNull UUID playerId,
                                                           @NotNull String nick,
                                                           @Nullable String fallback) {
        PlayerInfo playerInfo = UMinecraft.getNetHandler().getPlayerInfo(playerId);
        if (playerInfo != null) return;

        ResourceLocation skinLocation = skins.getIfPresent(nick);
        if (skinLocation != null) return;

        if (fallback != null) {
            ResourceLocation fallbackIdentifier = new ResourceLocation(
                    "plasmovoice",
                    "skins/" + Hashing.sha1().hashUnencodedChars(nick.toLowerCase())
            );

            registerBase64Texture(fallback, fallbackIdentifier);
            skins.put(nick, fallbackIdentifier);
        }

        GameProfile profile = new GameProfile(playerId, nick);

        SkinManager skinManager = UMinecraft.getMinecraft().getSkinManager();

        //#if MC>=12002
        //$$ skinLocation = skinManager.getInsecureSkin(profile).texture();
        //$$ skins.put(profile.getName(), skinLocation);
        //#else
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = skinManager
                .getInsecureSkinInformation(profile);
        if (textures.isEmpty()) {
            skinManager.registerSkins(
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
        //#endif
    }

    public static synchronized void loadSkin(@NotNull McGameProfile gameProfile) {
        PlayerInfo playerInfo = UMinecraft.getNetHandler().getPlayerInfo(gameProfile.getId());
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

        skinLocation = getInsecureSkinLocation(profile);
        skins.put(gameProfile.getName(), skinLocation);
    }

    public static ResourceLocation getInsecureSkinLocation(GameProfile gameProfile) {
        //#if MC>=12002
        //$$ return UMinecraft.getMinecraft().getSkinManager().getInsecureSkin(gameProfile).texture();
        //#else
        MinecraftProfileTexture minecraftProfileTexture = UMinecraft.getMinecraft().getSkinManager().getInsecureSkinInformation(gameProfile).get(MinecraftProfileTexture.Type.SKIN);
        return minecraftProfileTexture != null ? UMinecraft.getMinecraft().getSkinManager().registerTexture(minecraftProfileTexture, MinecraftProfileTexture.Type.SKIN) : getDefaultSkin(gameProfile.getId());
        //#endif
    }

    public static synchronized @NotNull ResourceLocation getSkin(@NotNull UUID playerId, @NotNull String nick) {
        PlayerInfo playerInfo = UMinecraft.getNetHandler().getPlayerInfo(playerId);
        if (playerInfo != null) {
            //#if MC>=12002
            //$$ return playerInfo.getSkin().texture();
            //#else
            return playerInfo.getSkinLocation();
            //#endif
        }

        ResourceLocation skinLocation = skins.getIfPresent(nick);
        if (skinLocation != null) return skinLocation;

        return getDefaultSkin(playerId);
    }

    public static @NotNull ResourceLocation getDefaultSkin(@NotNull UUID playerId) {
        //#if MC>=12002
        //$$ return DefaultPlayerSkin.get(playerId).texture();
        //#else
        return DefaultPlayerSkin.getDefaultSkin(playerId);
        //#endif
    }

    private ModPlayerSkins() {
    }
}
