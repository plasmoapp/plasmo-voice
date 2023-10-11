package su.plo.lib.mod.entity;

import lombok.ToString;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.entity.MinecraftPlayerEntity;

//#if MC>=12002
//$$ import net.minecraft.world.scores.DisplaySlot;
//#endif

@ToString
public class ModPlayer<P extends Player> extends ModEntity<P> implements MinecraftPlayerEntity {

    public ModPlayer(@NotNull P player) {
        super(player);
    }

    @Override
    public @NotNull String getName() {
        return instance.getGameProfile().getName();
    }

    @Override
    public boolean isSpectator() {
        return instance.isSpectator();
    }

    @Override
    public boolean isSneaking() {
        return instance.isDescending();
    }

    @Override
    public boolean hasLabelScoreboard() {
        //#if MC>=12002
        //$$ return instance.getScoreboard().getDisplayObjective(DisplaySlot.BELOW_NAME) != null;
        //#else
        return instance.getScoreboard().getDisplayObjective(2) != null;
        //#endif
    }
}
