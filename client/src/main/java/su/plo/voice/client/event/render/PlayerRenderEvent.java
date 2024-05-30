package su.plo.voice.client.event.render;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.world.entity.player.Player;
import su.plo.voice.api.event.Event;
import su.plo.voice.client.render.ModCamera;

public final class PlayerRenderEvent implements Event {

    @Getter
    private final PoseStack stack;
    @Getter
    private final ModCamera camera;
    @Getter
    private final Player player;
    @Getter
    private final int light;
    private final boolean label;
    @Getter
    private final boolean fakePlayer;

    public PlayerRenderEvent(@NonNull PoseStack stack,
                             @NonNull ModCamera camera,
                             @NonNull Player player,
                             int light,
                             boolean label,
                             boolean fakePlayer) {
        this.stack = stack;
        this.camera = camera;
        this.light = light;
        this.player = player;
        this.label = label;
        this.fakePlayer = fakePlayer;
    }

    public boolean hasLabel() {
        return label;
    }
}
