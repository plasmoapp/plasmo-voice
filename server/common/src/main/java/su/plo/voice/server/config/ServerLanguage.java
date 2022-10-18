package su.plo.voice.server.config;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import su.plo.config.Config;
import su.plo.config.ConfigField;
import su.plo.voice.api.server.mute.MuteDurationUnit;

@Config
@Data
@Accessors(fluent = true)
public final class ServerLanguage {

    @ConfigField
    private String noPermissions = "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.";
    @ConfigField
    private String playerNotFound = "Player not found";
    @ConfigField
    private String playerOnlyCommand = "Only player can execute this command";
    @ConfigField
    private String modMissingKickMessage = "Sorry, you need to install the Plasmo Voice mod to play on this server.\\nDownload here: https://www.curseforge.com/minecraft/mc-mods/plasmo-voice-client";

    @ConfigField
    private Mutes mutes = new Mutes();
    @ConfigField
    private Commands commands = new Commands();

    @Config
    @Data
    public static class Mutes {

        @ConfigField
        private String emptyReason = "not specified";
        @ConfigField
        private String temporallyMuted = "You've been muted %1$s. Reason: %2$s";
        @ConfigField
        private String permanentlyMuted = "You've been permanently muted. Reason: %s";
        @ConfigField
        private String unmuted = "You've been unmuted";
        @ConfigField
        private Durations durations = new Durations();

        @Config
        @Data
        public static class Durations {

            @ConfigField
            private String seconds = "for %s sec";
            @ConfigField
            private String minutes = "for %s min";
            @ConfigField
            private String hours = "for %s h";
            @ConfigField
            private String days = "for %s d";
            @ConfigField
            private String weeks = "for %s w";

            public String format(long duration, @NotNull MuteDurationUnit durationUnit) {
                switch (durationUnit) {
                    case MINUTE:
                        return String.format(minutes(), duration);
                    case HOUR:
                        return String.format(hours(), duration);
                    case DAY:
                        return String.format(days(), duration);
                    case WEEK:
                        return String.format(weeks(), duration);
                    case TIMESTAMP:
                        long diff = duration - System.currentTimeMillis();
                        if (diff <= 0L) {
                            throw new IllegalArgumentException("TIMESTAMP duration should be in the future");
                        }

                        return String.format(
                                seconds(),
                                diff / 1_000L
                        );
                    default:
                        return String.format(seconds(), duration);
                }
            }
        }
    }

    @Config
    @Data
    public static class Commands {

        @ConfigField
        private MuteCommand mute = new MuteCommand();
        @ConfigField
        private UnmuteCommand unmute = new UnmuteCommand();
        @ConfigField
        private MuteListCommand muteList = new MuteListCommand();
        @ConfigField
        private ListCommand list = new ListCommand();
        @ConfigField
        private ReconnectCommand reconnect = new ReconnectCommand();
        @ConfigField
        private ReloadCommand reload = new ReloadCommand();
    }

    @Config
    @Data
    public static class MuteCommand {

        @ConfigField
        private String usage = "Usage: /vmute <player> [duration] [reason]";
        @ConfigField
        private String alreadyMuted = "%s already muted";
        @ConfigField
        private String temporallyMuted = "Muted %1$s %2$s. Reason: %3$s";
        @ConfigField
        private String permanentlyMuted = "%1$s is permanently muted. Reason: %2$s";
    }

    @Config
    @Data
    public static class UnmuteCommand {

        @ConfigField
        private String usage = "Usage: /vunmute <player>";
        @ConfigField
        private String notMuted = "%s not muted";
        @ConfigField
        private String unmuted = "Unmuted %s";
    }

    @Config
    @Data
    public static class MuteListCommand {

        @ConfigField
        private String header = "Muted players:";
        @ConfigField
        private String entry = "%1$s, expires: %2$s. Reason: %3$s";
        @ConfigField
        private String entryMutedBy = "%1$s muted by %2$s, expires: %3$s. Reason: %4$s";
        @ConfigField
        private String empty = "No players are muted";
        @ConfigField
        private String expireAt = "%1$s at %2$s";
        @ConfigField
        private String neverExpires = "never";
        @ConfigField
        private String expirationDate = "yyyy.MM.dd";
        @ConfigField
        private String expirationTime = "HH:mm:ss";
    }

    @Config
    @Data
    public static class ListCommand {

        @ConfigField
        private String message = "Clients (%1$s/%2$s): %3$s";
        @ConfigField
        private String empty = "no players with Plasmo Voice installed";
    }

    @Config
    @Data
    public static class ReconnectCommand {

        @ConfigField
        private String message = "Reconnect packet sent";
    }

    @Config
    @Data
    public static class ReloadCommand {

        @ConfigField
        private String message = "Config reloaded";
    }
}
