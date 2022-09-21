package su.plo.voice.client.audio.source;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.AlAudioDevice;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.client.audio.device.source.DeviceSource;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.audio.source.DirectSourceInfo;

public final class ModClientDirectSource extends ModClientAudioSource<DirectSourceInfo> {

    public ModClientDirectSource(@NotNull PlasmoVoiceClient voiceClient, ClientConfig config) {
        super(voiceClient, config);
    }

    @Override
    public void initialize(DirectSourceInfo sourceInfo) throws DeviceException {
        super.initialize(sourceInfo);
        updateSourceParams();
    }

    @Override
    public void updateInfo(DirectSourceInfo sourceInfo) {
        if (getInfo().isCameraRelative() != sourceInfo.isCameraRelative()) {
            updateSourceParams();
        }

        super.updateInfo(sourceInfo);
    }

    @Override
    protected float[] getPosition(float[] position) {
        if (sourceInfo.getRelativePosition() != null) {
            if (sourceInfo.isCameraRelative()) {
                position[0] = (float) sourceInfo.getRelativePosition().getX();
                position[1] = (float) sourceInfo.getRelativePosition().getY();
                position[2] = (float) sourceInfo.getRelativePosition().getZ();
            } else {
                position = getAbsoluteSourcePosition(position);
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

    private float[] getAbsoluteSourcePosition(float[] position) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || sourceInfo.getRelativePosition() == null) return position;

        position[0] = (float) (player.getX() + sourceInfo.getRelativePosition().getX());
        position[1] = (float) (player.getEyeY() + sourceInfo.getRelativePosition().getY());
        position[2] = (float) (player.getZ() + sourceInfo.getRelativePosition().getZ());

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
