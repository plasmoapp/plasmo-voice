package su.plo.voice.server.util.version;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import su.plo.lib.api.chat.MinecraftTextClickEvent;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.chat.MinecraftTextHoverEvent;
import su.plo.lib.api.chat.MinecraftTextStyle;
import su.plo.voice.api.server.player.PlayerModLoader;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.util.version.ModrinthLoader;
import su.plo.voice.util.version.ModrinthVersion;
import su.plo.voice.util.version.SemanticVersion;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public final class ServerVersionUtil {

    private static final Cache<String, String> LINKS_CACHE = CacheBuilder
            .newBuilder()
            .expireAfterAccess(15L, TimeUnit.SECONDS)
            .build();

    public static ModrinthLoader getPlayerModrinthLoader(@NonNull VoicePlayer player) {
        return player.getModLoader()
                .filter(loader -> loader.equals(PlayerModLoader.FORGE))
                .map(loader -> ModrinthLoader.FORGE)
                .orElse(ModrinthLoader.FABRIC);
    }

    public static void suggestSupportedVersion(@NonNull VoicePlayer player,
                                               @NonNull SemanticVersion serverVersion,
                                               @NonNull String minecraftVersion) {
        try {
            String downloadLink = LINKS_CACHE.get(
                    getVersionCacheKey(minecraftVersion, getPlayerModrinthLoader(player), serverVersion),
                    () -> {
                        if (!serverVersion.isRelease()) {
                            return ModrinthVersion.from(serverVersion.string(), minecraftVersion, getPlayerModrinthLoader(player))
                                    .map(ModrinthVersion::downloadLink)
                                    .orElse("https://modrinth.com/plugin/plasmo-voice");
                        } else {
                            return ModrinthVersion.getLatest(minecraftVersion, getPlayerModrinthLoader(player), false, serverVersion)
                                    .map(ModrinthVersion::downloadLink)
                                    .orElse("https://modrinth.com/plugin/plasmo-voice");
                        }
                    }
            );

            player.getInstance().sendMessage(MinecraftTextComponent.translatable(
                    "pv.error.version_not_supported",
                    MinecraftTextComponent.translatable("pv.error.version_not_supported_click")
                            .withStyle(MinecraftTextStyle.YELLOW)
                            .clickEvent(MinecraftTextClickEvent.openUrl(downloadLink))
                            .hoverEvent(MinecraftTextHoverEvent.showText(MinecraftTextComponent.translatable(
                                    "pv.error.version_not_supported_hover", downloadLink
                            )))
            ));
        } catch (ExecutionException e) {
            LogManager.getLogger().error("Failed to get version from modrinth", e);
        }
    }

    private static String getVersionCacheKey(String minecraftVersion, ModrinthLoader loader, SemanticVersion serverVersion) {
        return minecraftVersion + loader.name() + serverVersion.string();
    }

    private ServerVersionUtil() {
    }
}
