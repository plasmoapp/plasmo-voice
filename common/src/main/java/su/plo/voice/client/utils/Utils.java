package su.plo.voice.client.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;


public class Utils {
    public static Player getPlayerBySight(Level world, Player player) {
        Vec3 playerPos = player.getEyePosition(1.0F);
        Vec3 rotVector = player.getLookAngle();

        for (int i = 0; i < 16; i++) {
            playerPos = playerPos.add(rotVector);
            BlockState state = world.getBlockState(new BlockPos(playerPos));
            if (!state.isAir() && state.getMaterial().isSolid()) {
                break;
            }

            AABB box = AAABofSize(playerPos.subtract(0, 1, 0), 1, 2, 1);
            for (Player playerEntity : world.players()) {
                if (box.contains(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ()) &&
                        !playerEntity.isInvisibleTo(player)) {
                    return playerEntity;
                }
            }
        }

        return null;
    }

    public static AABB AAABofSize(Vec3 vec3, double d, double e, double f) {
        return new AABB(vec3.x - d / 2.0D, vec3.y - e / 2.0D, vec3.z - f / 2.0D, vec3.x + d / 2.0D, vec3.y + e / 2.0D, vec3.z + f / 2.0D);
    }
}
