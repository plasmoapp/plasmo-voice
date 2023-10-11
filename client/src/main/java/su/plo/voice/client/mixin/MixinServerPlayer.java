package su.plo.voice.client.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.lib.mod.server.ModServerLib;
import su.plo.lib.mod.server.entity.ModServerPlayer;

//#if MC<11903
//$$ import net.minecraft.world.entity.player.ProfilePublicKey;
//$$
//$$ import org.jetbrains.annotations.Nullable;
//#endif

//#if MC>=12002
//$$ import net.minecraft.server.level.ClientInformation;
//#else
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
//#endif

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player {

    //#if MC>=11903
    public MixinServerPlayer(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }
    //#else
    //$$ public MixinServerPlayer(Level level, BlockPos blockPos, float f, GameProfile gameProfile, @Nullable ProfilePublicKey profilePublicKey) {
    //$$     super(level, blockPos, f, gameProfile, profilePublicKey);
    //$$ }
    //#endif

    //#if MC>=12002
    //$$ @Inject(method = "updateOptions", at = @At("HEAD"))
    //$$ public void updateOptions(ClientInformation clientInformation, CallbackInfo ci) {
    //$$     ModServerLib.INSTANCE.getPlayerById(getUUID()).ifPresent((player) -> {
    //$$         ((ModServerPlayer) player).setLanguage(
    //$$                 clientInformation.language()
    //$$         );
    //$$     });
    //$$ }
    //#else
    @Inject(method = "updateOptions", at = @At("HEAD"))
    public void updateOptions(ServerboundClientInformationPacket serverboundClientInformationPacket, CallbackInfo ci) {
        ModServerLib.INSTANCE.getPlayerById(getUUID()).ifPresent((player) -> {
            ((ModServerPlayer) player).setLanguage(
                    serverboundClientInformationPacket.language()
            );
        });
    }
    //#endif
}
