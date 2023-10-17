package su.plo.voice.server.command;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.command.McCommand;
import su.plo.slib.api.command.McCommandSource;
import su.plo.slib.api.server.McServerLib;
import su.plo.slib.api.server.entity.player.McServerPlayer;
import su.plo.voice.api.server.mute.MuteDurationUnit;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.mute.VoiceMuteManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public final class VoiceMuteCommand implements McCommand {

    private static final Pattern DURATION_PATTERN = Pattern.compile("^([0-9]*)([mhdwsu]|permanent)?$");
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^([0-9]*)$");

    private final BaseVoiceServer voiceServer;
    private final McServerLib minecraftServer;

    @Override
    public void execute(@NotNull McCommandSource source, @NotNull String[] arguments) {
        if (arguments.length == 0) {
            source.sendMessage(McTextComponent.translatable("pv.error.no_permissions"));
            return;
        }

        McServerPlayer player = minecraftServer.getPlayerByName(arguments[0]);
        if (player == null) {
            source.sendMessage(McTextComponent.translatable("pv.error.player_not_found"));
            return;
        }

        VoiceMuteManager muteManager = (VoiceMuteManager) voiceServer.getMuteManager();

        if (muteManager.getMute(player.getUuid()).isPresent()) {
            source.sendMessage(
                    McTextComponent.translatable("pv.command.mute.already_muted", player.getName())
            );
            return;
        }

        int reasonSpaceIndex = 1;
        MuteDurationUnit durationUnit = null;
        long duration = 0;

        if (arguments.length > 1) {
            String durationArg = arguments[1];
            Matcher matcher = DURATION_PATTERN.matcher(durationArg);
            if (matcher.find()) {
                String type = matcher.group(2);
                if (type == null || !type.equals("permanent")) {
                    durationUnit = parseDurationUnit(type);
                }

                duration = parseDuration(matcher.group(1), durationUnit);
                reasonSpaceIndex = 2;
            }

        }

        String reason = null;
        if (arguments.length > reasonSpaceIndex) {
            reason = String.join(" ", Arrays.copyOfRange(arguments, reasonSpaceIndex, arguments.length));
        }

        if (durationUnit == null) {
            source.sendMessage(
                    McTextComponent.translatable(
                            "pv.command.mute.permanently_muted",
                            player.getName(),
                            muteManager.formatMuteReason(reason)
                    )
            );
        } else {
            try {
                source.sendMessage(
                        McTextComponent.translatable(
                                "pv.command.mute.temporarily_muted",
                                player.getName(),
                                durationUnit.translate(duration),
                                muteManager.formatMuteReason(reason)
                        )
                );
            } catch (IllegalArgumentException e) {
                source.sendMessage(McTextComponent.literal(e.getMessage()));
            }
        }

        UUID mutedBy = null;
        if (source instanceof McServerPlayer) {
            mutedBy = ((McServerPlayer) source).getUuid();
        }

        muteManager.mute(
                player.getUuid(),
                mutedBy,
                duration,
                durationUnit,
                reason,
                false
        );
    }

    @Override
    public boolean hasPermission(@NotNull McCommandSource source, @Nullable String[] arguments) {
        return source.hasPermission("pv.mute");
    }

    @Override
    public @NotNull List<String> suggest(@NotNull McCommandSource source, @NotNull String[] arguments) {
        if (arguments.length <= 1) {
            return Suggestions.players(minecraftServer, source, arguments.length > 0 ? arguments[0] : "");
        } else if (arguments.length == 2) {
            if (arguments[1].isEmpty()) {
                return ImmutableList.of("permanent");
            } else if ("permanent".startsWith(arguments[1])) {
                return ImmutableList.of("permanent");
            }

            Matcher matcher = INTEGER_PATTERN.matcher(arguments[1]);
            if (matcher.find()) {
                List<String> durations = new ArrayList<>();
                durations.add(arguments[1] + "s");
                durations.add(arguments[1] + "m");
                durations.add(arguments[1] + "h");
                durations.add(arguments[1] + "d");
                durations.add(arguments[1] + "w");
                return durations;
            }
        }

        return McCommand.super.suggest(source, arguments);
    }

    private long parseDuration(@NotNull String durationString, @Nullable MuteDurationUnit durationUnit) {
        if (durationUnit == null) return 0L;

        long duration = Long.parseLong(durationString);
        if (durationUnit == MuteDurationUnit.TIMESTAMP) {
            return duration * 1_000L;
        }

        return duration;
    }

    private MuteDurationUnit parseDurationUnit(@Nullable String type) {
        switch (Strings.nullToEmpty(type)) {
            case "m":
                return MuteDurationUnit.MINUTE;
            case "h":
                return MuteDurationUnit.HOUR;
            case "d":
                return MuteDurationUnit.DAY;
            case "w":
                return MuteDurationUnit.WEEK;
            case "u":
                return MuteDurationUnit.TIMESTAMP;
            default:
                return MuteDurationUnit.SECOND;
        }
    }
}
