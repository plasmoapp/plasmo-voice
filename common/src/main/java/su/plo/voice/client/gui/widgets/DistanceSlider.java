package su.plo.voice.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.config.ClientConfig;

import java.util.Comparator;

public class DistanceSlider extends AbstractSliderButton {
    private final int maxSteps;

    public DistanceSlider(int x, int y, int width) {
        super(x, y, width, 20, Component.empty(),
                (float) VoiceClient.getServerConfig().getDistances().indexOf((int) VoiceClient.getServerConfig().getDistance())
                        / (float) (VoiceClient.getServerConfig().getDistances().size() - 1));
        this.updateMessage();
        this.maxSteps = VoiceClient.getServerConfig().getDistances().size();
    }

    public void updateValue() {
        this.value = (float) VoiceClient.getServerConfig().getDistances().indexOf((int) VoiceClient.getServerConfig().getDistance())
                / (float) (VoiceClient.getServerConfig().getDistances().size() - 1);
        this.updateMessage();
    }

    private double adjust(double value) {
        return Mth.clamp(value, VoiceClient.getServerConfig().getMinDistance(), VoiceClient.getServerConfig().getMaxDistance());
    }

    public int getValue(double ratio) {
        double value = this.adjust(Mth.lerp(
                Mth.clamp(ratio, 0.0D, 1.0D),
                VoiceClient.getServerConfig().getMinDistance(),
                VoiceClient.getServerConfig().getMaxDistance()
        ));

        return VoiceClient.getServerConfig().getDistances().stream()
                .min(Comparator.comparingInt(i -> Math.abs(i - (int) value))).orElseGet(() -> (int) VoiceClient.getServerConfig().getMinDistance());
    }

    protected void updateMessage() {
        this.setMessage(Component.literal(String.valueOf(this.getValue(this.value))));
    }

    protected void applyValue() {
        int value = this.getValue(this.value);
        ClientConfig.ServerConfig serverConfig;
        if (VoiceClient.getClientConfig().getServers().containsKey(VoiceClient.getServerConfig().getIp())) {
            serverConfig = VoiceClient.getClientConfig().getServers().get(VoiceClient.getServerConfig().getIp());
            serverConfig.distance.set(value);
            VoiceClient.getServerConfig().setDistance((short) value);
        }
    }

    @Override
    protected void renderBg(PoseStack matrices, Minecraft client, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (this.isHoveredOrFocused() ? 2 : 1) * 20;

        float stepValue = (float) VoiceClient.getServerConfig().getDistances().indexOf((int) VoiceClient.getServerConfig().getDistance()) / ((float) maxSteps - 1);

        blit(matrices, this.x + (int) (stepValue * (double) (this.width - 8)), this.y, 0, 46 + i, 4, 20);
        blit(matrices, this.x + (int) (stepValue * (double) (this.width - 8)) + 4, this.y, 196, 46 + i, 4, 20);
    }
}
