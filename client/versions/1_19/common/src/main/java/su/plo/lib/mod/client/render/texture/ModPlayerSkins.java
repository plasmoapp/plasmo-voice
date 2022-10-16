package su.plo.lib.mod.client.render.texture;

import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.client.render.texture.MinecraftPlayerSkins;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class ModPlayerSkins implements MinecraftPlayerSkins {

    private final Minecraft minecraft = Minecraft.getInstance();

    private final ResourceLocation steveSkin = new ResourceLocation(getSteveSkin());
    private final ResourceLocation alexSkin = new ResourceLocation(getAlexSkin());
    private final Map<String, ResourceLocation> skins = new HashMap<>();


    @Override
    public synchronized CompletableFuture<String> loadSkin(@NotNull UUID playerId,
                                                           @NotNull String nick,
                                                           @Nullable String fallback) {
        if (skins.containsKey(nick)) {
            return CompletableFuture.completedFuture(skins.get(nick).toString());
        }

        CompletableFuture<String> future = new CompletableFuture<>();

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

        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = minecraft.getSkinManager().getInsecureSkinInformation(profile);
        if (textures == null || textures.isEmpty()) {
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

        future.complete(getSkin(playerId, nick));

        return future;
    }

    @Override
    public synchronized @NotNull String getSkin(@NotNull UUID playerId, @NotNull String nick) {
        return skins.getOrDefault(nick, shouldUseSlimModel(playerId) ? alexSkin : steveSkin).toString();
    }

    private static boolean shouldUseSlimModel(@NotNull UUID playerId) {
        return (playerId.hashCode() & 1) == 1;
    }
}
