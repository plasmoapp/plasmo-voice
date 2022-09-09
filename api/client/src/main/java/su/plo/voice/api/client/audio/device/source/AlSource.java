package su.plo.voice.api.client.audio.device.source;

// todo: doc
public interface AlSource extends DeviceSource {

    long getPointer();

    void play();

    void stop();

    void pause();

    State getState();

    float getPitch();

    void setPitch(float pitch);

    float getVolume();

    void setVolume(float volume);

    boolean isRelative();

    void setRelative(boolean relative);

    int getInt(int param);

    void setInt(int param, int value);

    void getIntArray(int param, int[] values);

    void setIntArray(int param, int[] values);

    float getFloat(int param);

    void setFloat(int param, float value);

    void getFloatArray(int param, float[] values);

    void setFloatArray(int param, float[] values);

    enum State {
        INITIAL(4113),
        PLAYING(4114),
        PAUSED(4115),
        STOPPED(4116);

        private final int state;

        State(int state) {
            this.state = state;
        }

        public int getValue() {
            return state;
        }

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
