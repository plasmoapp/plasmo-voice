package su.plo.voice.client.audio;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class SoundOcclusion {

    private static final double OCCLUSION_MULTIPLIER = 0.5D;
    private static final double OCCLUSION_MAX = 0.98D;

    public static double getOccludedPercent(Level world, Vec3 sound, Vec3 listener) {
        double occludedPercent = 0D;
        sound = sound.add(0.01D, 0.01D, 0.01D);

        if (Double.isNaN(sound.x) || Double.isNaN(sound.y) || Double.isNaN(sound.z)) return occludedPercent;
        if (Double.isNaN(listener.x) || Double.isNaN(listener.y) || Double.isNaN(listener.z)) return occludedPercent;

        BlockPos listenerPos = new BlockPos(listener);
        BlockPos soundPos = new BlockPos(sound);
        int i = 0;

        while (i++ < 200) {
            Vec3 prevSound = sound;
            BlockPos prevSoundPos = soundPos;

            if (Double.isNaN(sound.x) || Double.isNaN(sound.y) || Double.isNaN(sound.z)) {
                return occludedPercent;
            }

            if (soundPos.equals(listenerPos)) {
                return occludedPercent;
            }

            boolean shouldChangeX = listenerPos.getX() != soundPos.getX();
            boolean shouldChangeY = listenerPos.getY() != soundPos.getY();
            boolean shouldChangeZ = listenerPos.getZ() != soundPos.getZ();

            int nextX = soundPos.getX() + (listenerPos.getX() > soundPos.getX() ? 1 : 0);
            int nextY = soundPos.getY() + (listenerPos.getY() > soundPos.getY() ? 1 : 0);
            int nextZ = soundPos.getZ() + (listenerPos.getZ() > soundPos.getZ() ? 1 : 0);

            double xDifference = listener.x - sound.x;
            double yDifference = listener.y - sound.y;
            double zDifference = listener.z - sound.z;

            double xPercentChange = shouldChangeX ? ((double) nextX - sound.x) / xDifference : Double.POSITIVE_INFINITY;
            double yPercentChange = shouldChangeY ? ((double) nextY - sound.y) / yDifference : Double.POSITIVE_INFINITY;
            double zPercentChange = shouldChangeZ ? ((double) nextZ - sound.z) / zDifference : Double.POSITIVE_INFINITY;

            BlockPos soundPosOffset = null;
            if (xPercentChange < yPercentChange && xPercentChange < zPercentChange) {
                sound = new Vec3(nextX, sound.y + yDifference * xPercentChange, sound.z + zDifference * xPercentChange);
                if (listenerPos.getX() < soundPos.getX()) {
                    soundPosOffset = new BlockPos(-1, 0, 0);
                }
            } else if (yPercentChange < zPercentChange) {
                sound = new Vec3(sound.x + xDifference * yPercentChange, nextY, sound.z + zDifference * yPercentChange);
                if (listenerPos.getY() < soundPos.getY()) {
                    soundPosOffset = new BlockPos(0, -1, 0);
                }
            } else {
                sound = new Vec3(sound.x + xDifference * zPercentChange, sound.y + yDifference * zPercentChange, nextZ);
                if (listenerPos.getZ() < soundPos.getZ()) {
                    soundPosOffset = new BlockPos(0, 0, -1);
                }
            }

            soundPos = new BlockPos(sound);
            if (soundPosOffset != null) {
                soundPos = soundPos.offset(soundPosOffset);
            }

            if (i <= 1) continue;

            BlockState state = world.getBlockState(prevSoundPos);
            Material material = state.getMaterial();
            VoxelShape collisionShape = state.getCollisionShape(world, prevSoundPos);

            if (state.isAir()
                    || state.getShape(world, prevSoundPos).isEmpty()
                    || collisionShape == Shapes.empty()) continue;

            BlockHitResult rayTrace = collisionShape.clip(prevSound, listener, prevSoundPos);
            if (rayTrace == null) continue;

            double newOcclusion = material.isSolid() ? OCCLUSION_MULTIPLIER : OCCLUSION_MULTIPLIER / 2.0D;

            if (occludedPercent > 0) {
                occludedPercent += newOcclusion / 4;
            } else {
                occludedPercent += newOcclusion;
            }

            if (occludedPercent > OCCLUSION_MAX) {
                return OCCLUSION_MAX;
            }
        }

        return occludedPercent;
    }

    private SoundOcclusion() {
    }
}
