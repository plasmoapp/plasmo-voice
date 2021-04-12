package su.plo.voice.sound;

import club.minnced.opus.util.OpusLibrary;

import java.io.IOException;

public final class AudioNatives
{
    private static boolean initialized;
    private static boolean audioSupported;

    private AudioNatives() {}

    /**
     * Checks whether the opus binary was loaded, if not it will be initialized here.
     * <br>This is used by JDA to check at runtime whether the opus library is available or not.
     *
     */
    public static synchronized void ensureOpus()
    {
        if (initialized)
            return;
        initialized = true;
        try
        {
            if (OpusLibrary.isInitialized()) {
                audioSupported = true;
                return;
            }
            audioSupported = OpusLibrary.loadFromJar();
        }
        catch (Throwable e)
        {
            handleException(e);
        }
        finally
        {
            if (audioSupported)
                System.out.println("Audio System successfully setup!");
            else
                System.out.println("Audio System encountered problems while loading, thus, is disabled.");
        }
    }

    private static void handleException(Throwable e)
    {
        if (e instanceof UnsupportedOperationException)
        {
            System.out.printf("Sorry, JDA's audio system doesn't support this system.\n%s\n", e.getMessage());
        }
        else if (e instanceof NoClassDefFoundError)
        {
            System.out.println("Missing opus dependency, unable to initialize audio!");
        }
        else if (e instanceof IOException)
        {
            System.out.println("There was an IO Exception when setting up the temp files for audio.");
            e.printStackTrace();
        }
        else if (e instanceof UnsatisfiedLinkError)
        {
            System.out.println("JDA encountered a problem when attempting to load the Native libraries. Contact a DEV.");
            e.printStackTrace();
        }
        else if (e instanceof Error)
        {
            throw (Error) e;
        }
        else
        {
            System.out.println("An unknown exception occurred while attempting to setup JDA's audio system!");
            e.printStackTrace();
        }
    }
}