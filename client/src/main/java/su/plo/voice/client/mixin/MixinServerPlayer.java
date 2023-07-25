package su.plo.voice.client.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.lib.mod.server.ModServerLib;
import su.plo.lib.mod.server.entity.ModServerPlayer;

//#if MC<11701
//$$ import java.lang.reflect.Field;
//#endif

//#if MC>=11900
//#if MC<11903
//$$ import net.minecraft.world.entity.player.ProfilePublicKey;
//$$
//$$ import org.jetbrains.annotations.Nullable;
//#endif
//#endif

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player {

    //#if MC>=11903
    public MixinServerPlayer(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }
    //#elseif MC>=11900
    //$$ public MixinServerPlayer(Level level, BlockPos blockPos, float f, GameProfile gameProfile, @Nullable ProfilePublicKey profilePublicKey) {
    //$$     super(level, blockPos, f, gameProfile, profilePublicKey);
    //$$ }
    //#else
    //$$ public MixinServerPlayer(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
    //$$     super(level, blockPos, f, gameProfile);
    //$$ }
    //#endif

    @Inject(method = "updateOptions", at = @At("HEAD"))
    public void updateOptions(ServerboundClientInformationPacket serverboundClientInformationPacket, CallbackInfo ci) {
        ModServerLib.INSTANCE.getPlayerById(getUUID()).ifPresent((player) -> {
            
            String language = "en_us";

            //#if MC>=11701
            language = serverboundClientInformationPacket.language();
            //#else
            //$$ Class<?> packetClass = serverboundClientInformationPacket.getClass();
            //$$ for (Field field : packetClass.getFields()) {
            //$$     if (field.getType() == String.class) {
            //$$         try {
            //$$             language = (String) field.get(serverboundClientInformationPacket);
            //$$         } catch (IllegalAccessException e) {
            //$$             throw new RuntimeException(e);
            //$$         }
            //$$     }
            //$$ }
            //#endif
            
            ((ModServerPlayer) player).setLanguage(language);
        });
    }
}
