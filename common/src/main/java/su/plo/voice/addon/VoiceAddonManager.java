package su.plo.voice.addon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.addon.AddonManager;
import su.plo.voice.api.addon.annotation.processor.JsonAddon;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VoiceAddonManager implements AddonManager {

    private static final Logger LOGGER = LogManager.getLogger(VoiceAddonManager.class);

    private final Map<Object, AddonContainer> addonByInstance = Maps.newHashMap();
    private final Map<String, AddonContainer> addons = Maps.newHashMap();

    public VoiceAddonManager(List<File> addonFolders)  {
        addonFolders.forEach(this::scanForAddons);
    }

    @Override
    public boolean isLoaded(@NotNull String id) {
        return addons.containsKey(id);
    }

    @Override
    public Optional<AddonContainer> getAddon(String id) {
        return Optional.ofNullable(addons.get(id));
    }

    @Override
    public Optional<AddonContainer> getAddon(Object instance) {
        return Optional.ofNullable(addonByInstance.get(instance));
    }

    private void scanForAddons(File folder) {
        checkNotNull(folder, "folder");
        if (!folder.isDirectory()) return;

        List<VoiceAddon> addons = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                folder.toPath(),
                p -> p.toFile().isFile() && p.toString().endsWith(".jar")
        )) {
            for (Path path : stream) {
                try {
                    for (JsonAddon jsonAddon : getJsonAddons(path)) {
                        addons.add(createAddon(jsonAddon, path));
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to load addons ({}) {}", folder, e.getMessage());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to scan folder ({}) for addons: {}", folder, e.getMessage());
        }

        if (addons.isEmpty()) return;

        for (VoiceAddon addon : addons) {
            // todo: addon dependencies
//            for (AddonDependency dependency : addon.getDependencies()) {
//                if (dependency.isOptional()) continue;
//                if (!this.addons.containsKey(dependency.getId())) {
//                    LOGGER.error("Addon {} is missing dependency {}", addon.getId(), dependency.getId());
//                    continue load;
//                }
//            }

            try {
                loadAddon(addon);
            } catch (Exception e) {
                LOGGER.error("Failed to load the addon {}: {}", addon.getId(), e.getMessage());
                continue;
            }
            this.addons.put(addon.getId(), addon);
            this.addonByInstance.put(addon.getInstance().get(), addon);
        }
    }

    private VoiceAddon createAddon(JsonAddon jsonAddon, Path addonPath) throws Exception {
        URL pluginJarUrl = addonPath.toUri().toURL();
        URLClassLoader loader = AccessController.doPrivileged(
                (PrivilegedAction<URLClassLoader>) () -> new URLClassLoader(new URL[]{pluginJarUrl})
        );

        Class<?> mainClass = loader.loadClass(jsonAddon.getMainClass());

        return new VoiceAddon(
                jsonAddon.getId(),
                addonPath,
                mainClass
        );
    }

    private List<JsonAddon> getJsonAddons(Path path) throws Exception {
        try (JarInputStream in = new JarInputStream(
                new BufferedInputStream(Files.newInputStream(path))
        )) {
            JarEntry entry;
            while ((entry = in.getNextJarEntry()) != null) {
                if (entry.getName().equals("plasmovoice-addons.json")) {
                    try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                        return JsonAddon.from(reader);
                    }
                }
            }
        }

        return ImmutableList.of();
    }

    private void loadAddon(@NotNull AddonContainer addon) throws Exception {
        Object instance = addon.getMainClass().getDeclaredConstructor().newInstance();
        ((VoiceAddon) addon).setInstance(instance);
    }
}
