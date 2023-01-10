package su.plo.voice;

import com.google.gson.Gson;
import lombok.Getter;
import net.luckperms.api.LuckPerms;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.commands.*;
import su.plo.voice.common.packets.Packet;
import su.plo.voice.common.packets.tcp.ClientMutedPacket;
import su.plo.voice.common.packets.tcp.ClientUnmutedPacket;
import su.plo.voice.common.packets.tcp.ConfigPacket;
import su.plo.voice.common.packets.tcp.PacketTCP;
import su.plo.voice.common.packets.udp.PacketUDP;
import su.plo.voice.data.DataEntity;
import su.plo.voice.data.ServerMutedEntity;
import su.plo.voice.events.PlayerConfigEvent;
import su.plo.voice.events.PlayerVoiceMuteEvent;
import su.plo.voice.events.PlayerVoiceUnmuteEvent;
import su.plo.voice.listeners.LuckPermsListener;
import su.plo.voice.listeners.PlayerListener;
import su.plo.voice.listeners.PluginChannelListener;
import su.plo.voice.placeholders.PlaceholderPlasmoVoice;
import su.plo.voice.socket.SocketClientUDP;
import su.plo.voice.socket.SocketServerUDP;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class PlasmoVoice extends JavaPlugin implements PlasmoVoiceAPI {
    @Getter
    private static PlasmoVoice instance;
    @Getter
    private static Logger voiceLogger;

    // protocol version
    public static final String rawVersion = "1.0.0";
    public static final int version = calculateVersion(rawVersion);
    public static final String rawMinVersion = "0.0.6";
    public static final int minVersion = calculateVersion(rawMinVersion);

    public static final String downloadLink = String.format("https://github.com/plasmoapp/plasmo-voice/releases/tag/%s", rawVersion);
    private static ConcurrentHashMap<UUID, ServerMutedEntity> muted = new ConcurrentHashMap<>();
    public static Gson gson = new Gson();

    private SocketServerUDP socketServerUDP;
    @Getter
    private PlasmoVoiceConfig voiceConfig;

    private LuckPermsListener luckPermsListener;

    @Override
    public void onEnable() {
        instance = this;
        voiceLogger = super.getLogger();

        // save default config
        saveDefaultConfig();

        loadData();

        this.updateConfig();

        this.getServer().getMessenger().registerIncomingPluginChannel(this, "plasmo:voice", new PluginChannelListener());
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "plasmo:voice");

        socketServerUDP = new SocketServerUDP(voiceConfig.getIp(), voiceConfig.getPort());
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

        PluginCommand voiceUnmuteCommand = getCommand("vunmute");
        voiceUnmuteCommand.setExecutor(voiceUnmute);
        voiceUnmuteCommand.setTabCompleter(voiceUnmute);

        // Voice mute list
        getCommand("vmutelist").setExecutor(new VoiceMuteList());

        getCommand("vreload").setExecutor(new VoiceReload());
        getCommand("vreconnect").setExecutor(new VoiceReconnect());
        getCommand("vlist").setExecutor(new VoiceList());

        // Register placeholders
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderPlasmoVoice().register();
        }

        // Plugin metrics
        Metrics metrics = new Metrics(this, 10928);
        metrics.addCustomChart(new SingleLineChart("players_with_forge_mod", () ->
                (int) SocketServerUDP.clients.values().stream().filter(s -> s.getType().equals("forge")).count()));
        metrics.addCustomChart(new SingleLineChart("players_with_fabric_mod", () ->
                (int) SocketServerUDP.clients.values().stream().filter(s -> s.getType().equals("fabric")).count()));
        metrics.addCustomChart(new SimplePie("server_type", () -> "Spigot"));

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerListener.reconnectPlayer(player);

            if (player.isOp() && !SocketServerUDP.started) {
                player.sendMessage(PlasmoVoice.getInstance().getPrefix() +
                        String.format("Voice chat is installed but doesn't work. Check if port %d UDP is open.",
                                voiceConfig.getPort()));
            }
        }

        Bukkit.getServicesManager().register(PlasmoVoiceAPI.class, this, this, ServicePriority.Normal);

        // LuckPerms
        if (getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            RegisteredServiceProvider<LuckPerms> luckPermsProvider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            if (luckPermsProvider != null) {
                LuckPerms luckPerms = luckPermsProvider.getProvider();
                luckPermsListener = new LuckPermsListener(luckPerms);
            }
        }
    }

    @Override
    public void onDisable() {
        if (socketServerUDP != null) {
            socketServerUDP.close();
            socketServerUDP.interrupt();
        }

        if (luckPermsListener != null) {
            luckPermsListener.unsubscribe();
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
        } catch (NumberFormatException ignored) {
        }

        return ver;
    }

    public void updateConfig() {
        FileConfiguration config = getConfig();
        Configuration def = getConfig().getDefaults();

        if (!def.getString("config_version").equals(config.getString("config_version"))) {
            voiceLogger.warning("Config outdated. Check https://github.com/plasmoapp/plasmo-voice/wiki/How-to-install-Server for new config.");
        }

        int sampleRate = config.getInt("sample_rate");
        if (sampleRate != 8000 && sampleRate != 12000 && sampleRate != 24000 && sampleRate != 48000) {
            voiceLogger.warning("Sample rate cannot be " + sampleRate);
            return;
        }

        List<Integer> distances = config.getIntegerList("distances");
        if (distances.size() == 0) {
            voiceLogger.warning("Distances cannot be empty");
            return;
        }
        int defaultDistance = config.getInt("default_distance");
        if (defaultDistance == 0) {
            defaultDistance = distances.get(0);
        } else if (!distances.contains(defaultDistance)) {
            defaultDistance = distances.get(0);
        }

        int fadeDivisor = config.getInt("fade_divisor");
        if (fadeDivisor <= 0) {
            voiceLogger.warning("Fade distance cannot be <= 0");
            return;
        }

        int priorityFadeDivisor = config.getInt("priority_fade_divisor");
        if (priorityFadeDivisor <= 0) {
            voiceLogger.warning("Priority fade distance cannot be <= 0");
            return;
        }

        int clientModCheckTimeout = config.getInt("client_mod_check_timeout");
        if (clientModCheckTimeout < 20) {
            voiceLogger.warning("Client mod check timeout cannot be < 20 ticks");
            return;
        }

        int udpPort = config.getInt("udp.port");
        if (udpPort == 0) {
            udpPort = getServer().getPort();
        }

        this.voiceConfig = new PlasmoVoiceConfig(
                config.getString("udp.ip"),
                udpPort,
                config.getString("udp.proxy_ip"),
                config.getInt("udp.proxy_port"),
                sampleRate,
                distances,
                defaultDistance,
                config.getInt("max_priority_distance"),
                config.getBoolean("disable_voice_activation"),
                fadeDivisor,
                priorityFadeDivisor,
                config.getBoolean("client_mod_required"),
                clientModCheckTimeout
        );
    }

    private void loadData() {
        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new FileReader(this.getDataFolder().getPath() + "/data.json"));
            DataEntity data = gson.fromJson(bufferedReader, DataEntity.class);
            if (data != null) {
                for (ServerMutedEntity e : data.getMuted()) {
                    muted.put(e.getUuid(), e);
                }
            }
        } catch (FileNotFoundException ignored) {
        }
    }

    private void saveData() {
        try {
            try (Writer w = new FileWriter(this.getDataFolder().getPath() + "/data.json")) {
                List<ServerMutedEntity> list = new ArrayList<>(muted.values());
                w.write(gson.toJson(new DataEntity(list)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get message with prefix from config
    public String getMessagePrefix(String name) {
        // Get message or use default value
        String message = getConfig().getString("messages." + name);
        if (message == null) {
            message = "";
        } else {
            message = ChatColor.translateAlternateColorCodes('&', message);
        }

        return this.getPrefix() + message;
    }

    // Get message from config
    public String getMessage(String name) {
        // Get message or use default value
        String message = getConfig().getString("messages." + name);
        if (message == null) {
            message = "";
        } else {
            message = ChatColor.translateAlternateColorCodes('&', message);
        }

        return message;
    }

    // Get plugin prefix from config
    public String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.prefix"));
    }

    @Override
    public void mute(UUID playerUUID, long duration, DurationUnit durationUnit, @Nullable String reason, boolean silent) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        if (player.getFirstPlayed() == 0) {
            throw new NullPointerException("Player not found");
        }

        if (duration > 0 && durationUnit == null) {
            throw new NullPointerException("durationUnit cannot be null if duration > 0");
        }

        String durationMessage = durationUnit == null ? "" : durationUnit.format(duration);
        if (duration > 0) {
            duration = durationUnit.multiply(duration) * 1000L;
            duration += System.currentTimeMillis();
        }

        ServerMutedEntity serverMuted = new ServerMutedEntity(player.getUniqueId(), duration, reason);
        PlasmoVoice.muted.put(player.getUniqueId(), serverMuted);

        Player onlinePlayer = Bukkit.getPlayer(playerUUID);
        if (onlinePlayer != null) {
            PluginChannelListener.sendToClients(new ClientMutedPacket(serverMuted.getUuid(), serverMuted.getTo()), onlinePlayer);

            if (!silent) {
                onlinePlayer.sendMessage((duration > 0
                        ? PlasmoVoice.getInstance().getMessagePrefix("player_muted")
                        : PlasmoVoice.getInstance().getMessagePrefix("player_muted_perm"))
                        .replace("{duration}", durationMessage)
                        .replace("{reason}", reason != null
                                ? reason
                                : PlasmoVoice.getInstance().getMessage("mute_no_reason")
                        )
                );
            }
        }

        Bukkit.getPluginManager().callEvent(new PlayerVoiceMuteEvent(player, duration));
    }

    @Override
    public boolean unmute(UUID playerUUID, boolean silent) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        if (player.getFirstPlayed() == 0) {
            return false;
        }

        ServerMutedEntity muted = PlasmoVoice.muted.get(player.getUniqueId());
        if (muted == null) {
            return false;
        }

        PlasmoVoice.muted.remove(muted.getUuid());

        Player onlinePlayer = Bukkit.getPlayer(player.getUniqueId());
        if (onlinePlayer != null) {
            PluginChannelListener.sendToClients(new ClientUnmutedPacket(player.getUniqueId()), onlinePlayer);

            if (!silent) {
                onlinePlayer.sendMessage(PlasmoVoice.getInstance().getMessagePrefix("player_unmuted"));
            }
        }

        Bukkit.getPluginManager().callEvent(new PlayerVoiceUnmuteEvent(player));
        return true;
    }

    @Override
    public Set<Player> getConnectedPlayers() {
        return SocketServerUDP.clients.values().stream()
                .map(SocketClientUDP::getPlayer)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean sendVoicePacketToPlayer(Packet packet, Player recipient) {
        if (!hasVoiceChat(recipient.getUniqueId())) return false;

        byte[] bytes;
        try {
            bytes = PacketUDP.write(packet);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        try {
            SocketServerUDP.sendTo(bytes, SocketServerUDP.clients.get(recipient.getUniqueId()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean isMuted(UUID playerUUID) {
        ServerMutedEntity e = PlasmoVoice.muted.get(playerUUID);
        if (e == null) {
            return false;
        }

        if (e.getTo() == 0 || e.getTo() > System.currentTimeMillis()) {
            return true;
        } else {
            PlasmoVoice.muted.remove(e.getUuid());

            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                Bukkit.getScheduler().runTaskAsynchronously(PlasmoVoice.getInstance(), () -> {
                    PluginChannelListener.sendToClients(new ClientUnmutedPacket(e.getUuid()), player);
                });
            }

            return false;
        }
    }

    @Override
    public Map<UUID, ServerMutedEntity> getMutedMap() {
        return muted;
    }

    @Override
    public boolean hasVoiceChat(UUID player) {
        return SocketServerUDP.clients.entrySet()
                .stream()
                .anyMatch(entry -> entry.getKey().equals(player));
    }

    @Nullable
    @Override
    public String getPlayerModLoader(UUID player) {
        return SocketServerUDP.clients
                .values()
                .stream()
                .filter(c -> c.getPlayer().getUniqueId().equals(player))
                .findFirst()
                .map(SocketClientUDP::getType)
                .orElse(null);
    }

    @Override
    public List<UUID> getPlayers() {
        return SocketServerUDP.clients
                .values()
                .stream()
                .map(client -> client.getPlayer().getUniqueId())
                .collect(Collectors.toList());
    }

    @Override
    public void setVoiceDistances(UUID playerId, List<Integer> distances, Integer defaultDistance, Integer fadeDivisor) {
        if (distances.size() == 0) {
            throw new IllegalArgumentException("distances should contains at least 1 element");
        }

        if (!distances.contains(defaultDistance)) {
            throw new IllegalArgumentException("distances should contain defaultDistance");
        }

        if (fadeDivisor < 1) {
            throw new IllegalArgumentException("fadeDivisor should be >= 1");
        }

        if (!hasVoiceChat(playerId)) {
            throw new IllegalArgumentException("Player does not have Plasmo Voice installed");
        }

        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            throw new IllegalArgumentException("Player not found");
        }

        ConfigPacket packet = new ConfigPacket(
                voiceConfig.getSampleRate(),
                new ArrayList<>(distances),
                defaultDistance,
                voiceConfig.getMaxPriorityDistance(),
                voiceConfig.getFadeDivisor(),
                voiceConfig.getPriorityFadeDivisor(),
                voiceConfig.isDisableVoiceActivation() || !player.hasPermission("voice.activation")
        );

        PlayerConfigEvent event = new PlayerConfigEvent(player, packet, PlayerConfigEvent.Cause.PLUGIN);

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }

            try {
                player.sendPluginMessage(PlasmoVoice.getInstance(), "plasmo:voice", PacketTCP.write(packet));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
