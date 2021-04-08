package su.plo.voice.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import su.plo.voice.PlasmoVoice;
import su.plo.voice.socket.SocketServerUDP;

import java.util.ArrayList;
import java.util.List;

public class VoiceList implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        List<String> clients = new ArrayList<>();
        SocketServerUDP.clients.forEach((player, socket) -> {
            clients.add(player.getName());
        });

        sender.sendMessage(String.format(PlasmoVoice.getInstance().getMessagePrefix("list"),
                String.join(", ", clients)));
        return true;
    }
}
