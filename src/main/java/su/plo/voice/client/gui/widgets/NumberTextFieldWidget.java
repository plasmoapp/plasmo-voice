package su.plo.voice.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public class NumberTextFieldWidget extends EditBox {
    private final Pattern pattern = Pattern.compile("[0-9]+$");

    public NumberTextFieldWidget(Font textRenderer, int x, int y, int width, int height, String text,
                                 int min, int max, int defaultValue, Consumer<Integer> onChange) {
        super(textRenderer, x, y, width - 3, height - 2, null);
        if (onChange != null) {
            this.setResponder(s -> {
                if (s.isEmpty()) {
                    onChange.accept(defaultValue);
                } else {
                    try {
                        onChange.accept(Math.max(min + 1, Integer.parseInt(s)));
                    } catch (NumberFormatException ignored) {
                        onChange.accept(defaultValue);
                    }
                }
            });
        }
        this.setFilter(s -> {
            if (s.isEmpty()) {
                return true;
            }

            if (!pattern.matcher(s).matches()) {
                return false;
            }

            int i = Integer.parseInt(s);
            return i > 0 && i <= max;
        });
        this.setValue(text);
    }

    @Override
    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.x += 1;
        this.y += 1;
        matrices.pushPose();
        RenderSystem.enableDepthTest();
        matrices.translate(0.0D, 0.0D, 0.0D);
        super.renderButton(matrices, mouseX, mouseY, delta);
        matrices.popPose();
    }
}
