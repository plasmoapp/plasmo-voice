package su.plo.voice.util.version;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Accessors(fluent = true)
@RequiredArgsConstructor
public final class SemanticVersion {

    // forge-1.19.3-2.0.0+ALPHA => 2.0.0 alpha
    // fabric-1.19.x-2.0.0 => 2.0.0
    // 2.0.0+ALPHA => 2.0.0 alpha
    // 2.0.0-SNAPSHOT.build => 2.0.0 alpha
    // 2.0.0-SNAPSHOT => 2.0.0 alpha
    // 2.0.0 => 2.0.0
    private static final Pattern VERSION_PATTERN = Pattern.compile(".*((-)?(\\d+)\\.(\\d+)\\.(\\d+).*)");

    public static SemanticVersion parse(@NonNull String strVersion) {
        Matcher matcher = VERSION_PATTERN.matcher(strVersion);
        if (!matcher.matches()) throw new IllegalArgumentException("Bad version. Valid format: X.X.X");

        int major, minor, patch;

        try {
            major = Integer.parseInt(matcher.group(3));
            minor = Integer.parseInt(matcher.group(4));
            patch = Integer.parseInt(matcher.group(5));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Bad version. Valid format: X.X.X", e);
        }

        return new SemanticVersion(
                strVersion,
                major,
                minor,
                patch,
                strVersion.contains("+") || strVersion.toLowerCase().contains("snapshot")
                        ? SemanticVersion.Branch.ALPHA
                        : SemanticVersion.Branch.RELEASE
        );
    }

    private final String string;

    private final int major;
    private final int minor;
    private final int patch;
    private final Branch branch;

    public boolean isOutdated(@NonNull SemanticVersion version) {
        if (major != version.major) {
            return major < version.major;
        } else if (minor != version.minor) {
            return minor < version.minor;
        } else if (patch != version.patch) {
            return patch < version.patch;
        } else {
            return branch == Branch.ALPHA && version.branch == Branch.RELEASE;
        }
    }

    public boolean isRelease() {
        return this.branch == Branch.RELEASE;
    }

    public String prettyString() {
        Matcher matcher = VERSION_PATTERN.matcher(string);
        if (!matcher.matches()) return string;

        return matcher.group(1);
    }

    @Override
    public String toString() {
        return prettyString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof SemanticVersion)) return false;

        SemanticVersion version = (SemanticVersion) o;
        return this.toString().equals(version.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public enum Branch {

        RELEASE,
        ALPHA
    }
}

