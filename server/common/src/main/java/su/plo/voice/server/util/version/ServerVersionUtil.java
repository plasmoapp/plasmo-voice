package su.plo.voice.server.util.version;

import lombok.NonNull;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.chat.style.McTextClickEvent;
import su.plo.slib.api.chat.style.McTextHoverEvent;
import su.plo.slib.api.chat.style.McTextStyle;
import su.plo.voice.api.server.player.VoiceServerPlayer;

import java.util.regex.Pattern;

public final class ServerVersionUtil {

    private static final String MODRINTH_LINK = "https://modrinth.com/plugin/plasmo-voice";

    private static final Pattern MINECRAFT_VERSION_PATTERN = Pattern.compile("[a-zA-Z0-9._-]{1,32}");

    public static void suggestSupportedVersion(@NonNull VoiceServerPlayer player,
                                               @NonNull String minecraftVersion) {
        String downloadLink = getVersionsLink(minecraftVersion);

        player.getInstance().sendMessage(McTextComponent.translatable(
                "pv.error.version_not_supported",
                McTextComponent.translatable("pv.error.version_not_supported_click")
                        .withStyle(McTextStyle.YELLOW)
                        .clickEvent(McTextClickEvent.openUrl(downloadLink))
                        .hoverEvent(McTextHoverEvent.showText(McTextComponent.translatable(
                                "pv.error.version_not_supported_hover", downloadLink
                        )))
        ));
    }

    private static String getVersionsLink(@NonNull String minecraftVersion) {
        if (!MINECRAFT_VERSION_PATTERN.matcher(minecraftVersion).matches()) return MODRINTH_LINK;

        return MODRINTH_LINK + "/versions?g=" + minecraftVersion;
    }

    private ServerVersionUtil() {
    }
}
