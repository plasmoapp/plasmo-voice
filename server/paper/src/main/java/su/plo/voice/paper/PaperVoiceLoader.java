package su.plo.voice.paper;

import org.bukkit.plugin.java.JavaPlugin;

public final class PaperVoiceLoader extends JavaPlugin {

    private final PaperVoiceServer voiceServer = new PaperVoiceServer(this);

    @Override
    public void onLoad() {
        voiceServer.onLoad();
    }

    @Override
    public void onEnable() {
        voiceServer.onInitialize();
    }

    @Override
    public void onDisable() {
        voiceServer.onShutdown();
    }
}
