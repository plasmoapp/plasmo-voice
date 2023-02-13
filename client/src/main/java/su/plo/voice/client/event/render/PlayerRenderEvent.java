package su.plo.voice.client.event.render;

import gg.essential.universal.UMatrixStack;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import su.plo.voice.api.event.Event;
import su.plo.voice.client.render.ModCamera;

public final class PlayerRenderEvent implements Event {

    @Getter
    private final UMatrixStack stack;
    @Getter
    private final ModCamera camera;
    @Getter
    private final Player player;
    @Getter
    private final int light;
    private final boolean label;
    @Getter
    private final boolean fakePlayer;

    public PlayerRenderEvent(@NonNull UMatrixStack stack,
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
