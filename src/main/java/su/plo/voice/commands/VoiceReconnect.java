package su.plo.voice.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.plo.voice.PlasmoVoice;
import su.plo.voice.listeners.PlayerListener;

public class VoiceReconnect implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        sender.sendMessage(PlasmoVoice.getInstance().getMessagePrefix("reconnect_sent"));
        PlayerListener.reconnectPlayer((Player) sender);
        return true;
    }
}
