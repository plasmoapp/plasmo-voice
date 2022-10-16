package su.plo.lib.mod.client.render.particle;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.render.particle.MinecraftBlockParticle;
import su.plo.lib.api.client.render.particle.MinecraftParticles;

import java.lang.reflect.Field;
import java.util.Objects;

public final class ModSimpleParticles implements MinecraftParticles {

    @Override
    public @NotNull MinecraftBlockParticle createBlockParticle(int x, int y, @NotNull String blockId) {
        Block block = null;
        for (Field field : Blocks.class.getFields()) {
            try {
                Object object = field.get(null);
                if (object instanceof Block fieldBlock) {
                    String[] nameSplit = fieldBlock.getDescriptionId().split("\\.");
                    if (Objects.equals(nameSplit[nameSplit.length - 1], blockId)) {
                        block = fieldBlock;
                        break;
                    }
                }
            } catch (IllegalAccessException ignored) {
            }
        }

        if (block == null) {
            throw new IllegalArgumentException("Block " + blockId + " not found");
        }

        return new ModBlockParticle(
                new BlockDustParticle2D(x, y, 0D, 0D, block.defaultBlockState())
        );
    }
}
