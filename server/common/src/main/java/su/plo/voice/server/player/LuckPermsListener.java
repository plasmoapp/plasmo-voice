package su.plo.voice.server.player;

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
import su.plo.voice.api.server.player.PlayerManager;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.server.event.player.PlayerPermissionUpdateEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public final class LuckPermsListener {

    private final PlasmoVoiceServer voiceServer;
    private final PlayerManager players;

    private final LuckPerms luckPerms = LuckPermsProvider.get();
    private final List<EventSubscription<?>> subscriptions = new ArrayList<>();

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
        Collection<String> synchronizedPermissions = players.getSynchronizedPermissions();
        if (synchronizedPermissions.isEmpty()) return;

        if (event.isUser()) {
            User user = (User) event.getTarget();
            Optional<VoicePlayer> player = players.getPlayerById(user.getUniqueId());
            if (!player.isPresent() || !player.get().hasVoiceChat()) return;

            if (synchronizedPermissions.contains(node.getKey())) {
                onChanged(player.get(), node.getKey());
            }
        } else if (event.isGroup()) {
            Group group = (Group) event.getTarget();

            // iterate all online players and check if they are in group
            if (synchronizedPermissions.contains(node.getKey())) {
                for (VoicePlayer player : players.getPlayers()) {
                    if (player.hasPermission("group." + group.getName())) {
                        onChanged(player, node.getKey());
                    }
                }
            }
        }
    }

    private void onChanged(@NotNull VoicePlayer player, @NotNull String permission) {
        voiceServer.getEventBus().call(new PlayerPermissionUpdateEvent(player, permission));
    }
}
