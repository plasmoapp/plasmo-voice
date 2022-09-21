package su.plo.voice.api.server.audio.line;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.proto.data.audio.line.SourceLine;

public interface ServerSourceLine extends SourceLine {

    /**
     * Gets the line's addon
     *
     * @return the line's addon
     */
    @NotNull AddonContainer getAddon();

    void setIcon(@NotNull String icon);
}
