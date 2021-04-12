package su.plo.voice.gui.settings;

import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import su.plo.voice.Voice;
import su.plo.voice.VoiceClientServerConfig;
import su.plo.voice.VoiceServerConfig;

import java.util.Comparator;

public class DistanceSlider extends AbstractSlider {
    public DistanceSlider(int x, int y, int width) {
        super(x, y, width, 20, StringTextComponent.EMPTY,
                (float) Voice.serverConfig.distances.indexOf((int) Voice.serverConfig.distance)
                        / (float) (Voice.serverConfig.distances.size() - 1));
        this.updateMessage();
    }

    private double adjust(double value) {
        return MathHelper.clamp(value, Voice.serverConfig.minDistance, Voice.serverConfig.maxDistance);
    }

    public int getValue(double ratio) {
        double value = this.adjust(MathHelper.lerp(MathHelper.clamp(ratio, 0.0D, 1.0D), Voice.serverConfig.minDistance, Voice.serverConfig.maxDistance));

        return Voice.serverConfig.distances.stream()
                .min(Comparator.comparingInt(i -> Math.abs(i - (int) value))).orElseGet(() -> (int) Voice.serverConfig.minDistance);
    }

    protected void updateMessage() {
        this.setMessage((new TranslationTextComponent("gui.plasmo_voice.distance", this.getValue(this.value))));
    }

    protected void applyValue() {
        int value = this.getValue(this.value);
        VoiceClientServerConfig serverConfig;
        if(Voice.config.servers.containsKey(Voice.serverConfig.ip)) {
            serverConfig = Voice.config.servers.get(Voice.serverConfig.ip);
        } else {
            serverConfig = new VoiceClientServerConfig();
        }

        serverConfig.distance = (short) value;
        Voice.serverConfig.distance = (short) value;
        Voice.config.servers.put(Voice.serverConfig.ip, serverConfig);
    }
}