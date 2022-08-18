package su.plo.voice.client.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.source.StaticSourceInfo;

public final class ClientStaticSource extends BaseClientAudioSource<StaticSourceInfo> {

    public ClientStaticSource(@NotNull PlasmoVoiceClient voiceClient, ClientConfig config) {
        super(voiceClient, config);
    }

    @Override
    protected float[] getPosition(float[] position) {
        position[0] = (float) sourceInfo.getPosition().getX();
        position[1] = (float) sourceInfo.getPosition().getY();
        position[2] = (float) sourceInfo.getPosition().getZ();

        return position;
    }

    @Override
    protected float[] getLookAngle(float[] lookAngle) {
        position[0] = (float) sourceInfo.getPosition().getX();
        position[1] = (float) sourceInfo.getPosition().getY();
        position[2] = (float) sourceInfo.getPosition().getZ();

        return position;
    }
}
