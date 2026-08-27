package su.plo.lib.mod.client.render.texture;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import su.plo.slib.api.entity.player.McGameProfile;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.client.resources.PlayerSkin;

import java.util.Optional;

//#if MC>=12109
//$$ import com.mojang.authlib.properties.PropertyMap;
//$$ import com.google.common.collect.Multimap;
//$$ import com.google.common.collect.LinkedHashMultimap;
//#endif

public final class ModPlayerSkins {

    private static final Cache<UUID, Supplier<ResourceLocation>> SKINS = CacheBuilder
            .newBuilder()
            .expireAfterAccess(15L, TimeUnit.SECONDS)
            .build();

    public static synchronized void loadSkin(
            @NotNull UUID playerId,
            @NotNull String nick,
            @NotNull ScheduledExecutorService backgroundExecutor
    ) {
        PlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(playerId);
        if (playerInfo != null) return;

        Supplier<ResourceLocation> skinLocation = SKINS.getIfPresent(playerId);
        if (skinLocation != null) return;

        backgroundExecutor.execute(() -> {
            GameProfile profile = Optional.ofNullable(
                            Minecraft.getInstance()
            //#if MC>=12109
            //$$                         .services()
            //$$                         .sessionService()
            //#else
                                    .getMinecraftSessionService()
            //#endif
                                    .fetchProfile(playerId, false)
                    )
                    .map(ProfileResult::profile)
                    .orElse(new GameProfile(playerId, nick));

            SKINS.put(profile.getId(), getInsecureSkinLocation(profile));
        });
    }

    public static synchronized void loadSkin(@NotNull McGameProfile gameProfile) {
        PlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(gameProfile.getId());
        if (playerInfo != null) return;

        Supplier<ResourceLocation> skinLocation = SKINS.getIfPresent(gameProfile.getId());
        if (skinLocation != null) return;

        //#if MC>=12109
        //$$ Multimap<String, Property> propertyMap = LinkedHashMultimap.create();
        //$$
        //$$ gameProfile.getProperties().forEach((property) -> {
        //$$     propertyMap.put(property.getName(), new Property(
        //$$             property.getName(),
        //$$             property.getValue(),
        //$$             property.getSignature()
        //$$     ));
        //$$ });
        //$$
        //$$ GameProfile profile = new GameProfile(
        //$$         gameProfile.getId(),
        //$$         gameProfile.getName(),
        //$$         new PropertyMap(propertyMap)
        //$$ );
        //#else
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
        //#endif

        skinLocation = getInsecureSkinLocation(profile);
        SKINS.put(gameProfile.getId(), skinLocation);
    }

    private static Supplier<ResourceLocation> getInsecureSkinLocation(GameProfile gameProfile) {
        //#if MC>=12109
        //$$ Supplier<PlayerSkin> skinSupplier = Minecraft.getInstance().getSkinManager().createLookup(gameProfile, false);
        //#else
        Supplier<PlayerSkin> skinSupplier = Minecraft.getInstance().getSkinManager().lookupInsecure(gameProfile);
        //#endif
        return () -> {
        //#if MC>=12109
        //$$     Identifier skinLocation = skinSupplier.get().body().texturePath();
        //#else
            ResourceLocation skinLocation = skinSupplier.get().texture();
        //#endif
        //#if MC<12111
            Minecraft.getInstance()
                    .getTextureManager()
                    .getTexture(skinLocation)
                    .setFilter(false, true);
        //#endif
            return skinLocation;
        };
    }

    public static synchronized @NotNull ResourceLocation getSkin(@NotNull UUID playerId, @NotNull String nick) {
        PlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(playerId);
        if (playerInfo != null) {
            //#if MC>=12109
            //$$ return playerInfo.getSkin().body().texturePath();
            //#else
            return playerInfo.getSkin().texture();
            //#endif
        }

        Supplier<ResourceLocation> skinLocation = SKINS.getIfPresent(playerId);
        if (skinLocation != null) return skinLocation.get();

        return getDefaultSkin(playerId);
    }

    public static @NotNull ResourceLocation getDefaultSkin(@NotNull UUID playerId) {
        //#if MC>=12109
        //$$ return DefaultPlayerSkin.get(playerId).body().texturePath();
        //#else
        return DefaultPlayerSkin.get(playerId).texture();
        //#endif
    }

    private ModPlayerSkins() {
    }
}
