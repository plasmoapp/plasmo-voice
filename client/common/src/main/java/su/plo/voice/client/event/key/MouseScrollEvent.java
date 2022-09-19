package su.plo.voice.client.event.key;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.voice.api.event.EventCancellableBase;

/**
 * This event is fires once the key was pressed
 */
public final class MouseScrollEvent extends EventCancellableBase {

    @Getter
    private final MinecraftClientLib minecraft;
    @Getter
    private final double horizontal;
    @Getter
    private final double vertical;

    public MouseScrollEvent(@NotNull MinecraftClientLib minecraft,
                            double horizontal,
                            double vertical) {
        this.minecraft = Preconditions.checkNotNull(minecraft, "minecraft");
        this.horizontal = horizontal;
        this.vertical = vertical;
    }
}
