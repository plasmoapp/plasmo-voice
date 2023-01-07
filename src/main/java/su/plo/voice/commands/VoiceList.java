package su.plo.voice.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.plo.voice.PlasmoVoice;
import su.plo.voice.socket.SocketClientUDP;
import su.plo.voice.socket.SocketServerUDP;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VoiceList implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        List<String> clients = SocketServerUDP.clients.values()
                .stream()
                .map(SocketClientUDP::getPlayer)
                .map(Player::getName)
                .collect(Collectors.toList());

        List<String> clientsToRemove = new ArrayList<>();
        if (sender instanceof Player) {
            for (Player currentPlayer : Bukkit.getOnlinePlayers()) {
                if (!((Player) sender).canSee(currentPlayer)) {
                    clientsToRemove.add(currentPlayer.getName());
                }
            }
        }
        clients.removeAll(clientsToRemove);

        sender.sendMessage(PlasmoVoice.getInstance().getMessagePrefix("list")
                .replace("{count}", String.valueOf(clients.size()))
                .replace("{online_players}", String.valueOf(Bukkit.getOnlinePlayers().size() - clientsToRemove.size()))
                .replace("{players}", String.join(", ", clients)));
        return true;
    }
}
