package su.plo.voice.server.command;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.lib.api.server.command.MinecraftCommand;
import su.plo.lib.api.server.command.MinecraftCommandSource;
import su.plo.lib.api.server.entity.MinecraftServerPlayerEntity;
import su.plo.voice.api.server.mute.MuteDurationUnit;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.config.ServerLanguage;
import su.plo.voice.server.mute.VoiceMuteManager;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public final class VoiceMuteCommand implements MinecraftCommand {

    private static final Pattern DURATION_PATTERN = Pattern.compile("^([0-9]*)([mhdwsu]|permanent)?$");
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^([0-9]*)$");

    private final BaseVoiceServer voiceServer;
    private final MinecraftServerLib minecraftServer;

    @Override
    public void execute(@NotNull MinecraftCommandSource source, @NotNull String[] arguments) {
        ServerLanguage language = voiceServer.getLanguages().getLanguage(source);

        if (arguments.length == 0) {
            source.sendMessage(language.commands().mute().usage());
            return;
        }

        Optional<MinecraftServerPlayerEntity> player = minecraftServer.getPlayerByName(arguments[0]);
        if (!player.isPresent()) {
            source.sendMessage(language.playerNotFound());
            return;
        }

        VoiceMuteManager muteManager = (VoiceMuteManager) voiceServer.getMuteManager();

        if (muteManager.getMute(player.get().getUUID()).isPresent()) {
            source.sendMessage(String.format(
                    language.commands().mute().alreadyMuted(),
                    player.get().getName()
            ));
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
            source.sendMessage(String.format(
                    language.commands().mute().permanentlyMuted(),
                    player.get().getName(),
                    muteManager.formatMuteReason(language, reason)
            ));
        } else {
            try {
                source.sendMessage(String.format(
                        language.commands().mute().temporallyMuted(),
                        player.get().getName(),
                        language.mutes().durations().format(duration, durationUnit),
                        muteManager.formatMuteReason(language, reason)
                ));
            } catch (IllegalArgumentException e) {
                source.sendMessage(MinecraftTextComponent.literal(e.getMessage()));
            }
        }

        UUID mutedBy = null;
        if (source instanceof MinecraftServerPlayerEntity) {
            mutedBy = ((MinecraftServerPlayerEntity) source).getUUID();
        }

        muteManager.mute(
                player.get().getUUID(),
                mutedBy,
                duration,
                durationUnit,
                reason,
                false
        );
    }

    @Override
    public boolean hasPermission(@NotNull MinecraftCommandSource source, @Nullable String[] arguments) {
        return source.hasPermission("voice.mute");
    }

    @Override
    public List<String> suggest(@NotNull MinecraftCommandSource source, @NotNull String[] arguments) {
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

        return MinecraftCommand.super.suggest(source, arguments);
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
