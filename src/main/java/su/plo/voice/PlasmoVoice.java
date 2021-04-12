package su.plo.voice;

import com.google.gson.Gson;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import su.plo.voice.commands.*;
import su.plo.voice.common.entities.MutedEntity;
import su.plo.voice.data.DataEntity;
import su.plo.voice.listeners.PlayerListener;
import su.plo.voice.listeners.PluginChannelListener;
import su.plo.voice.socket.SocketServerUDP;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class PlasmoVoice extends JavaPlugin {
    private static PlasmoVoice instance;
    public static Logger logger;

    public static final String rawVersion = "1.0.0";
    public static final int version = calculateVersion(rawVersion);

    public static final String rawMinVersion = "0.0.6";
    public static final int minVersion = calculateVersion(rawMinVersion);

    public static final String downloadLink = String.format("https://github.com/plasmoapp/plasmo-voice/releases/tag/%s", rawVersion);
    public static ConcurrentHashMap<UUID, MutedEntity> muted = new ConcurrentHashMap<>();
    public static Gson gson = new Gson();

    private SocketServerUDP socketServerUDP;
    public PlasmoVoiceConfig config;

    public static PlasmoVoice getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();

        // save default config
        saveDefaultConfig();

        loadData();

        this.updateConfig();

        this.getServer().getMessenger().registerIncomingPluginChannel(this, "plasmo:voice", new PluginChannelListener());
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "plasmo:voice");

        socketServerUDP = new SocketServerUDP(config.ip, config.port);
        socketServerUDP.start();

        // add listener
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        // Mute command
        VoiceMute voiceMute = new VoiceMute();

        PluginCommand voiceMuteCommand = getCommand("vmute");
        voiceMuteCommand.setExecutor(voiceMute);
        voiceMuteCommand.setTabCompleter(voiceMute);

        // Unmute command
        VoiceUnmute voiceUnmute = new VoiceUnmute();

        PluginCommand voiceUnmuteCommand = getCommand("unvmute");
        voiceUnmuteCommand.setExecutor(voiceUnmute);
        voiceUnmuteCommand.setTabCompleter(voiceUnmute);

        getCommand("vreload").setExecutor(new VoiceReload());
        getCommand("vreconnect").setExecutor(new VoiceReconnect());
        getCommand("vlist").setExecutor(new VoiceList());

        // Plugin metrics
        Metrics metrics = new Metrics(this, 10928);
        metrics.addCustomChart(new SingleLineChart("players_with_forge_mod", () ->
                (int) SocketServerUDP.clients.values().stream().filter(s -> s.getType().equals("forge")).count()));
        metrics.addCustomChart(new SingleLineChart("players_with_fabric_mod", () ->
                (int) SocketServerUDP.clients.values().stream().filter(s -> s.getType().equals("fabric")).count()));

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerListener.reconnectPlayer(player);

            if(player.isOp() && !SocketServerUDP.started) {
                player.sendMessage(PlasmoVoice.getInstance().getPrefix() +
                        String.format("Voice chat is installed but doesn't work. Check if port %d UDP is open.",
                                PlasmoVoice.getInstance().config.port));
            }
        }
    }

    @Override
    public void onDisable() {
        if(socketServerUDP != null) {
            socketServerUDP.close();
            socketServerUDP.interrupt();
        }

        saveData();
    }

    public static int calculateVersion(String s) {
        int ver = 0;
        String[] version = s.split("\\.");
        try {
            ver += Integer.parseInt(version[0]) * 1000;
            ver += Integer.parseInt(version[1]) * 100;
            ver += Integer.parseInt(version[2]);
        } catch (NumberFormatException ignored) {}

        return ver;
    }

    public void updateConfig() {
        FileConfiguration config = getConfig();
        Configuration def = getConfig().getDefaults();


        if(!def.getString("config_version").equals(config.getString("config_version"))) {
            PlasmoVoice.logger.warning("Config outdated. Check https://github.com/plasmoapp/plasmo-voice/wiki/How-to-install-Server for new config.");
        }

        int sampleRate = config.getInt("sample_rate");
        if (sampleRate != 8000 && sampleRate != 12000 && sampleRate != 24000 && sampleRate != 48000) {
            PlasmoVoice.logger.warning("Sample rate cannot be " + sampleRate);
            return;
        }

        List<Integer> distances = config.getIntegerList("distances");
        if(distances.size() == 0) {
            PlasmoVoice.logger.warning("Distances cannot be empty");
            return;
        }
        int defaultDistance = config.getInt("default_distance");
        if(defaultDistance == 0) {
            defaultDistance = distances.get(0);
        } else if(!distances.contains(defaultDistance)) {
            defaultDistance = distances.get(0);
        }

        int fadeDivisor = config.getInt("fade_divisor");
        if(fadeDivisor <= 0) {
            PlasmoVoice.logger.warning("Fade distance cannot be <= 0");
            return;
        }

        int priorityFadeDivisor = config.getInt("priority_fade_divisor");
        if(priorityFadeDivisor <= 0) {
            PlasmoVoice.logger.warning("Priority fade distance cannot be <= 0");
            return;
        }

        this.config = new PlasmoVoiceConfig(config.getString("udp.ip"),
                config.getInt("udp.port"),
                sampleRate,
                distances,
                defaultDistance,
                config.getInt("max_priority_distance"),
                config.getBoolean("disable_voice_activation"),
                fadeDivisor,
                priorityFadeDivisor);
    }

    private void loadData() {
        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new FileReader(this.getDataFolder().getPath() + "/data.json"));
            DataEntity data = gson.fromJson(bufferedReader, DataEntity.class);
            if(data != null) {
                for(MutedEntity e : data.muted) {
                    muted.put(e.uuid, e);
                }
            }
        } catch (FileNotFoundException ignored) {}
    }

    private void saveData() {
        try {
            try(Writer w = new FileWriter(this.getDataFolder().getPath() + "/data.json")) {
                List<MutedEntity> list = new ArrayList<>();
                muted.forEach((ignored, m) -> {
                    list.add(m);
                });

                w.write(gson.toJson(new DataEntity(list)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get message with prefix from config
    public String getMessagePrefix(String name) {
        String prefix = this.getPrefix();

        // Get message or use default value
        String message = this.getConfig().getString("messages." + name);
        if(message == null) {
            message = "";
        } else {
            message = ChatColor.translateAlternateColorCodes('&', message);
        }

        return prefix + message;
    }

    // Get plugin prefix from config
    public String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.prefix"));
    }
}
