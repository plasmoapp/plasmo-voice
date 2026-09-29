package su.plo.voice.util.version;

import kotlin.text.StringsKt;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Accessors(fluent = true)
@RequiredArgsConstructor
public final class SemanticVersion {
    // neoforge-1.21.1-2.0.0+ALPHA => 2.0.0 alpha
    // fabric-1.21.x-2.0.0 => 2.0.0
    // 2.0.0+ALPHA => 2.0.0 alpha
    // 2.0.0-SNAPSHOT.build => 2.0.0 alpha
    // 2.0.0-SNAPSHOT => 2.0.0 alpha
    // spigot-2.0.0-beta.2 => 2.0.0 beta 2
    // 2.0.0 => 2.0.0
    private static final Pattern VERSION_PATTERN = Pattern.compile(".*((-)?(\\d+)\\.(\\d+)\\.(\\d+).*)");
    private static final Pattern BETA_PATTERN = Pattern.compile("-beta(?:\\.(\\d+))?", Pattern.CASE_INSENSITIVE);

    public static SemanticVersion parse(@NonNull String strVersion) {
        Matcher matcher = VERSION_PATTERN.matcher(strVersion);
        if (!matcher.matches()) throw new IllegalArgumentException("Bad version. Valid format: X.X.X");

        Matcher betaMatcher = BETA_PATTERN.matcher(matcher.group(1));
        boolean beta = betaMatcher.find();

        int major, minor, patch, betaNumber;

        try {
            major = Integer.parseInt(matcher.group(3));
            minor = Integer.parseInt(matcher.group(4));
            patch = Integer.parseInt(matcher.group(5));
            betaNumber = beta && betaMatcher.group(1) != null ? Integer.parseInt(betaMatcher.group(1)) : 0;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Bad version. Valid format: X.X.X", e);
        }
        if (StringsKt.endsWith(strVersion, "-SNAPSHOT", true)) {
            strVersion = strVersion.substring(0, strVersion.length() - "-SNAPSHOT".length());
        }

        Branch branch;
        if (strVersion.contains("+") || strVersion.toLowerCase().contains("snapshot")) {
            branch = Branch.ALPHA;
        } else if (beta) {
            branch = Branch.BETA;
        } else {
            branch = Branch.RELEASE;
        }

        return new SemanticVersion(
                strVersion,
                major,
                minor,
                patch,
                branch,
                branch == Branch.BETA ? betaNumber : 0
        );
    }

    private final String string;

    private final int major;
    private final int minor;
    private final int patch;
    private final Branch branch;
    private final int betaNumber;

    public boolean isOutdated(@NonNull SemanticVersion version) {
        if (major != version.major) {
            return major < version.major;
        } else if (minor != version.minor) {
            return minor < version.minor;
        } else if (patch != version.patch) {
            return patch < version.patch;
        } else if (branch != version.branch) {
            return branch.compareTo(version.branch) < 0;
        } else {
            return branch == Branch.BETA && betaNumber < version.betaNumber;
        }
    }

    public boolean isRelease() {
        return this.branch == Branch.RELEASE;
    }

    public int asInt() {
        return major * 100 + minor * 10 + patch;
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
        ALPHA,
        BETA,
        RELEASE
    }
}
