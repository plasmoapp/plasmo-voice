package su.plo.voice.commands;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import su.plo.voice.PlasmoVoice;
import su.plo.voice.PlasmoVoiceAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VoiceMute implements TabExecutor {
    private final Pattern pattern = Pattern.compile("^([0-9]*)([mhdwu])?$");
    private final Pattern integerPattern = Pattern.compile("^([0-9]*)$");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(PlasmoVoice.getInstance().getMessagePrefix("help.mute"));
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(PlasmoVoice.getInstance().getMessagePrefix("player_not_found"));
            return true;
        }

        if (PlasmoVoice.getInstance().isMuted(player.getUniqueId())) {
            sender.sendMessage(PlasmoVoice.getInstance().getMessagePrefix("already_muted")
                    .replace("{player}", player.getName()));
            return true;
        }

        PlasmoVoiceAPI.DurationUnit durationUnit = null;
        long duration = 0;
        String reason = null;
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
                                durationUnit = PlasmoVoiceAPI.DurationUnit.MINUTES;
                                break;
                            case "h":
                                durationUnit = PlasmoVoiceAPI.DurationUnit.HOURS;
                                break;
                            case "d":
                                durationUnit = PlasmoVoiceAPI.DurationUnit.DAYS;
                                break;
                            case "w":
                                durationUnit = PlasmoVoiceAPI.DurationUnit.WEEKS;
                                break;
                            case "u":
                                duration = duration - System.currentTimeMillis() / 1000L;
                                durationUnit = PlasmoVoiceAPI.DurationUnit.TIMESTAMP;
                                break;
                            default:
                                durationUnit = PlasmoVoiceAPI.DurationUnit.SECONDS;
                                break;
                        }
                    } else {
                        durationUnit = PlasmoVoiceAPI.DurationUnit.SECONDS;
                    }
                } else {
                    reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                }
            }
        }

        if (reason == null && args.length > 2) {
            reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        }

        if (duration == 0L) {
            sender.sendMessage(PlasmoVoice.getInstance().getMessagePrefix("muted_perm")
                    .replace("{player}", player.getName())
                    .replace("{reason}", reason != null
                            ? reason
                            : PlasmoVoice.getInstance().getMessage("mute_no_reason")));
        } else {
            sender.sendMessage(PlasmoVoice.getInstance().getMessagePrefix("muted")
                    .replace("{player}", player.getName())
                    .replace("{duration}", durationUnit.format(duration))
                    .replace("{reason}", reason != null
                            ? reason
                            : PlasmoVoice.getInstance().getMessage("mute_no_reason")));
        }

        PlasmoVoice.getInstance().mute(player.getUniqueId(), duration, durationUnit, reason, false);
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
