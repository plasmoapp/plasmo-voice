package su.plo.voice.server;

import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import su.plo.voice.common.packets.tcp.ClientUnmutedPacket;
import su.plo.voice.server.config.Configuration;
import su.plo.voice.server.config.ServerMuted;
import su.plo.voice.server.network.ServerNetworkHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {
    @Getter
    private final ConcurrentHashMap<UUID, Map<String, Boolean>> permissions = new ConcurrentHashMap<>();

    public synchronized boolean hasPermission(UUID player, String permission) {
        Map<String, Boolean> perms = permissions.get(player);
        if (perms != null) {
            return perms.getOrDefault(permission, hasDefaultPermission(player, permission));
        } else {
            return hasDefaultPermission(player, permission);
        }
    }

    public synchronized void setPermission(UUID uuid, String permission, boolean value) {
        if (hasDefaultPermission(uuid, permission) == value) {
            unSetPermission(uuid, permission);
            return;
        }

        if (permissions.containsKey(uuid)) {
            Map<String, Boolean> perms = permissions.get(uuid);
            perms.put(permission, value);
        } else {
            Map<String, Boolean> perms = new HashMap<>();
            perms.put(permission, value);
            permissions.put(uuid, perms);
        }

        VoiceServer.saveData(true);
    }

    public synchronized void unSetPermission(UUID uuid, String permission) {
        if (permissions.containsKey(uuid)) {
            Map<String, Boolean> perms = permissions.get(uuid);
            perms.remove(permission);
        }

        VoiceServer.saveData(true);
    }

    public boolean hasDefaultPermission(UUID player, String permission) {
        String defaultPermission = getDefaultPermission(permission);
        if (defaultPermission.equals("op")) {
            return isOp(player);
        } else {
            return true;
        }
    }

    private String getDefaultPermission(String permission) {
        Configuration section = VoiceServer.getInstance().getConfig().getSection("permissions");
        Object obj = section.getSelf().get(permission);
        if (obj == null) {
            return "";
        }

        return obj instanceof String ? (String) obj : "";
    }

    public boolean isMuted(UUID uuid) {
        ServerMuted muted = VoiceServer.getMuted().get(uuid);
        if (muted != null) {
            if (muted.getTo() == 0 || muted.getTo() > System.currentTimeMillis()) {
                return true;
            } else {
                VoiceServer.getMuted().remove(uuid);
                ServerNetworkHandler.sendToClients(new ClientUnmutedPacket(uuid), null);
            }
        }

        return false;
    }

    public static ServerPlayer getByUUID(UUID uuid) {
        return VoiceServer.getServer().getPlayerList().getPlayer(uuid);
    }

    public static boolean isOp(UUID player) {
        return VoiceServer.getServer().getPlayerList().isOp(VoiceServer.getServer().getProfileCache().get(player));
    }

    public static boolean isOp(ServerPlayer player) {
        return VoiceServer.getServer().getPlayerList().isOp(player.getGameProfile());
    }
}
