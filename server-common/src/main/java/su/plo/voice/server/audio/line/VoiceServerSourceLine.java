package su.plo.voice.server.audio.line;

import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;

@ToString(callSuper = true)
public class VoiceServerSourceLine extends VoiceSourceLine implements ServerSourceLine {

    @Getter
    protected final AddonContainer addon;

    public VoiceServerSourceLine(@NotNull AddonContainer addon,
                                 @NotNull String name,
                                 @NotNull String translation,
                                 @NotNull String icon,
                                 int weight) {
        super(name, translation, icon, weight, null);

        this.addon = addon;
    }

    @Override
    public void setIcon(@NotNull String icon) {
        this.icon = icon;
    }

    @Override
    public @NotNull VoiceSourceLine getPlayerSourceLine(@NotNull VoicePlayer<?> player) {
        return this;
    }
}
