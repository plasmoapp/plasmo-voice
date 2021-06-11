package su.plo.voice.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BackgroundScreen extends Screen {
    protected final Identifier TEXTURE;
    protected final boolean disableBackground;
    protected int guiLeft;
    protected int guiTop;
    protected int xSize;
    protected int ySize;

    protected int headerHeight = 10;
    protected int footerHeight = 10;

    public BackgroundScreen(Text title, int width, int height, Identifier TEXTURE, boolean disableBackground) {
        super(title);
        this.client = MinecraftClient.getInstance();
        this.xSize = width;
        this.ySize = height;
        this.TEXTURE = TEXTURE;
        this.disableBackground = disableBackground;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (width - this.xSize) / 2;
        this.guiTop = (height - this.ySize) / 2;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if(!disableBackground) {
            this.renderBackground(matrices);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            RenderSystem.setShaderTexture(0, TEXTURE);

            // header
            drawTexture(matrices, guiLeft, guiTop, 0, 0, xSize, headerHeight, 512, 512);

            // main
            for(int y = 10; y < ySize - (headerHeight + footerHeight); y += 180) {
                int h = 180;
                if(h > ySize) {
                    h = ySize - (headerHeight + footerHeight);
                } else if(h == ySize) {
                    h = h - (headerHeight + footerHeight);
                } else if(y + h > ySize) {
                    h = ySize - (headerHeight + footerHeight) - h;
                }
                drawTexture(matrices, guiLeft, guiTop + y, 0, headerHeight, xSize, h, 512, 512);
            }

            // footer
            drawTexture(matrices, guiLeft, guiTop + ySize - footerHeight, 0, 190, xSize, footerHeight, 512, 512);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }
}
