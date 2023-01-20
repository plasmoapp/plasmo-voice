package su.plo.voice.api.server.config;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public interface ResourceLoader {

    @NotNull InputStream load(@NotNull String resourcePath) throws IOException;
}
