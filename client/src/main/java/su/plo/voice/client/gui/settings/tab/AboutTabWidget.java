package su.plo.voice.client.gui.settings.tab;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import su.plo.lib.mod.client.MinecraftUtil;
import su.plo.lib.mod.client.render.Colors;
import su.plo.lib.mod.client.render.gui.GuiRenderContext;
import su.plo.slib.api.chat.component.McTextComponent;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.gui.components.Button;
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget;
import su.plo.lib.mod.client.gui.widget.GuiWidgetListener;
import su.plo.lib.mod.client.language.LanguageUtil;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.lib.mod.client.render.texture.ModPlayerSkins;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.meta.PlasmoVoiceMeta;
import su.plo.voice.client.meta.developer.Developer;
import su.plo.voice.client.meta.Patron;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

//#if MC>=12005
//$$ import net.minecraft.client.Minecraft;
//#endif

public final class AboutTabWidget extends TabWidget {

    public AboutTabWidget(@NotNull VoiceSettingsScreen parent,
                          @NotNull PlasmoVoiceClient voiceClient,
                          @NotNull VoiceClientConfig config) {
        super(parent, voiceClient, config);
    }

    @Override
    public void init() {
        super.init();
        loadSkins();

        addEntry(new CategoryEntry(madeBy(), 24));
        PlasmoVoiceMeta.Companion.getMETA().getDevelopers()
                .forEach(developer -> addEntry(new DeveloperEntry(developer)));

        addEntry(new CategoryEntry(links(), 24));
        addEntry(new ListEntry(ImmutableList.of(
                new Button(0, 0, 0, 20, McTextComponent.literal("Github"), button -> {
                    openLink("https://github.com/plasmoapp/plasmo-voice");
                }, (button, matrices, mouseX, mouseY) -> {
                    setTooltip(McTextComponent.literal("https://github.com/plasmoapp/plasmo-voice"));
                }),
                new Button(0, 0, 0, 20, McTextComponent.literal("Discord"), button -> {
                    openLink("https://discord.com/invite/uueEqzwCJJ");
                }, (button, matrices, mouseX, mouseY) -> {
                    setTooltip(McTextComponent.literal("https://discord.com/invite/uueEqzwCJJ"));
                })
        )));
        this.addEntry(new ListEntry(ImmutableList.of(
                new Button(0, 0, 0, 20, McTextComponent.literal("Modrinth"), button -> {
                    openLink("https://modrinth.com/mod/plasmo-voice");
                }, (button, matrices, mouseX, mouseY) -> {
                    setTooltip(McTextComponent.literal("https://modrinth.com/mod/plasmo-voice"));
                }),
                new Button(0, 0, 0, 20, McTextComponent.literal("Spigot"), button -> {
                    openLink("https://www.spigotmc.org/resources/plasmo-voice-server.91064/");
                }, (button, matrices, mouseX, mouseY) -> {
                    setTooltip(McTextComponent.literal("https://www.spigotmc.org/resources/plasmo-voice-server.91064/"));
                })
        )));
        this.addEntry(new ListEntry(ImmutableList.of(
                new Button(0, 0, 0, 20, McTextComponent.literal("Patreon"), button -> {
                    openLink("https://www.patreon.com/plasmomc");
                }, (button, matrices, mouseX, mouseY) -> {
                    setTooltip(McTextComponent.literal("https://www.patreon.com/plasmomc"));
                }),
                new Button(0, 0, 0, 20, McTextComponent.literal("Boosty"), button -> {
                    openLink("https://boosty.to/plasmo");
                }, (button, matrices, mouseX, mouseY) -> {
                    setTooltip(McTextComponent.literal("https://boosty.to/plasmo"));
                })
        )));
        this.addEntry(new TextEntry(McTextComponent.translatable("gui.plasmovoice.about.copyright")));

        if (PlasmoVoiceMeta.Companion.getMETA().getPatrons().size() > 0) {
            addEntry(new CategoryEntry(McTextComponent.translatable("gui.plasmovoice.about.support"), 24));
            PlasmoVoiceMeta.Companion.getMETA().getPatrons()
                    .forEach(patron -> addEntry(new PatronEntry(patron)));
        }
    }

    private McTextComponent madeBy() {
        McTextComponent madeBy = McTextComponent.translatable("gui.plasmovoice.about.made_by", "Plasmo Voice");
        if (!LanguageUtil.getOrDefault("gui.plasmovoice.about.made_by").contains("%s")) {
            madeBy = McTextComponent.literal("Plasmo Voice is made by");
        }

        return madeBy;
    }

    private McTextComponent links() {
        McTextComponent links = McTextComponent.translatable("gui.plasmovoice.about.links", "Plasmo Voice");
        if (!LanguageUtil.getOrDefault("gui.plasmovoice.about.links").contains("%s")) {
            links = McTextComponent.literal("Plasmo Voice on");
        }

        return links;
    }

    private void openLink(@NotNull String link) {
        MinecraftUtil.openUri(link);
    }

    private void loadSkins() {
        PlasmoVoiceMeta.Companion.getMETA().getDevelopers().forEach(developer ->
                ModPlayerSkins.loadSkin(
                        developer.getUuid(),
                        developer.getName(),
                        voiceClient.getBackgroundExecutor()
                )
        );

        PlasmoVoiceMeta.Companion.getMETA().getPatrons().forEach(patron ->
                ModPlayerSkins.loadSkin(
                        patron.getUuid(),
                        patron.getName(),
                        voiceClient.getBackgroundExecutor()
                )
        );
    }

    class DeveloperEntry extends Entry {

        private final Developer developer;
        private final Button link;

