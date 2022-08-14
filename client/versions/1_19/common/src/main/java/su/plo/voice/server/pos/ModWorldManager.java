package su.plo.voice.server.pos;

import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.pos.VoiceWorld;
import su.plo.voice.api.server.pos.WorldManager;

import java.util.Map;

public final class ModWorldManager implements WorldManager {

    private final Map<ServerLevel, VoiceWorld> worldByServerObject = Maps.newConcurrentMap();

    @Override
    public @NotNull VoiceWorld wrap(@NotNull Object serverWorld) {
        if (!(serverWorld instanceof ServerLevel serverLevel))
            throw new IllegalArgumentException("serverWorld is not " + ServerLevel.class);

        return worldByServerObject.computeIfAbsent(serverLevel, ModVoiceWorld::new);
    }
}
