package su.plo.voice.util.version;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Optional;

@Data
@Accessors(fluent = true)
@RequiredArgsConstructor
public final class ModrinthVersion {

    private static final Gson GSON = new Gson();

    public static Optional<ModrinthVersion> checkForUpdates(@NonNull String voiceVersion,
                                                            @NonNull String minecraftVersion,
                                                            @NonNull PlatformLoader loader) throws IOException {
        if (!loader.modrinthSupported()) return Optional.empty();

        return findUpdate(SemanticVersion.parse(voiceVersion), getVersions(minecraftVersion, loader));
    }

    public static Optional<ModrinthVersion> findUpdate(@NonNull SemanticVersion currentVersion,
                                                       @NonNull JsonArray versions) {
        ModrinthVersion latestVersion = null;
        boolean anyNewerHasChangelog = false;

        for (JsonElement jsonElement : versions) {
            JsonObject version = jsonElement.getAsJsonObject();

            String versionType = version.get("version_type").getAsString();
            if (currentVersion.isRelease() && !versionType.equals("release")) continue;

            JsonArray files = version.get("files").getAsJsonArray();
            if (files.size() == 0) continue;

            SemanticVersion semanticVersion = SemanticVersion.parse(version.get("version_number").getAsString());
            if (!isNewer(currentVersion, semanticVersion)) continue;

            if (latestVersion == null || latestVersion.version().isOutdated(semanticVersion)) {
                latestVersion = new ModrinthVersion(
                        semanticVersion,
                        files.get(0).getAsJsonObject().get("url").getAsString()
                );
            }

            if (hasChangelog(version)) anyNewerHasChangelog = true;
        }

        return anyNewerHasChangelog ? Optional.ofNullable(latestVersion) : Optional.empty();
    }

    private static boolean isNewer(@NonNull SemanticVersion currentVersion, @NonNull SemanticVersion version) {
        return currentVersion.isOutdated(version) ||
                (!currentVersion.isRelease() && !version.equals(currentVersion) && !version.isOutdated(currentVersion));
    }

    private static boolean hasChangelog(@NonNull JsonObject version) {
        JsonElement changelog = version.get("changelog");
        return changelog != null && !changelog.isJsonNull() && !changelog.getAsString().trim().isEmpty();
    }

    private static JsonArray getVersions(@NonNull String minecraftVersion,
                                         @NonNull PlatformLoader loader) throws IOException {
        URL url = new URL(String.format(
                "https://api.modrinth.com/v2/project/plasmo-voice/version?loaders=[%%22%s%%22]&game_versions=[%%22%s%%22]",
                URLEncoder.encode(loader.toString(), "UTF-8"),
                URLEncoder.encode(minecraftVersion, "UTF-8")
        ));

        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(10_000);

        try (InputStream in = connection.getInputStream();
             Reader reader = new InputStreamReader(in)
        ) {
            return GSON.fromJson(reader, JsonArray.class);
        }
    }

    private final SemanticVersion version;
    private final String downloadLink;
}