        public DeveloperEntry(@NotNull Developer developer) {
            super(44);

            this.developer = developer;
            this.link = new Button(
                    0,
                    0,
                    56,
                    20,
                    McTextComponent.literal(developer.getSocialLinkName()),
                    (button) -> openLink(developer.getSocialLinkUrl()),
                    (button, matrices, mouseX, mouseY) -> {
                        setTooltip(McTextComponent.literal(developer.getSocialLinkUrl()));
                    });
        }

        @Override
        public void render(@NotNull GuiRenderContext context, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta) {
            renderBackground(context, x, y, entryWidth);

            ResourceLocation skinLocation = ModPlayerSkins.getSkin(developer.getUuid(), developer.getName());

            context.blit(skinLocation, x + 4, y + 4, 32, 32, 8.0F, 8.0F, 8, 8, 64, 64);
            context.blit(skinLocation, x + 4, y + 4, 32, 32, 40.0F, 8.0F, 8, 8, 64, 64);

            context.drawString(developer.getName(), x + 40, y + 11, Colors.WHITE);
            context.drawString(developer.getRole().getTranslatable(), x + 40, y + 21, new Color(0xAAAAAA));

            if (link != null) {
                link.setX(x + entryWidth - 62);
                link.setY(y + 10);
                link.render(context, mouseX, mouseY, delta);
            }
        }

        public void renderBackground(@NotNull GuiRenderContext context, int x, int y, int entryWidth) {
            int height = this.height - 4;

            //#if MC>=12005
            //$$ context.blit(
            //$$         Minecraft.getInstance().level == null ? MENU_LIST_BACKGROUND_LOCATION : INWORLD_MENU_LIST_BACKGROUND_LOCATION,
            //$$         x,
            //$$         y,
            //$$         0.0F,
            //$$         0.0F,
            //$$         entryWidth,
            //$$         height,
            //$$         32,
            //$$         32
            //$$ );
            //#else
            context.blitColor(
                    BACKGROUND_LOCATION,
                    x,
                    y,
                    0.0F,
                    0.0F,
                    entryWidth,
                    height,
                    16,
                    16,
                    new Color(64, 64, 64)
            );
            //#endif
        }

        @Override
        public List<? extends GuiWidgetListener> widgets() {
            return ImmutableList.of(link);
        }
    }

    class PatronEntry extends Entry {

        private final Patron patron;

        public PatronEntry(@NotNull Patron patron) {
            super(22);

            this.patron = patron;
        }

        @Override
        public void render(@NotNull GuiRenderContext context, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta) {
            renderBackground(context, x, y, entryWidth);

            ResourceLocation skinLocation = ModPlayerSkins.getSkin(patron.getUuid(), patron.getName());

            context.blit(skinLocation, x + 2, y + 2, 16, 16, 8.0F, 8.0F, 8, 8, 64, 64);
            context.blit(skinLocation, x + 2, y + 2, 16, 16, 40.0F, 8.0F, 8, 8, 64, 64);

            context.drawString(patron.getName(), x + 26, y + 6, Colors.WHITE);

            McTextComponent tier = McTextComponent.literal(patron.getTier());
            context.drawString(
                    tier,
                    x + entryWidth - RenderUtil.getTextWidth(tier) - 6,
                    y + 6,
                    new Color(0xAAAAAA)
            );
        }

        public void renderBackground(@NotNull GuiRenderContext context, int x, int y, int entryWidth) {
            int height = this.height - 2;

            //#if MC>=12005
            //$$ context.blit(
            //$$         Minecraft.getInstance().level == null ? MENU_LIST_BACKGROUND_LOCATION : INWORLD_MENU_LIST_BACKGROUND_LOCATION,
            //$$         x,
            //$$         y,
            //$$         0.0F,
            //$$         0.0F,
            //$$         entryWidth,
            //$$         height,
            //$$         32,
            //$$         32
            //$$ );
            //#else
            context.blitColor(
                    BACKGROUND_LOCATION,
                    x,
                    y,
                    0.0F,
                    0.0F,
                    entryWidth,
                    height,
                    16,
                    16,
                    new Color(0x404040)
            );
            //#endif
        }

        @Override
        public List<? extends GuiWidgetListener> widgets() {
            return Collections.emptyList();
        }
    }

    class ListEntry extends Entry {

        private final List<GuiAbstractWidget> widgets;

        public ListEntry(List<GuiAbstractWidget> widgets) {
            super(24);

            this.widgets = widgets;
        }

        @Override
        public List<? extends GuiWidgetListener> widgets() {
            return widgets;
        }

        @Override
        public void updatePosition(@NotNull GuiRenderContext context, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta) {
            int gap = 4;
            int elementWidth = entryWidth / widgets.size() - ((widgets.size() - 1) * (gap / 2));
            if (elementWidth % 2 == 1) {
                elementWidth += 1;
                gap = (entryWidth - (elementWidth * widgets.size())) / (widgets.size() - 1);

            }
            int elementX = x;

            for (GuiAbstractWidget element : widgets) {
                element.setX(elementX);
                element.setY(y);
                element.setWidth(elementWidth);

                elementX += elementWidth + gap;
            }
        }

        @Override
        public void render(@NotNull GuiRenderContext context, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta) {
            for (GuiAbstractWidget element : widgets) {
                element.render(context, mouseX, mouseY, delta);
            }
        }
    }

    class TextEntry extends Entry {

        private final McTextComponent text;

        public TextEntry(@NotNull McTextComponent text) {
            super(28);

            this.text = text;
        }

        @Override
        public void render(@NotNull GuiRenderContext context, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta) {
            int lines = context.drawStringMultiLine(
                    text,
                    x,
                    y,
                    new Color(0x808080),
                    entryWidth
            );

            setHeight(lines * RenderUtil.getFontHeight() + 10);
        }

        @Override
        public boolean changeFocus(boolean lookForwards) {
            return false;
        }
    }
}
