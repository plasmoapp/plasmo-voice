package su.plo.voice.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponent;

public class BackgroundScreen extends Screen {
    protected final ResourceLocation TEXTURE;
    protected final boolean disableBackground;
    protected int guiLeft;
    protected int guiTop;
    protected int xSize;
    protected int ySize;

    protected int headerHeight = 10;
    protected int footerHeight = 10;

    public BackgroundScreen(TextComponent title, int width, int height, ResourceLocation TEXTURE, boolean disableBackground) {
        super(title);
        this.minecraft = Minecraft.getInstance();
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

            RenderSystem.color4f(1F, 1F, 1F, 1F);
            minecraft.getTextureManager().bind(TEXTURE);

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
