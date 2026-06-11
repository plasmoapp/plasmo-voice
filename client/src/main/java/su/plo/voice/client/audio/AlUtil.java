package su.plo.voice.client.audio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.EXTThreadLocalContext;
import su.plo.voice.api.client.audio.device.AlContextAudioDevice;
import su.plo.voice.util.version.SemanticVersion;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

public final class AlUtil {

    private static final Logger LOGGER = LogManager.getLogger(AlUtil.class);

    private static boolean alSoftVersionResolved = false;
    private static SemanticVersion alSoftVersion = null;

    /**
     * OpenAL Soft 1.25.0 and 1.25.1 corrupt the multichannel to mono downmix on capture,
     * producing crackling/noise:
     * <a href="https://github.com/kcat/openal-soft/pull/1246">kcat/openal-soft#1246</a>,
     * fixed in 1.25.2.
     */
    public static boolean isCaptureMonoDownmixBroken() {
        SemanticVersion version = getAlSoftVersion();
        if (version == null) return false;

        SemanticVersion firstBroken = new SemanticVersion("1.25.0", 1, 25, 0, SemanticVersion.Branch.RELEASE);
        SemanticVersion firstFixed = new SemanticVersion("1.25.2", 1, 25, 2, SemanticVersion.Branch.RELEASE);

        return !version.isOutdated(firstBroken) && version.isOutdated(firstFixed);
    }

    public static synchronized SemanticVersion getAlSoftVersion() {
        if (alSoftVersionResolved) return alSoftVersion;
        alSoftVersionResolved = true;

        alSoftVersion = parseAlSoftVersion(AL10.alGetString(AL10.AL_VERSION));

        if (alSoftVersion != null) {
            LOGGER.info("Detected OpenAL Soft {}", alSoftVersion);
        }

        return alSoftVersion;
    }

    // version string looks like "1.1 ALSOFT 1.25.1"
    private static SemanticVersion parseAlSoftVersion(String version) {
        if (version == null) return null;

        String[] alVersionStringParts = version.split(" ");
        String alVersion = alVersionStringParts[alVersionStringParts.length - 1];
        String[] alVersionParts = alVersion.split("\\.");

        if (alVersionParts.length < 2) return null;

        try {
            int major = Integer.parseInt(alVersionParts[0]);
            int minor = Integer.parseInt(alVersionParts[1]);
            int patch = alVersionParts.length >= 3 ? Integer.parseInt(alVersionParts[2]) : 0;

            return new SemanticVersion(alVersion, major, minor, patch, SemanticVersion.Branch.RELEASE);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case 40961:
                return "Invalid name parameter.";
            case 40962:
                return "Invalid enumerated parameter value.";
            case 40963:
                return "Invalid parameter parameter value.";
            case 40964:
                return "Invalid operation.";
            case 40965:
                return "Unable to allocate memory.";
            default:
                return "An unrecognized error occurred.";
        }
    }

    public static void checkDeviceContext(AlContextAudioDevice device) {
        if (!sameDeviceContext(device)) {
            throw new IllegalStateException("This function should be called in the device context thread! Use AlAudioDevice::runInContext to run this function");
        }
    }

    public static boolean sameDeviceContext(AlContextAudioDevice device) {
        return EXTThreadLocalContext.alcGetThreadContext() == device.getContextPointer();
    }

    public static boolean checkErrors(String sectionName) {
        int i = AL11.alGetError();
        if (i != 0) {
            LOGGER.error("{}: {}", sectionName, getErrorMessage(i));
            return true;
        } else {
            return false;
        }
    }

    private static String getAlcErrorMessage(int errorCode) {
        switch (errorCode) {
            case 40961:
                return "Invalid device.";
            case 40962:
                return "Invalid context.";
            case 40963:
                return "Illegal enum.";
            case 40964:
                return "Invalid value.";
            case 40965:
                return "Unable to allocate memory.";
            default:
                return "An unrecognized error occurred.";
        }
    }

    public static boolean checkAlcErrors(long deviceHandle, String sectionName) {
        int i = ALC11.alcGetError(deviceHandle);
        if (i != 0) {
            LOGGER.error("{} {}: {}", sectionName, deviceHandle, getAlcErrorMessage(i));
            return true;
        } else {
            return false;
        }
    }

    public static int getFormatId(AudioFormat format) {
        Encoding encoding = format.getEncoding();
        int i = format.getChannels();
        int j = format.getSampleSizeInBits();
        if (encoding.equals(Encoding.PCM_UNSIGNED) || encoding.equals(Encoding.PCM_SIGNED)) {
            if (i == 1) {
                if (j == 8) {
                    return 4352;
                }

                if (j == 16) {
                    return 4353;
                }
            } else if (i == 2) {
                if (j == 8) {
                    return 4354;
                }

                if (j == 16) {
                    return 4355;
                }
            }
        }

        throw new IllegalArgumentException("Invalid audio format: " + format);
    }

    private AlUtil() {
    }
}
