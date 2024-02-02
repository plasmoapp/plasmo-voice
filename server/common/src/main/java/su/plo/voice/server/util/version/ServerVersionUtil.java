package su.plo.voice.server.util.version;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.chat.style.McTextClickEvent;
import su.plo.slib.api.chat.style.McTextHoverEvent;
import su.plo.slib.api.chat.style.McTextStyle;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.util.version.ModrinthLoader;
import su.plo.voice.util.version.ModrinthVersion;
import su.plo.voice.util.version.SemanticVersion;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public final class ServerVersionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerVersionUtil.class);

    private static final Cache<String, String> LINKS_CACHE = CacheBuilder
            .newBuilder()
            .expireAfterAccess(15L, TimeUnit.SECONDS)
            .build();

    public static ModrinthLoader getPlayerModrinthLoader(@NonNull VoiceServerPlayer player) {
        boolean isForge = player.getInstance().getRegisteredChannels()
                .stream()
                .anyMatch(channel -> channel.equals("fml:handshake") || channel.equalsIgnoreCase("forge:handshake"));

        return isForge ? ModrinthLoader.FORGE : ModrinthLoader.FABRIC;
    }

    public static void suggestSupportedVersion(@NonNull VoiceServerPlayer player,
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

            player.getInstance().sendMessage(McTextComponent.translatable(
                    "pv.error.version_not_supported",
                    McTextComponent.translatable("pv.error.version_not_supported_click")
                            .withStyle(McTextStyle.YELLOW)
                            .clickEvent(McTextClickEvent.openUrl(downloadLink))
                            .hoverEvent(McTextHoverEvent.showText(McTextComponent.translatable(
                                    "pv.error.version_not_supported_hover", downloadLink
                            )))
            ));
        } catch (ExecutionException e) {
            LOGGER.error("Failed to get version from modrinth", e);
        }
    }

    private static String getVersionCacheKey(String minecraftVersion, ModrinthLoader loader, SemanticVersion serverVersion) {
        return minecraftVersion + loader.name() + serverVersion.string();
    }

    private ServerVersionUtil() {
    }
}
