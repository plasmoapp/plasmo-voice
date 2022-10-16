package su.plo.voice.mod.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.lib.mod.server.ModServerLib;
import su.plo.lib.mod.server.entity.ModServerPlayer;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player {

    public MixinServerPlayer(Level level, BlockPos blockPos, float f, GameProfile gameProfile, @Nullable ProfilePublicKey profilePublicKey) {
        super(level, blockPos, f, gameProfile, profilePublicKey);
    }

    @Inject(method = "updateOptions", at = @At("HEAD"))
    public void updateOptions(ServerboundClientInformationPacket serverboundClientInformationPacket, CallbackInfo ci) {
        ModServerLib.INSTANCE.getPlayerById(getUUID()).ifPresent((player) -> {
            ((ModServerPlayer) player).setLanguage(
                    serverboundClientInformationPacket.language()
            );
        });
    }
}
