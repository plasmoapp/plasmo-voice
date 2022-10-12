package su.plo.voice.server.player;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeClearEvent;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.player.VoicePlayerManager;
import su.plo.voice.server.event.player.PlayerPermissionUpdateEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public final class LuckPermsListener {

    private final PlasmoVoiceServer voiceServer;
    private final VoicePlayerManager players;
    private final ScheduledExecutorService executor;

    private final LuckPerms luckPerms = LuckPermsProvider.get();
    private final List<EventSubscription<?>> subscriptions = new ArrayList<>();

    private final Map<String, ScheduledFuture<?>> permissionChanges = Maps.newHashMap();

    public void subscribe() {
        EventBus bus = luckPerms.getEventBus();

        EventSubscription<?> subscription = bus.subscribe(NodeAddEvent.class, this::onNodeAdd);
        subscriptions.add(subscription);

        subscription = bus.subscribe(NodeRemoveEvent.class, this::onNodeRemove);
        subscriptions.add(subscription);

        subscription = bus.subscribe(NodeClearEvent.class, this::onNodeClear);
        subscriptions.add(subscription);
    }

    public void unsubscribe() {
        subscriptions.forEach(EventSubscription::close);
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
            Optional<VoicePlayer> player = players.getPlayerById(user.getUniqueId());
            if (!player.isPresent() || !player.get().hasVoiceChat()) return;

            onLpPermissionChange(player.get(), node.getKey());
        } else if (event.isGroup()) {
            Group group = (Group) event.getTarget();

            // iterate all online players and check if they are in group
            for (VoicePlayer player : players.getPlayers()) {
                if (player.getInstance().hasPermission("group." + group.getName())) {
                    onLpPermissionChange(player, node.getKey());
                }
            }
        }
    }

    private synchronized void onLpPermissionChange(@NotNull VoicePlayer player, @NotNull String permission) {
        String playerPermissionKey = player.getInstance().getUUID() + "_" + permission;

        ScheduledFuture<?> future = permissionChanges.get(playerPermissionKey);
        if (future != null) future.cancel(false);

        permissionChanges.put(
                player.getInstance().getUUID() + "_" + permission,
                executor.schedule(
                        () -> onPermissionChange(player, permission),
                        100L,
                        TimeUnit.MILLISECONDS
                )
        );
    }

    private void onPermissionChange(@NotNull VoicePlayer player, @NotNull String permission) {
        System.out.println(permission);
        voiceServer.getEventBus().call(new PlayerPermissionUpdateEvent(player, permission));
        permissionChanges.remove(player.getInstance().getUUID() + "_" + permission);
    }
}
