package su.plo.voice.client.audio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.EXTThreadLocalContext;
import su.plo.voice.api.client.audio.device.AlAudioDevice;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

public final class AlUtil {

    private static final Logger LOGGER = LogManager.getLogger(AlUtil.class);

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

    public static void checkDeviceContext(AlAudioDevice device) {
        if (!sameDeviceContext(device)) {
            throw new IllegalStateException("This function should be called in the device context thread! Use AlAudioDevice::runInContext to run this function");
        }
    }

    public static boolean sameDeviceContext(AlAudioDevice device) {
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
            LOGGER.error("{}{}: {}", sectionName, deviceHandle, getAlcErrorMessage(i));
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
