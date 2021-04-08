package su.plo.voice.sound;

import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

public class Occlusion {
    private static final MinecraftClient MC = MinecraftClient.getInstance();

    public static double getOccludedPercent(World world, ClientPlayerEntity player, Vec3d source) {
        return getBaseOccludedPercent(world, source.add(0.0D, player.getStandingEyeHeight(), 0.0D),
                MC.player.getPos().add(0.0D, player.getStandingEyeHeight(), 0.0D) // wtf
        );
    }

    private static double getBaseOccludedPercent(World world, Vec3d sound, Vec3d listener) {
        double occludedPercent = 0.0D;
        sound = sound.add(0.01D, 0.01D, 0.01D);

        if (!Double.isNaN(sound.x) && !Double.isNaN(sound.y) && !Double.isNaN(sound.z)) {
            if (!Double.isNaN(listener.x) && !Double.isNaN(listener.y) && !Double.isNaN(listener.z)) {
                BlockPos listenerPos = new BlockPos(listener);
                BlockPos soundPos = new BlockPos(sound);
                int i = 0;

                while(i++ < 200) {
                    Vec3d prevSound = sound;
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
                    double xPercentChange = shouldChangeX ? ((double)nextX - sound.x) / xDifference : 1.0D / 0.0;
                    double yPercentChange = shouldChangeY ? ((double)nextY - sound.y) / yDifference : 1.0D / 0.0;
                    double zPercentChange = shouldChangeZ ? ((double)nextZ - sound.z) / zDifference : 1.0D / 0.0;
                    BlockPos soundPosOffset = null;
                    if (xPercentChange < yPercentChange && xPercentChange < zPercentChange) {
                        sound = new Vec3d(nextX, sound.y + yDifference * xPercentChange, sound.z + zDifference * xPercentChange);
                        if (listenerPos.getX() < soundPos.getX()) {
                            soundPosOffset = new BlockPos(-1, 0, 0);
                        }
                    } else if (yPercentChange < zPercentChange) {
                        sound = new Vec3d(sound.x + xDifference * yPercentChange, nextY, sound.z + zDifference * yPercentChange);
                        if (listenerPos.getY() < soundPos.getY()) {
                            soundPosOffset = new BlockPos(0, -1, 0);
                        }
                    } else {
                        sound = new Vec3d(sound.x + xDifference * zPercentChange, sound.y + yDifference * zPercentChange, nextZ);
                        if (listenerPos.getZ() < soundPos.getZ()) {
                            soundPosOffset = new BlockPos(0, 0, -1);
                        }
                    }

                    soundPos = new BlockPos(sound);
                    if (soundPosOffset != null) {
                        soundPos = soundPos.add(soundPosOffset);
                    }

                    if (i > 1) {
                        BlockState state =  world.getBlockState(prevSoundPos);
                        Material material = state.getMaterial();
                        VoxelShape collisionShape = state.getCollisionShape(world, prevSoundPos);
                        if (!state.isAir() && !state.getOutlineShape(world, prevSoundPos).isEmpty() && collisionShape != VoxelShapes.empty()) {
                            BlockHitResult rayTrace = collisionShape.raycast(prevSound, listener, prevSoundPos);
                            if (rayTrace != null) {
                                double occlusionMultiplier = 0.5D;
                                double occlusionMax = 0.98D;

//                                Double customOcclusion = null;
//                                if (rayTrace.getType() == HitResult.Type.BLOCK) {
//                                    // customOcclusion = SoundFiltersConfig.getCustomBlockOcclusion(MC.field_71441_e, new CachedBlockInfo(world, rayTrace.func_216350_a(), false));
//                                }

                                double newOcclusion = material.isSolid() ? occlusionMultiplier : occlusionMultiplier / 2.0D;
//                                if (customOcclusion != null) {
//                                    newOcclusion = customOcclusion * occlusionMultiplier;
//                                } else {
//                                    newOcclusion = material.isSolid() ? occlusionMultiplier : occlusionMultiplier / 2.0D;
//                                }

                                //newOcclusion *= sound.squaredDistanceTo(prevSound);
                                if(occludedPercent > 0) {
                                    occludedPercent += newOcclusion / 4;
                                } else {
                                    occludedPercent += newOcclusion;
                                }

                                if (occludedPercent > occlusionMax) {
                                    return occlusionMax;
                                }
                            }
                        }
                    }
                }

                return occludedPercent;
            } else {
                return occludedPercent;
            }
        } else {
            return occludedPercent;
        }
    }
}
