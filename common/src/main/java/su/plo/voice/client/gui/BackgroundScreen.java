package su.plo.voice.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

// todo удалить нахуй
public class BackgroundScreen extends Screen {
    protected final ResourceLocation TEXTURE;
    protected final boolean disableBackground;
    protected int guiLeft;
    protected int guiTop;
    protected int xSize;
    protected int ySize;

    protected int headerHeight = 10;
    protected int footerHeight = 10;

    public BackgroundScreen(Component title, int width, int height, ResourceLocation TEXTURE, boolean disableBackground) {
        super(title);
        this.xSize = width;
        this.ySize = height;
        this.TEXTURE = TEXTURE;
        this.disableBackground = disableBackground;
    }

    public void setHeight(int height) {
        this.ySize = height;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (width - this.xSize) / 2;
        this.guiTop = (height - this.ySize) / 2;
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if(!disableBackground) {
            this.renderBackground(matrices);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            RenderSystem.setShaderTexture(0, TEXTURE);

            // header
            blit(matrices, guiLeft, guiTop, 0, 0, xSize, headerHeight, 512, 512);

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
                blit(matrices, guiLeft, guiTop + y, 0, headerHeight, xSize, h, 512, 512);
            }

            // footer
            blit(matrices, guiLeft, guiTop + ySize - footerHeight, 0, 190, xSize, footerHeight, 512, 512);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }
}
