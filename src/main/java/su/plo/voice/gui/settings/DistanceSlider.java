package su.plo.voice.gui.settings;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.VoiceClientServerConfig;

import java.util.Comparator;

public class DistanceSlider extends SliderWidget {
    public DistanceSlider(int x, int y, int width) {
        super(x, y, width, 20, LiteralText.EMPTY,
                (float) VoiceClient.getServerConfig().getDistances().indexOf((int) VoiceClient.getServerConfig().getDistance())
                        / (float) (VoiceClient.getServerConfig().getDistances().size() - 1));
        this.updateMessage();
    }

    private double adjust(double value) {
        return MathHelper.clamp(value, VoiceClient.getServerConfig().getMinDistance(), VoiceClient.getServerConfig().getMaxDistance());
    }

    public int getValue(double ratio) {
        double value = this.adjust(MathHelper.lerp(
                MathHelper.clamp(ratio, 0.0D, 1.0D),
                VoiceClient.getServerConfig().getMinDistance(),
                VoiceClient.getServerConfig().getMaxDistance()
        ));

        return VoiceClient.getServerConfig().getDistances().stream()
                .min(Comparator.comparingInt(i -> Math.abs(i - (int) value))).orElseGet(() -> (int) VoiceClient.getServerConfig().getMinDistance());
    }

    protected void updateMessage() {
        this.setMessage((new TranslatableText("gui.plasmo_voice.distance", this.getValue(this.value))));
    }

    protected void applyValue() {
        int value = this.getValue(this.value);
        VoiceClientServerConfig serverConfig;
        if(VoiceClient.getClientConfig().getServers().containsKey(VoiceClient.getServerConfig().getIp())) {
            serverConfig = VoiceClient.getClientConfig().getServers().get(VoiceClient.getServerConfig().getIp());
        } else {
            serverConfig = new VoiceClientServerConfig();
        }

        serverConfig.setDistance((short) value);
        VoiceClient.getServerConfig().setDistance((short) value);
        VoiceClient.getClientConfig().getServers().put(VoiceClient.getServerConfig().getIp(), serverConfig);
    }
}