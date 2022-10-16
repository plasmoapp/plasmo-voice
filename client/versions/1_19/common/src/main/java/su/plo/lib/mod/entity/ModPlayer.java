package su.plo.lib.mod.entity;

import lombok.ToString;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
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
    public boolean hasLabelScoreboard() {
        Scoreboard scoreboard = instance.getScoreboard();
        Objective scoreboardObjective = scoreboard.getDisplayObjective(2);
        return scoreboardObjective != null;
    }
}
