package su.plo.voice.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import su.plo.voice.PlasmoVoice;
import su.plo.voice.common.entities.MutedEntity;
import su.plo.voice.common.packets.tcp.ClientMutedPacket;
import su.plo.voice.listeners.PluginChannelListener;

import java.util.List;

public class VoiceMute implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(PlasmoVoice.getInstance().getMessagePrefix("help.mute"));
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if(player == null) {
            sender.sendMessage(PlasmoVoice.getInstance().getMessagePrefix("player_not_found"));
            return true;
        }

        // TODO сделать формат 1s 1m 1h 1d 1w...
        long duration = 0;
        if(args.length > 1) {
            try {
                duration = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {}
        }

        if(duration > 0) {
            duration += System.currentTimeMillis();
            duration *= 1000;
        }

        MutedEntity muted = new MutedEntity(player.getUniqueId(), duration);
        PlasmoVoice.muted.put(player.getUniqueId(), muted);

        PluginChannelListener.sendToClients(new ClientMutedPacket(muted.uuid, muted.to));
        sender.sendMessage(String.format(PlasmoVoice.getInstance().getMessagePrefix("muted"), player.getName()));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        return null;
    }
}
