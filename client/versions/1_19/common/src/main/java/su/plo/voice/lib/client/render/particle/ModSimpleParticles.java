package su.plo.voice.lib.client.render.particle;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.render.particle.MinecraftBlockParticle;
import su.plo.lib.client.render.particle.MinecraftParticles;

import java.lang.reflect.Field;

public final class ModSimpleParticles implements MinecraftParticles {

    @Override
    public @NotNull MinecraftBlockParticle createBlockParticle(int x, int y, @NotNull String blockName) {
        Block block;
        Field field;
        try {
            field = Blocks.class.getField(blockName);
            block = (Block) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalArgumentException("Block not found");
        }

        return new ModBlockParticle(
                new BlockDustParticle2D(x, y, 0D, 0D, block.defaultBlockState())
        );
    }
}
