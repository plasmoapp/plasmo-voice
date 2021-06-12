package su.plo.voice.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.PlasmoVoice;
import su.plo.voice.common.packets.tcp.ClientUnmutedPacket;
import su.plo.voice.data.ServerMutedEntity;
import su.plo.voice.events.PlayerVoiceUnmuteEvent;
import su.plo.voice.listeners.PluginChannelListener;

import java.util.List;

public class VoiceUnmute implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(PlasmoVoice.getInstance().getMessagePrefix("help.unmute"));
            return true;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        if(player.getFirstPlayed() == 0) {
            sender.sendMessage(PlasmoVoice.getInstance().getMessagePrefix("player_not_found"));
            return true;
        }

        ServerMutedEntity muted = PlasmoVoice.muted.get(player.getUniqueId());
        if(muted == null) {
            sender.sendMessage(String.format(PlasmoVoice.getInstance().getMessagePrefix("not_muted"), player.getName()));
            return true;
        }

        if(muted.to > 0 && muted.to < System.currentTimeMillis()) {
            PlasmoVoice.muted.remove(muted.uuid);
            sender.sendMessage(String.format(PlasmoVoice.getInstance().getMessagePrefix("not_muted"), player.getName()));
            return true;
        }

        PlasmoVoice.muted.remove(muted.uuid);

        Player onlinePlayer = Bukkit.getPlayer(player.getUniqueId());
        if(onlinePlayer != null) {
            PluginChannelListener.sendToClients(new ClientUnmutedPacket(player.getUniqueId()), onlinePlayer);
        }

        sender.sendMessage(String.format(PlasmoVoice.getInstance().getMessagePrefix("unmuted"), player.getName()));

        Bukkit.getPluginManager().callEvent(new PlayerVoiceUnmuteEvent(player));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }
}
