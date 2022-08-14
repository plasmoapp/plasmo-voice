package su.plo.voice.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionUtil {

    private static final Pattern versionPattern = Pattern.compile("(\\d).(\\d).(\\d).*");

    public static int[] parseVersion(String strVersion) {
        int[] version = new int[3];
        Matcher matcher = versionPattern.matcher(strVersion);
        if (!matcher.matches()) throw new IllegalArgumentException("Bad version. Valid format: X.X.X");

        try {
            version[0] = Integer.parseInt(matcher.group(1));
            version[1] = Integer.parseInt(matcher.group(2));
            version[2] = Integer.parseInt(matcher.group(2));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Bad version. Valid format: X.X.X", e);
        }

        return version;
    }

    private VersionUtil() {
    }
}
