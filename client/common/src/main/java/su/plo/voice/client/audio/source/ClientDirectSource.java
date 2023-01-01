package su.plo.voice.client.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.AlAudioDevice;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.client.audio.device.source.DeviceSource;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.audio.source.DirectSourceInfo;
import su.plo.voice.proto.data.pos.Pos3d;

public final class ClientDirectSource extends BaseClientAudioSource<DirectSourceInfo> {

    public ClientDirectSource(@NotNull MinecraftClientLib minecraft,
                              @NotNull PlasmoVoiceClient voiceClient,
                              @NotNull ClientConfig config,
                              @NotNull SoundOcclusionSupplier soundOcclusionSupplier) {
        super(minecraft, voiceClient, config, soundOcclusionSupplier);
    }

    @Override
    public void initialize(DirectSourceInfo sourceInfo) throws DeviceException {
        super.initialize(sourceInfo);
        updateSourceParams();
    }

    @Override
    protected float[] getPosition(float[] position) {
        if (sourceInfo.getRelativePosition() != null) {
            if (sourceInfo.isCameraRelative()) {
                position[0] = (float) sourceInfo.getRelativePosition().getX();
                position[1] = (float) sourceInfo.getRelativePosition().getY();
                position[2] = (float) sourceInfo.getRelativePosition().getZ();
            } else {
                return getAbsoluteSourcePosition(position);
            }
        } else {
            position[0] = 0F;
            position[1] = 0F;
            position[2] = 0F;
        }

        return position;
    }

    @Override
    protected float[] getLookAngle(float[] lookAngle) {
        // todo: lookAngle?
        lookAngle[0] = 0F;
        lookAngle[1] = 0F;
        lookAngle[2] = 0F;

        return lookAngle;
    }

    @Override
    protected boolean shouldCalculateOcclusion() {
        return false; // todo: relative position occlusion
    }

    private float[] getAbsoluteSourcePosition(float[] position) {
        if (sourceInfo.getRelativePosition() == null) return position;

        minecraft.getClientPlayer().ifPresent((player) -> {
            Pos3d playerPosition = player.getPosition();

            position[0] = (float) (playerPosition.getX() + sourceInfo.getRelativePosition().getX());
            position[1] = (float) (playerPosition.getY() + player.getEyeHeight() + sourceInfo.getRelativePosition().getY());
            position[2] = (float) (playerPosition.getZ() + sourceInfo.getRelativePosition().getZ());
        });

        return position;
    }

    private void updateSourceParams() {
        for (DeviceSource source : sourceGroup.getSources()) {
            if (source instanceof AlSource) {
                AlSource alSource = (AlSource) source;
                AlAudioDevice device = (AlAudioDevice) alSource.getDevice();

                device.runInContext(() -> {
                    alSource.setInt(
                            0x202, // AL_SOURCE_RELATIVE
                            sourceInfo.isCameraRelative() ? 1 : 0
                    );
                });
            }
        }
    }
}
