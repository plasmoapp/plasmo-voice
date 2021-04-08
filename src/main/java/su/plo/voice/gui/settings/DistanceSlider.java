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
                (float) VoiceClient.serverConfig.distances.indexOf((int) VoiceClient.serverConfig.distance)
                        / (float) (VoiceClient.serverConfig.distances.size() - 1));
        this.updateMessage();
    }

    private double adjust(double value) {
        return MathHelper.clamp(value, VoiceClient.serverConfig.minDistance, VoiceClient.serverConfig.maxDistance);
    }

    public int getValue(double ratio) {
        double value = this.adjust(MathHelper.lerp(MathHelper.clamp(ratio, 0.0D, 1.0D), VoiceClient.serverConfig.minDistance, VoiceClient.serverConfig.maxDistance));

        return VoiceClient.serverConfig.distances.stream()
                .min(Comparator.comparingInt(i -> Math.abs(i - (int) value))).orElseGet(() -> (int) VoiceClient.serverConfig.minDistance);
    }

    protected void updateMessage() {
        this.setMessage((new TranslatableText("gui.plasmo_voice.distance", this.getValue(this.value))));
    }

    protected void applyValue() {
        int value = this.getValue(this.value);
        VoiceClientServerConfig serverConfig;
        if(VoiceClient.config.servers.containsKey(VoiceClient.serverConfig.ip)) {
            serverConfig = VoiceClient.config.servers.get(VoiceClient.serverConfig.ip);
        } else {
            serverConfig = new VoiceClientServerConfig();
        }

        serverConfig.distance = (short) value;
        VoiceClient.serverConfig.distance = (short) value;
        VoiceClient.config.servers.put(VoiceClient.serverConfig.ip, serverConfig);
    }
}