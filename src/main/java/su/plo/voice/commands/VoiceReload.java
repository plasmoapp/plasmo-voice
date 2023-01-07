package su.plo.voice.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.plo.voice.PlasmoVoice;
import su.plo.voice.PlasmoVoiceConfig;
import su.plo.voice.common.packets.tcp.ConfigPacket;
import su.plo.voice.common.packets.tcp.PacketTCP;
import su.plo.voice.events.PlayerConfigEvent;
import su.plo.voice.socket.SocketServerUDP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.UUID;

public class VoiceReload implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        PlasmoVoice.getInstance().reloadConfig();
        PlasmoVoice.getInstance().updateConfig();

        PlasmoVoiceConfig config = PlasmoVoice.getInstance().getVoiceConfig();

        Bukkit.getScheduler().runTaskAsynchronously(PlasmoVoice.getInstance(), () -> {
            try {
                Enumeration<UUID> it = SocketServerUDP.clients.keys();
                while (it.hasMoreElements()) {
                    Player player = Bukkit.getPlayer(it.nextElement());
                    if (player == null) continue;

                    ConfigPacket configPacket = new ConfigPacket(
                            config.getSampleRate(),
                            new ArrayList<>(config.getDistances()),
                            config.getDefaultDistance(),
                            config.getMaxPriorityDistance(),
                            config.getFadeDivisor(),
                            config.getPriorityFadeDivisor(),
                            config.isDisableVoiceActivation() || !player.hasPermission("voice.activation")
                    );

                    PlayerConfigEvent event = new PlayerConfigEvent(player, configPacket, PlayerConfigEvent.Cause.RELOAD);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        continue;
                    }

                    byte[] pkt = PacketTCP.write(configPacket);

                    player.sendPluginMessage(PlasmoVoice.getInstance(), "plasmo:voice", pkt);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        sender.sendMessage(PlasmoVoice.getInstance().getMessagePrefix("reloaded"));
        return true;
    }
}
