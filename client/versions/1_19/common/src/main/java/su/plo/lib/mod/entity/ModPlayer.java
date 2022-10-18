package su.plo.lib.mod.entity;

import lombok.ToString;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.entity.MinecraftPlayer;

@ToString
public class ModPlayer<P extends Player> extends ModEntity<P> implements MinecraftPlayer {

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
        return instance.getScoreboard().getDisplayObjective(2) != null;
    }
}
