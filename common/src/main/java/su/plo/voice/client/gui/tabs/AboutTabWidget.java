package su.plo.voice.client.gui.tabs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.opengl.GL11;
import su.plo.voice.client.gui.VoiceSettingsScreen;

import java.io.IOException;
import java.util.*;

public class AboutTabWidget extends TabWidget {
    private static final Map<String, List<Translator>> translators = ImmutableMap.of(
            "ja_jp", ImmutableList.of(
                    new Translator(new GameProfile(UUID.fromString("a2c18c8f-538d-4ea1-bdee-464dd9df3c52"), "sya_ri"), "GitHub", "https://github.com/sya-ri")
            ),
            "it_it", ImmutableList.of(
                    new Translator(new GameProfile(UUID.fromString("717a29b8-7476-4d29-ba64-b9c157b9bf83"), "Yriokiri"), "GitHub", "https://github.com/Yriokiri")
            ),
            "nl_nl", ImmutableList.of(
                    new Translator(new GameProfile(UUID.fromString("db7dce6f-3f20-4c35-b8ca-83b51658ac92"), "EEstar"), "GitHub", "https://github.com/emielderckx")
            ),
            "uk_ua", ImmutableList.of(
                    new Translator(new GameProfile(UUID.fromString("b149c762-6da3-41b4-8db7-0e87330fb9aa"), "_ProSplash_"), "GitHub", "https://github.com/Yuraplonka")
            )
    );
    private final VoiceSettingsScreen parent;

    public AboutTabWidget(Minecraft client, VoiceSettingsScreen parent) {
        super(client, parent);
        this.parent = parent;

        Language language = Language.getInstance();

        Component madeBy = new TranslatableComponent("gui.plasmo_voice.about.made_by", "Plasmo Voice");
        if (!language.getOrDefault(((TranslatableComponent) madeBy).getKey()).contains("%s")) {
            madeBy = new TextComponent("Plasmo Voice is made by");
        }
        this.addEntry(new CategoryEntry(this, madeBy, 24));
        this.addEntry(new DeveloperEntry(this, "KPidS", new TranslatableComponent("gui.plasmo_voice.about.huix"), "Twitch", "https://twitch.tv/kpids"));
        this.addEntry(new DeveloperEntry(this, "Apehum", new TranslatableComponent("gui.plasmo_voice.about.programming"), "Twitch", "https://twitch.tv/apehum"));
        this.addEntry(new DeveloperEntry(this, "Venterok", new TranslatableComponent("gui.plasmo_voice.about.artist"), "Twitter", "https://twitter.com/venterrok"));

        LanguageInfo languageInfo = client.getLanguageManager().getSelected();
        List<Translator> langTranslators = translators.get(languageInfo.getCode());
        if (langTranslators != null) {
            for (Translator translator : langTranslators) {
                DeveloperEntry.loadTranslatorSkin(translator);
                this.addEntry(new DeveloperEntry(
                        this,
                        translator.getProfile().getName(),
                        new TranslatableComponent("gui.plasmo_voice.about.translator", languageInfo.getName()),
                        translator.getLink(),
                        translator.getLinkUrl()
                ));
            }
        }
        Component links = new TranslatableComponent("gui.plasmo_voice.about.links", "Plasmo Voice");
        if (!language.getOrDefault(((TranslatableComponent) links).getKey()).contains("%s")) {
            links = new TextComponent("Plasmo Voice on");
        }
        this.addEntry(new CategoryEntry(this, links));
        this.addEntry(new ListEntry(ImmutableList.of(
                new Button(0, 0, 0, 20, new TextComponent("Github"), button -> {
                    openLink("https://github.com/plasmoapp/plasmo-voice");
                }, (button, matrices, mouseX, mouseY) -> {
                    setTooltip(ImmutableList.of(new TextComponent("https://github.com/plasmoapp/plasmo-voice")));
                }),
                new Button(0, 0, 0, 20, new TextComponent("Discord"), button -> {
                    openLink("https://discord.com/invite/uueEqzwCJJ");
                }, (button, matrices, mouseX, mouseY) -> {
                    setTooltip(ImmutableList.of(new TextComponent("https://discord.com/invite/uueEqzwCJJ")));
                })
        )));
        this.addEntry(new ListEntry(ImmutableList.of(
                new Button(0, 0, 0, 20, new TextComponent("CurseForge"), button -> {
                    openLink("https://www.curseforge.com/minecraft/mc-mods/plasmo-voice-client");
                }, (button, matrices, mouseX, mouseY) -> {
                    setTooltip(ImmutableList.of(new TextComponent("https://www.curseforge.com/minecraft/mc-mods/plasmo-voice-client")));
                }),
                new Button(0, 0, 0, 20, new TextComponent("Spigot"), button -> {
                    openLink("https://www.spigotmc.org/resources/plasmo-voice-server.91064/");
                }, (button, matrices, mouseX, mouseY) -> {
                    setTooltip(ImmutableList.of(new TextComponent("https://www.spigotmc.org/resources/plasmo-voice-server.91064/")));
                })
        )));
        this.addEntry(new TextEntry(new TranslatableComponent("gui.plasmo_voice.about.copyright")));
    }

