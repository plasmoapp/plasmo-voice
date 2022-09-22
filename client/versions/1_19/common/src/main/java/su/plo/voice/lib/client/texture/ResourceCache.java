package su.plo.voice.lib.client.texture;

import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class ResourceCache {

    private final Map<String, ResourceLocation> locationByString = Maps.newConcurrentMap();

    public ResourceLocation getLocation(@NotNull String resourceLocation) {
        return locationByString.computeIfAbsent(
                resourceLocation,
                ResourceLocation::new
        );
    }
}
