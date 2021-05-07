package su.plo.voice.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.PlasmoVoice;

import java.text.SimpleDateFormat;
import java.util.Date;

public class VoiceMuteList implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (PlasmoVoice.muted.size() == 0) {
            sender.sendMessage(PlasmoVoice.getInstance().getMessagePrefix("muted_list_empty"));
            return true;
        }

        sender.sendMessage(PlasmoVoice.getInstance().getMessagePrefix("muted_list"));
        PlasmoVoice.muted.forEach((uuid, muted) -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            String expires = muted.to > 0
                    ? new SimpleDateFormat(PlasmoVoice.getInstance().getMessage("mute_expires_format")).format(new Date(muted.to))
                    : PlasmoVoice.getInstance().getMessage("mute_expires_never");
            String reason = muted.reason == null
                    ? PlasmoVoice.getInstance().getMessage("mute_no_reason")
                    : muted.reason;
            sender.sendMessage(PlasmoVoice.getInstance().getMessage("muted_list_entry")
                    .replace("{player}", player.getName())
                    .replace("{expires}", expires)
                    .replace("{reason}", reason));
        });

        return true;
    }
}
