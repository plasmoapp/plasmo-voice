package su.plo.voice.commands;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import su.plo.voice.PlasmoVoice;
import su.plo.voice.common.packets.tcp.ClientMutedPacket;
import su.plo.voice.data.ServerMutedEntity;
import su.plo.voice.events.PlayerVoiceMuteEvent;
import su.plo.voice.listeners.PluginChannelListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VoiceMute implements TabExecutor {
    private final Pattern pattern = Pattern.compile("([0-9]*)([mhdw])?");
    private final Pattern integerPattern = Pattern.compile("^([0-9]*)$");

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

        String durationMessage = PlasmoVoice.getInstance().getMessage("mute_durations.permanent");
        long duration = 0;
        if (args.length > 1) {
            if (!args[1].startsWith("perm")) {
                Matcher matcher = pattern.matcher(args[1]);
                if (matcher.find()) {
                    duration = Integer.parseInt(matcher.group(1));
                    if (duration > 0) {
                        String type = matcher.group(2);
                        if (type == null) {
                            type = "";
                        }

                        switch (type) {
                            case "m":
                                durationMessage = String.format(PlasmoVoice.getInstance().getMessage("mute_durations.minutes"), duration);
                                duration *= 60;
                                break;
                            case "h":
                                durationMessage = String.format(PlasmoVoice.getInstance().getMessage("mute_durations.hours"), duration);
                                duration *= 3600;
                                break;
                            case "d":
                                durationMessage = String.format(PlasmoVoice.getInstance().getMessage("mute_durations.days"), duration);
                                duration *= 86400;
                                break;
                            case "w":
                                durationMessage = String.format(PlasmoVoice.getInstance().getMessage("mute_durations.weeks"), duration);
                                duration *= 604800;
                                break;
                            default:
                                durationMessage = String.format(PlasmoVoice.getInstance().getMessage("mute_durations.seconds"), duration);
                                break;
                        }
                    } else {
                        durationMessage = String.format(PlasmoVoice.getInstance().getMessage("mute_durations.seconds"), duration);
                    }
                }
            }
        }

        if (duration > 0) {
            duration *= 1000;
            duration += System.currentTimeMillis();
        }

        String reason = null;
        if (args.length > 2) {
            reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        }

        ServerMutedEntity serverMuted = new ServerMutedEntity(player.getUniqueId(), duration, reason);
        PlasmoVoice.muted.put(player.getUniqueId(), serverMuted);

        PluginChannelListener.sendToClients(new ClientMutedPacket(serverMuted.uuid, serverMuted.to), player);
        sender.sendMessage(String.format(PlasmoVoice.getInstance().getMessagePrefix("muted"), player.getName()));

        player.sendMessage((duration > 0
                ? PlasmoVoice.getInstance().getMessagePrefix("player_muted")
                : PlasmoVoice.getInstance().getMessagePrefix("player_muted_perm"))
                .replace("{duration}", durationMessage)
                .replace("{reason}", reason != null
                        ? reason
                        : PlasmoVoice.getInstance().getMessage("mute_no_reason")
                )
        );

        Bukkit.getPluginManager().callEvent(new PlayerVoiceMuteEvent(player, duration));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 2) {
            if (args[1].isEmpty()) {
                return ImmutableList.of("permanent");
            }

            Matcher matcher = integerPattern.matcher(args[1]);
            if (matcher.find()) {
                List<String> durations = new ArrayList<>();
                durations.add(args[1] + "m");
                durations.add(args[1] + "h");
                durations.add(args[1] + "d");
                durations.add(args[1] + "w");
                return durations;
            }
        }
        return null;
    }
}
