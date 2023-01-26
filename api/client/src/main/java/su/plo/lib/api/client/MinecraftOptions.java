package su.plo.lib.api.client;

public interface MinecraftOptions {

    boolean isGuiHidden();

    int getRenderDistance();

    float getBackgroundOpacity(float m);

    int getBackgroundColor(int m);

    String getLanguageCode();
}
