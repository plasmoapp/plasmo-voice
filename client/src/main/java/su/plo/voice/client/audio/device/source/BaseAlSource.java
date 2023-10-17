package su.plo.voice.client.audio.device.source;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import su.plo.slib.api.position.Pos3d;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.AlContextAudioDevice;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.client.event.audio.device.source.AlSourcePauseEvent;
import su.plo.voice.api.client.event.audio.device.source.AlSourcePlayEvent;
import su.plo.voice.api.client.event.audio.device.source.AlSourceStopEvent;
import su.plo.voice.api.client.event.audio.device.source.AlSourceUpdateParamEvent;
import su.plo.voice.client.audio.AlUtil;
import su.plo.voice.client.audio.device.AlOutputDevice;

import java.util.Arrays;

public abstract class BaseAlSource implements AlSource {

    protected final PlasmoVoiceClient client;
    protected final AlOutputDevice device;
    protected final int format;
    protected int pointer;

    private Pos3d position;

    protected BaseAlSource(PlasmoVoiceClient client, AlOutputDevice device, boolean stereo, int pointer) {
        this.client = client;
        this.device = device;
        this.pointer = pointer;
        this.format = stereo ? AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_MONO16;
    }

    @Override
    public boolean isClosed() {
        return pointer == 0L;
    }

    @Override
    public @NotNull AlContextAudioDevice getDevice() {
        return device;
    }

    @Override
    public long getPointer() {
        return pointer;
    }

    @Override
    public void play() {
        AlUtil.checkDeviceContext(device);

        AlSourcePlayEvent event = new AlSourcePlayEvent(this);
        client.getEventBus().fire(event);
        if (event.isCancelled()) return;

        AL11.alSourcePlay(pointer);
        AlUtil.checkErrors("Source pause");
    }

    @Override
    public void stop() {
        AlUtil.checkDeviceContext(device);

        AlSourceStopEvent event = new AlSourceStopEvent(this);
        client.getEventBus().fire(event);
        if (event.isCancelled()) return;

        AL11.alSourceStop(pointer);
        AlUtil.checkErrors("Source pause");
    }

    @Override
    public void pause() {
        AlUtil.checkDeviceContext(device);

        AlSourcePauseEvent event = new AlSourcePauseEvent(this);
        client.getEventBus().fire(event);
        if (event.isCancelled()) return;

        AL11.alSourcePause(pointer);
        AlUtil.checkErrors("Source pause");
    }

    @Override
    public State getState() {
        return State.fromInt(getInt(AL11.AL_SOURCE_STATE));
    }

    @Override
    public float getPitch() {
        return getFloat(AL11.AL_PITCH);
    }

    @Override
    public void setPitch(float pitch) {
        setFloat(AL11.AL_PITCH, pitch);
    }

    @Override
    public float getVolume() {
        return getFloat(AL11.AL_GAIN);
    }

    @Override
    public void setVolume(float volume) {
        AlUtil.checkDeviceContext(device);
        setFloat(AL11.AL_GAIN, volume);
    }

    @Override
    public boolean isRelative() {
        AlUtil.checkDeviceContext(device);
        return getInt(AL11.AL_SOURCE_RELATIVE) == 1;
    }

    @Override
    public void setRelative(boolean relative) {
        AlUtil.checkDeviceContext(device);
        setInt(AL11.AL_SOURCE_RELATIVE, relative ? 1 : 0);
    }

    @Override
    public int getInt(int param) {
        AlUtil.checkDeviceContext(device);
        if (isClosed()) return -1;

        int value = AL11.alGetSourcei(pointer, param);
        AlUtil.checkErrors("Get source int " + param);

        return value;
    }

    @Override
    public void setInt(int param, int value) {
        AlUtil.checkDeviceContext(device);
        if (isClosed()) return;

        if (!callParamEvent(param, value)) return;
        AL11.alSourcei(pointer, param, value);

        AlUtil.checkErrors("Set source int " + param + ": " + value);
    }

    @Override
    public void getIntArray(int param, int[] values) {
        AlUtil.checkDeviceContext(device);
        if (isClosed()) return;

        AL11.alGetSourceiv(pointer, param, values);
        AlUtil.checkErrors("Get source int[] " + param);
    }

    @Override
    public void setIntArray(int param, int[] values) {
        AlUtil.checkDeviceContext(device);
        if (isClosed()) return;

        if (!callParamEvent(param, values)) return;
        AL11.alSourceiv(pointer, param, values);
        AlUtil.checkErrors("Set source int[] " + param + ": " + Arrays.toString(values));
    }

    @Override
    public float getFloat(int param) {
        AlUtil.checkDeviceContext(device);
        if (isClosed()) return -1F;

        float value = AL11.alGetSourcei(pointer, param);
        AlUtil.checkErrors("Get source float " + param);

        return value;
    }

    @Override
    public void setFloat(int param, float value) {
        AlUtil.checkDeviceContext(device);
        if (isClosed()) return;

        if (!callParamEvent(param, value)) return;
        AL11.alSourcef(pointer, param, value);
        AlUtil.checkErrors("Set source float " + param + ": " + value);
    }

    @Override
    public void getFloatArray(int param, float[] values) {
        AlUtil.checkDeviceContext(device);
        if (isClosed()) return;

        AL11.alGetSourcefv(pointer, param, values);
        AlUtil.checkErrors("Get source float[] " + param);
    }

    @Override
    public void setFloatArray(int param, float[] values) {
        AlUtil.checkDeviceContext(device);
        if (isClosed()) return;

        if (!callParamEvent(param, values)) return;
        AL11.alSourcefv(pointer, param, values);
        AlUtil.checkErrors("Set source float[] " + param + ": " + Arrays.toString(values));
    }

    private boolean callParamEvent(int param, Object value) {
        AlSourceUpdateParamEvent event = new AlSourceUpdateParamEvent(this, param, value);
        return client.getEventBus().fire(event);
    }
}
