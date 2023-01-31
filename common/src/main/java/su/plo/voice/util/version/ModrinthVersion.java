package su.plo.voice.util.version;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Optional;

@Data
@Accessors(fluent = true)
@RequiredArgsConstructor
public final class ModrinthVersion {

    private static final Gson GSON = new Gson();

    public static Optional<ModrinthVersion> checkForUpdates(@NonNull String voiceVersion,
                                                   @NonNull String minecraftVersion,
                                                   @NonNull ModrinthLoader loader) throws IOException {
        SemanticVersion version = SemanticVersion.parse(voiceVersion);

        return ModrinthVersion.getLatest(minecraftVersion, loader, !version.isRelease(), null)
                .filter(latestVersion ->
                        (!version.isRelease() && !latestVersion.version().equals(version) && !latestVersion.version().isOutdated(version)) || // alpha check
                        version.isOutdated(latestVersion.version())
                );
    }

    public static Optional<ModrinthVersion> from(@NonNull String stringVersion,
                                                 @NonNull String minecraftVersion,
                                                 @NonNull ModrinthLoader loader) throws IOException {

        JsonArray versions = getVersions(minecraftVersion, loader);

        for (JsonElement jsonElement : versions) {
            JsonObject version = jsonElement.getAsJsonObject();

            String versionNumber = version.get("version_number").getAsString();

            if (versionNumber.contains(stringVersion)) {
                JsonArray files = version.get("files").getAsJsonArray();
                if (files.size() == 0) continue;

                return Optional.of(new ModrinthVersion(
                        SemanticVersion.parse(versionNumber),
                        files.get(0).getAsJsonObject().get("url").getAsString())
                );
            }
        }

        return Optional.empty();
    }

    public static Optional<ModrinthVersion> getLatest(@NonNull String minecraftVersion,
                                                      @NonNull ModrinthLoader loader,
                                                      boolean alpha,
                                                      @Nullable SemanticVersion targetVersion) throws IOException {
        JsonArray versions = getVersions(minecraftVersion, loader);

        for (JsonElement jsonElement : versions) {
            JsonObject version = jsonElement.getAsJsonObject();

            String versionType = version.get("version_type").getAsString();
            if (versionType.equals("alpha") && !alpha) continue;

            String versionNumber = version.get("version_number").getAsString();
            JsonArray files = version.get("files").getAsJsonArray();
            if (files.size() == 0) continue;

            SemanticVersion semanticVersion = SemanticVersion.parse(versionNumber);
            if (targetVersion != null && semanticVersion.major() != targetVersion.major()) continue;

            return Optional.of(new ModrinthVersion(
                    semanticVersion,
                    files.get(0).getAsJsonObject().get("url").getAsString())
            );
        }

        return Optional.empty();
    }

    private static JsonArray getVersions(@NonNull String minecraftVersion,
                                         @NonNull ModrinthLoader loader) throws IOException {
        URL url = new URL(String.format(
                "https://api.modrinth.com/v2/project/plasmo-voice/version?loaders=[%%22%s%%22]&game_versions=[%%22%s%%22]",
                loader, minecraftVersion
        ));

        try (InputStream in = url.openStream();
             Reader reader = new InputStreamReader(in)
        ) {
            return GSON.fromJson(reader, JsonArray.class);
        }
    }
    private final SemanticVersion version;
    private final String downloadLink;
}
