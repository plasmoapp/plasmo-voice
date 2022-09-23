package su.plo.voice.client.entity;

import lombok.Getter;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.entity.MinecraftPlayer;

public class ModPlayer extends ModEntity implements MinecraftPlayer {

    @Getter
    private final AbstractClientPlayer instance;

    public ModPlayer(@NotNull AbstractClientPlayer player) {
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
