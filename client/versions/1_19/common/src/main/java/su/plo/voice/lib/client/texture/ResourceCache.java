package su.plo.voice.lib.client.texture;

import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class ResourceCache {

    private final Map<String, ResourceLocation> resourceLocations = Maps.newConcurrentMap();

    public ResourceLocation getLocation(@NotNull String resourceLocation) {
        return resourceLocations.computeIfAbsent(
                resourceLocation,
                ResourceLocation::new
        );
    }
}
