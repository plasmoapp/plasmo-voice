package su.plo.voice.api.client.audio.device.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.device.AlContextAudioDevice;

/**
 * Represents an OpenAL audio source.
 */
public interface AlSource extends DeviceSource {

    /**
     * Gets the OpenAL audio device associated with this source.
     *
     * @return The OpenAL audio device.
     */
    @NotNull AlContextAudioDevice getDevice();

    /**
     * Gets the pointer value associated with this audio source.
     *
     * @return The pointer value.
     */
    long getPointer();

    /**
     * Starts playback of the audio source.
     */
    void play();

    /**
     * Stops playback of the audio source.
     */
    void stop();

    /**
     * Pauses playback of the audio source.
     */
    void pause();

    /**
     * Gets the current state of the audio source.
     *
     * @return The state of the audio source.
     */
    State getState();

    /**
     * Gets the pitch of the audio source.
     *
     * @return The pitch value.
     */
    float getPitch();

    /**
     * Sets the pitch of the audio source.
     *
     * @param pitch The pitch value to set.
     */
    void setPitch(float pitch);

    /**
     * Gets the volume of the audio source.
     *
     * @return The volume value.
     */
    float getVolume();

    /**
     * Sets the volume of the audio source.
     *
     * @param volume The volume value to set.
     */
    void setVolume(float volume);

    /**
     * Checks if the audio source is in relative mode.
     *
     * @return {@code true} if the audio source is in relative mode; otherwise, {@code false}.
     */
    boolean isRelative();

    /**
     * Sets the audio source to relative mode.
     *
     * @param relative {@code true} to enable relative mode; {@code false} to disable it.
     */
    void setRelative(boolean relative);

    /**
     * Gets an integer parameter of the audio source.
     *
     * @param param The parameter to query.
     * @return The integer value of the parameter.
     */
    int getInt(int param);

    /**
     * Sets an integer parameter of the audio source.
     *
     * @param param The parameter to set.
     * @param value The integer value to set.
     */
    void setInt(int param, int value);

    /**
     * Gets an array of integer parameters of the audio source.
     *
     * @param param  The parameter to query.
     * @param values An array to store the values.
     */
    void getIntArray(int param, int[] values);

    /**
     * Sets an array of integer parameters of the audio source.
     *
     * @param param  The parameter to set.
     * @param values An array containing the values to set.
     */
    void setIntArray(int param, int[] values);

    /**
     * Gets a float parameter of the audio source.
     *
     * @param param The parameter to query.
     * @return The float value of the parameter.
     */
    float getFloat(int param);

    /**
     * Sets a float parameter of the audio source.
     *
     * @param param The parameter to set.
     * @param value The float value to set.
     */
    void setFloat(int param, float value);

    /**
     * Gets an array of float parameters of the audio source.
     *
     * @param param  The parameter to query.
     * @param values An array to store the values.
     */
    void getFloatArray(int param, float[] values);

    /**
     * Sets an array of float parameters of the audio source.
     *
     * @param param  The parameter to set.
     * @param values An array containing the values to set.
     */
    void setFloatArray(int param, float[] values);

    /**
     * Sets the timeout in milliseconds for the audio source to close.
     *
     * @param timeoutMs The timeout value in milliseconds.
     */
    void setCloseTimeoutMs(long timeoutMs);

    enum State {
        INITIAL(4113),
        PLAYING(4114),
        PAUSED(4115),
        STOPPED(4116);

        private final int state;

        State(int state) {
            this.state = state;
        }

        /**
         * Gets the integer value associated with the state.
         *
         * @return The integer value of the state.
         */
        public int getValue() {
            return state;
        }

        /**
         * Converts an integer value to a State enum.
         *
         * @param state The integer value to convert.
         * @return The corresponding State enum.
         */
        public static State fromInt(int state) {
            switch (state) {
                case 4114:
                    return PLAYING;
                case 4115:
                    return PAUSED;
                case 4116:
                    return STOPPED;
                default:
                    return INITIAL;
            }
        }
    }
}
