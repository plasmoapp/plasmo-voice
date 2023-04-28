package su.plo.lib.mod.client.render.shader;

import su.plo.voice.universal.shader.BlendState;
import su.plo.voice.universal.shader.UShader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import su.plo.voice.client.ModVoiceClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ShaderUtil {

    public static UShader loadShader(@NonNull String vertName, @NonNull String fragName, @NonNull BlendState blendState) throws IOException {
        return UShader.Companion.fromLegacyShader(
                readShader(vertName, "vsh"),
                readShader(fragName, "fsh"),
                blendState
        );
    }

    private static String readShader(@NonNull String name, @NonNull String ext) throws IOException {
        try (InputStream is = ModVoiceClient.class.getResource(String.format("/shaders/%s.%s", name, ext)).openStream()) {
            return new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
        }
    }
}
