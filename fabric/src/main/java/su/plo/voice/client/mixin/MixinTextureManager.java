package su.plo.voice.client.mixin;//package su.plo.voice.client.mixin;
//
//import com.google.common.hash.Hashing;
//import com.mojang.authlib.minecraft.MinecraftProfileTexture;
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.texture.*;
//import net.minecraft.client.util.DefaultSkinHelper;
//import net.minecraft.util.Identifier;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.ModifyVariable;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//import su.plo.voice.utils.IPlayerSkinProvider;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//
//@Mixin(TextureManager.class)
//public abstract class MixinTextureManager {
//    @Shadow protected abstract AbstractTexture loadTexture(Identifier id, AbstractTexture texture);
//
//    @Shadow @Final private Map<Identifier, AbstractTexture> textures;
//
//    @Shadow @Final private Set<TextureTickListener> tickListeners;
//
//    @Shadow protected abstract void closeTexture(Identifier id, AbstractTexture texture);
//
//    // todo optifine support for capes
//    @Inject(method = "registerTexture", at = @At("HEAD"), cancellable = true)
//    public void registerTexture(Identifier id, AbstractTexture texture, CallbackInfo ci) {
//        if (id.getPath().startsWith("capeof")) {
//            final HttpURLConnection connection;
//            try {
//                String[] split = id.getPath().split("/");
//                URL url = new URL("https://vc.plo.su/capes/" + split[split.length - 1] + ".png");
//                connection = (HttpURLConnection) url.openConnection();
//                connection.setConnectTimeout(15000);
//                connection.setReadTimeout(15000);
//                connection.setUseCaches(false);
//
//                if (connection.getResponseCode() == 200) {
//                    String string = Hashing.sha1().hashUnencodedChars(split[split.length - 1]).toString();
//                    File file = new File(((IPlayerSkinProvider) MinecraftClient.getInstance().getSkinProvider()).getSkinCacheDir(),
//                            string.length() > 2 ? string.substring(0, 2) : "xx");
//                    File file2 = new File(file, string);
//                    if (file2.exists()) {
//                        file2.delete();
//                    }
//
//                    MinecraftProfileTexture profileTexture = new MinecraftProfileTexture(url.toString(), new HashMap<>());
//                    texture = this.loadTexture(id, new PlayerSkinTexture(file2,
//                            profileTexture.getUrl(), DefaultSkinHelper.getTexture(), false, null));
//                    AbstractTexture abstractTexture = this.textures.put(id, texture);
//                    if (abstractTexture != texture) {
//                        if (abstractTexture != null && abstractTexture != MissingSprite.getMissingSpriteTexture()) {
//                            this.tickListeners.remove(abstractTexture);
//                            this.closeTexture(id, abstractTexture);
//                        }
//
//                        if (texture instanceof TextureTickListener) {
//                            this.tickListeners.add((TextureTickListener)texture);
//                        }
//                    }
//
//                    ci.cancel();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}
