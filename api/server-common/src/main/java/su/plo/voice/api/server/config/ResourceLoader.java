package su.plo.voice.api.server.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

public interface ResourceLoader {

    @Nullable InputStream load(@NotNull String resourcePath) throws IOException;
}