    protected void openLink(String linkUrl) {
        AboutTabWidget.this.minecraft.setScreen(new ConfirmLinkScreen(ok -> AboutTabWidget.this.confirmLink(ok, linkUrl), linkUrl, true));
    }

    private void confirmLink(boolean ok, String linkUrl) {
        if (ok) {
            Util.getPlatform().openUri(linkUrl);
        }

        AboutTabWidget.this.minecraft.setScreen(AboutTabWidget.this.parent);
    }

    @Data
    @AllArgsConstructor
    static class Translator {
        private GameProfile profile;
        private String link;
        private String linkUrl;
    }

    static class ListEntry extends Entry {
        private final List<AbstractWidget> list;

        public ListEntry(List<AbstractWidget> list) {
            super(24);
            this.list = list;
        }

        public List<? extends GuiEventListener> children() {
            return list;
        }

        @Override
        public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int gap = 4;
            int elementWidth = entryWidth / list.size() - ((list.size() - 1) * (gap / 2));
            if (elementWidth % 2 == 1) {
                elementWidth += 1;
                gap = (entryWidth - (elementWidth * list.size())) / (list.size() - 1);

            }
            int elementX = x;

            for (AbstractWidget element : list) {
                element.x = elementX;
                element.y = y;
                element.setWidth(elementWidth);

                element.render(matrices, mouseX, mouseY, tickDelta);

                elementX += elementWidth + gap;
            }
        }
    }

    class TextEntry extends Entry {
        final Component text;

        public TextEntry(Component text) {
            super(28);
            this.text = text;
        }

        public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            List<FormattedCharSequence> lines = AboutTabWidget.this.minecraft.font.split(text, entryWidth);
            int lineHeight = AboutTabWidget.this.minecraft.font.lineHeight;
            this.setHeight((lines.size() * AboutTabWidget.this.minecraft.font.lineHeight) + 8);

            int elementY = y + 16;

            for(Iterator<FormattedCharSequence> var7 = lines.iterator(); var7.hasNext(); elementY += lineHeight) {
                FormattedCharSequence orderedText = var7.next();
                AboutTabWidget.this.minecraft.font.drawShadow(matrices, orderedText, x, (float)(elementY - lineHeight - 1), -8355712);
            }
        }

        public boolean changeFocus(boolean lookForwards) {
            return false;
        }

        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }
    }

    public static class DeveloperEntry extends Entry {
        private static final ResourceLocation steveSkin = new ResourceLocation("textures/entity/steve.png");
        public static Map<String, ResourceLocation> skins = new HashMap<>();

        public static void loadTranslatorSkin(Translator translator) {
            if (!skins.containsKey(translator.getProfile().getName())) {
                RenderSystem.recordRenderCall(() -> {
                    loadSkin(
                            translator.getProfile(),
                            null
                    );
                });
            }
        }

        public static void loadSkins() {
            RenderSystem.recordRenderCall(() -> {
                loadSkin(
                        new GameProfile(UUID.fromString("8f552657-df1d-42cd-89c6-c176e195f703"), "Apehum"),
                        "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"
                );
                loadSkin(
                        new GameProfile(UUID.fromString("2714d55f-ffef-4655-a93e-d8ca13230e76"), "KPidS"),
                        "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"
                );
                loadSkin(
                        new GameProfile(UUID.fromString("cfb727e7-efcc-4596-8c2b-9c6e38c8eea4"), "Venterok"),
                        "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"
                );
            });
        }

        private static void loadSkin(GameProfile profile, String fallback) {
            if (skins.containsKey(profile.getName())) {
                return;
            }

            if (fallback != null) {
                ResourceLocation fallbackIdentifier = new ResourceLocation("plasmo_voice", "skins/" + Hashing.sha1().hashUnencodedChars(profile.getName().toLowerCase()).toString());
                try {
                    Minecraft.getInstance().getTextureManager().register(
                            fallbackIdentifier,
                            new DynamicTexture(NativeImage.fromBase64(fallback))
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // fallback
                skins.put(profile.getName(), fallbackIdentifier);
            }

            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = Minecraft.getInstance().getSkinManager().getInsecureSkinInformation(profile);
            if (textures == null || textures.isEmpty()) {
                Minecraft.getInstance().getSkinManager().registerSkins(
                        profile,
                        (type, identifier, texture) -> {
                            if (type.equals(MinecraftProfileTexture.Type.SKIN)) {
                                skins.put(profile.getName(), identifier);
                            }
                        }, false);
            } else {
                String hash = Hashing.sha1().hashUnencodedChars(textures.get(MinecraftProfileTexture.Type.SKIN).getHash()).toString();
                ResourceLocation identifier = new ResourceLocation("skins/" + hash);
                skins.put(profile.getName(), identifier);
            }
        }

        private final TextComponent nick;
        private final Component role;
        private final Button link;
        private final Minecraft client = Minecraft.getInstance();

        public DeveloperEntry(AboutTabWidget parent, String nick, Component role, String link, String linkUrl) {
            super(44);
            this.nick = new TextComponent(nick);
            this.role = role;
            this.link = new Button(0, 0, 56, 20, new TextComponent(link), button -> {
                parent.openLink(linkUrl);
            }, (button, matrices, mouseX, mouseY) -> {
                parent.setTooltip(ImmutableList.of(
                        new TextComponent(linkUrl)
                ));
            });
        }

        public void renderBackground(int y, int x, int entryWidth, int entryHeight) {
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuilder();
            client.getTextureManager().bind(BACKGROUND_LOCATION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferBuilder.vertex(x, y + entryHeight, 0.0D).uv(0.0F, (float) entryHeight / 32.0F + 0)
                    .color(40, 40, 40, 255).endVertex();
            bufferBuilder.vertex(x + entryWidth, y + entryHeight, 0.0D).uv((float) entryWidth / 32.0F, (float) entryHeight / 32.0F + 0)
                    .color(40, 40, 40, 255).endVertex();
            bufferBuilder.vertex(x + entryWidth, y, 0.0D).uv((float) entryWidth / 32.0F, 0)
                    .color(40, 40, 40, 255).endVertex();
            bufferBuilder.vertex(x, y, 0.0D).uv(0.0F, 0)
                    .color(40, 40, 40, 255).endVertex();
            tessellator.end();
        }

        public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            renderBackground(y, x, entryWidth, entryHeight);

            client.getTextureManager().bind(skins.getOrDefault(nick.getContents(), steveSkin));
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            blit(matrices, x + 4, y + 4, 32, 32, 8.0F, 8.0F, 8, 8, 64, 64);
            blit(matrices, x + 4, y + 4, 32, 32, 40.0F, 8.0F, 8, 8, 64, 64);

            client.font.drawShadow(matrices, nick, x + 40, y + 11, 16777215);
            client.font.drawShadow(matrices, role, x + 40, y + 21, -5592406);

            link.x = x + entryWidth - 62;
            link.y = y + 10;
            link.render(matrices, mouseX, mouseY, tickDelta);
        }

        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.link);
        }
    }
}
