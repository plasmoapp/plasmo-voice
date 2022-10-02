package su.plo.voice.client.entity;

import lombok.Getter;
import lombok.ToString;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.entity.MinecraftPlayer;

@ToString
public class ModPlayer extends ModEntity implements MinecraftPlayer {

    @Getter
    private final Player instance;

    public ModPlayer(@NotNull Player player) {
        super(player);

        this.instance = player;
    }

    @Override
    public boolean hasLabelScoreboard() {
        Scoreboard scoreboard = instance.getScoreboard();
        Objective scoreboardObjective = scoreboard.getDisplayObjective(2);
        return scoreboardObjective != null;
    }
}
