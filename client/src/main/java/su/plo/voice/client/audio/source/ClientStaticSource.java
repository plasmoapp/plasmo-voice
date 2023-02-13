package su.plo.voice.client.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.audio.source.StaticSourceInfo;

public final class ClientStaticSource extends BaseClientAudioSource<StaticSourceInfo> {

    public ClientStaticSource(@NotNull PlasmoVoiceClient voiceClient,
                              @NotNull ClientConfig config) {
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
        lookAngle[0] = (float) sourceInfo.getLookAngle().getX();
        lookAngle[1] = (float) sourceInfo.getLookAngle().getY();
        lookAngle[2] = (float) sourceInfo.getLookAngle().getZ();

        return lookAngle;
    }
}
