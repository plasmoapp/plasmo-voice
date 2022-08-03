package su.plo.voice.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.socket.SocketServerUDP;

public class PlaceholderPlasmoVoice extends PlaceholderExpansion {
    private static final String identifier = "plasmovoice";
    private static final String author = "Apehum";
    private static final String version = "1.0.0";

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    @Override
    public @NotNull String getAuthor() {
        return author;
    }

    @Override
    public @NotNull String getVersion() {
        return version;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.equalsIgnoreCase("installed") && player != null) {
            return SocketServerUDP.clients.containsKey(player) ? "true" : "false";
        } else if (params.equalsIgnoreCase("players")) {
            return String.valueOf(SocketServerUDP.clients.size());
        }

        return null;
    }
}
