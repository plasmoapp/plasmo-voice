package su.plo.voice.server.audio.line;

import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;
import su.plo.voice.proto.packets.tcp.clientbound.SourceLineRegisterPacket;

@ToString(callSuper = true)
public class VoiceServerSourceLine extends VoiceSourceLine implements ServerSourceLine {

    protected final PlasmoVoiceServer voiceServer;
    @Getter
    protected final AddonContainer addon;

    public VoiceServerSourceLine(@NotNull PlasmoVoiceServer voiceServer,
                                 @NotNull AddonContainer addon,
                                 @NotNull String name,
                                 @NotNull String translation,
                                 @NotNull String icon,
                                 int weight) {
        super(name, translation, icon, weight, null);

        this.voiceServer = voiceServer;
        this.addon = addon;
    }

    @Override
    public void setIcon(@NotNull String icon) {
        this.icon = icon;

    }

    /**
     * Broadcasts {@link SourceLineRegisterPacket}
     */
    public void update() {
        voiceServer.getTcpConnectionManager().broadcast(
                new SourceLineRegisterPacket(this),
                null
        );
    }
}
