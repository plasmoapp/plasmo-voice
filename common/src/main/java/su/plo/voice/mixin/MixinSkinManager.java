package su.plo.voice.mixin;

import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.utils.IPlayerSkinProvider;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

@Mixin(SkinManager.class)
public class MixinSkinManager implements IPlayerSkinProvider {
    @Shadow @Final private File skinsDirectory;

    @Shadow @Final private TextureManager textureManager;

    @Inject(method = "registerSkins", at = @At(value = "RETURN"))
    private void loadSkin(GameProfile profile, SkinManager.SkinTextureCallback callback, boolean requireSecure, CallbackInfo ci) {
        Runnable runnable = () -> {
            final HttpURLConnection connection;
            try {
                URL url = new URL("https://vc.plo.su/capes/" + profile.getName() + ".png");

                MinecraftProfileTexture texture = new MinecraftProfileTexture(url.toString(), new HashMap<>());
                String string = Hashing.sha1().hashUnencodedChars(texture.getHash()).toString();
                ResourceLocation identifier = new ResourceLocation("skins/" + string);
                File file = new File(this.skinsDirectory, string.length() > 2 ? string.substring(0, 2) : "xx");
                File file2 = new File(file, string);
                if (file2.exists()) {
                    if (System.currentTimeMillis() - file2.lastModified() < 86400L) {

                        RenderSystem.recordRenderCall(() -> {
                            HttpTexture playerSkinTexture = new HttpTexture(file2, texture.getUrl(), DefaultPlayerSkin.getDefaultSkin(), false, () -> {
                                if (callback != null) {
                                    callback.onSkinTextureAvailable(MinecraftProfileTexture.Type.CAPE, identifier, texture);
                                }
                            });
                            this.textureManager.register(identifier, playerSkinTexture);
                        });
                        return;
                    } else {
                        file2.delete();
                    }
                }

                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.setUseCaches(false);

                if (connection.getResponseCode() == 200) {
                    RenderSystem.recordRenderCall(() -> {
                        HttpTexture playerSkinTexture = new HttpTexture(file2, texture.getUrl(), DefaultPlayerSkin.getDefaultSkin(), false, () -> {
                            if (callback != null) {
                                callback.onSkinTextureAvailable(MinecraftProfileTexture.Type.CAPE, identifier, texture);
                            }
                        });
                        this.textureManager.register(identifier, playerSkinTexture);
                    });

                    // todo optifine
//                    MinecraftClient.getInstance().execute(() -> {
//                        RenderSystem.recordRenderCall(() -> {
//                            try {
//                                MinecraftProfileTexture texture = new MinecraftProfileTexture(url.toString(), new HashMap<>());
//                                String string = Hashing.sha1().hashUnencodedChars("of_" + texture.getHash()).toString();
//                                Identifier identifier = new Identifier("capeof/" + profile.getName());
//                                File file = new File(this.skinCacheDir, string.length() > 2 ? string.substring(0, 2) : "xx");
//                                File file2 = new File(file, string);
//                                if (file2.exists()) {
//                                    file2.delete();
//                                }
//                                PlayerSkinTexture playerSkinTexture = new PlayerSkinTexture(file2, texture.getUrl(), DefaultSkinHelper.getTexture(), false, () -> {
//                                    if (callback != null) {
//                                        callback.onSkinTextureAvailable(MinecraftProfileTexture.Type.CAPE, identifier, texture);
//                                    }
//
//                                });
//                                this.textureManager.registerTexture(identifier, playerSkinTexture);
//                            } catch (InvalidIdentifierException ignored) {
//
//                            }
//                        });
//                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        Util.backgroundExecutor().execute(runnable);
    }

    @Override
    public File getSkinCacheDir() {
        return this.skinsDirectory;
    }
}
