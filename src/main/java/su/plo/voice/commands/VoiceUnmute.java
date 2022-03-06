package su.plo.voice.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.PlasmoVoice;

import java.util.List;

public class VoiceUnmute implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(PlasmoVoice.getInstance().getMessagePrefix("help.unmute"));
            return true;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        if (player.getFirstPlayed() == 0 || player.getName() == null) {
            sender.sendMessage(PlasmoVoice.getInstance().getMessagePrefix("player_not_found"));
            return true;
        }

        if (!PlasmoVoice.getInstance().unmute(player.getUniqueId(), false)) {
            sender.sendMessage(PlasmoVoice.getInstance().getMessagePrefix("not_muted")
                    .replace("{player}", player.getName()));
            return true;
        }

        sender.sendMessage(PlasmoVoice.getInstance().getMessagePrefix("unmuted")
                .replace("{player}", player.getName()));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }
}
