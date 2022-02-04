package su.plo.voice.listeners;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeClearEvent;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import su.plo.voice.PlasmoVoice;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class LuckPermsListener {
    private final LinkedList<UUID> toUpdate = new LinkedList<>();
    private final List<EventSubscription<?>> subscriptions = new ArrayList<>();
    private final BukkitTask ticker;

    public LuckPermsListener(LuckPerms luckPerms) {
        EventBus bus = luckPerms.getEventBus();

        EventSubscription<?> subscription = bus.subscribe(NodeAddEvent.class, this::onNodeAdd);
        subscriptions.add(subscription);

        subscription = bus.subscribe(NodeRemoveEvent.class, this::onNodeRemove);
        subscriptions.add(subscription);

        subscription = bus.subscribe(NodeClearEvent.class, this::onNodeClear);
        subscriptions.add(subscription);

        this.ticker = Bukkit.getScheduler().runTaskTimerAsynchronously(PlasmoVoice.getInstance(), this::tick, 0L, 60L);
    }

    private synchronized void tick() {
        while (!toUpdate.isEmpty()) {
            UUID playerId = toUpdate.poll();
            Player player = Bukkit.getPlayer(playerId);
            if (player == null || !PlasmoVoice.getInstance().hasVoiceChat(playerId)) {
                continue;
            }

            PlayerListener.reconnectPlayer(player);
        }
    }

    public void unsubscribe() {
        ticker.cancel();

        for (EventSubscription<?> subscription : subscriptions) {
            subscription.close();
        }
    }

    private void onNodeAdd(NodeAddEvent event) {
        onNodeMutate(event, event.getNode());
    }

    private void onNodeRemove(NodeRemoveEvent event) {
        onNodeMutate(event, event.getNode());
    }

    private void onNodeClear(NodeClearEvent event) {
        for (Node node : event.getNodes()) {
            onNodeMutate(event, node);
        }
    }

    private void onNodeMutate(NodeMutateEvent event, Node node) {
        if (event.isUser()) {
            User user = (User) event.getTarget();
            Player player = Bukkit.getPlayer(user.getUniqueId());
            if (player == null || !PlasmoVoice.getInstance().hasVoiceChat(user.getUniqueId())) {
                return;
            }

            if (node.getKey().equals("voice.speak") ||
                    node.getKey().equals("voice.priority") ||
                    node.getKey().equals("voice.activation")) {
                onChanged(player);
            }
        } else if (event.isGroup()) {
            Group group = (Group) event.getTarget();

            // iterate all online players and check if they are in group
            if (node.getKey().equals("voice.speak") ||
                    node.getKey().equals("voice.priority") ||
                    node.getKey().equals("voice.activation")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("group." + group.getName())) {
                        onChanged(player);
                    }
                }
            }
        }
    }

    private synchronized void onChanged(Player player) {
        if (!toUpdate.contains(player.getUniqueId())) {
            toUpdate.offer(player.getUniqueId());
        }
    }
}
