package su.plo.voice.server.audio.capture;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.PlasmoVoice;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.addon.AddonManager;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.api.server.audio.capture.ServerActivationManager;
import su.plo.voice.api.server.connection.ConnectionManager;
import su.plo.voice.api.server.event.audio.capture.ServerActivationRegisterEvent;
import su.plo.voice.api.server.event.audio.capture.ServerActivationUnregisterEvent;
import su.plo.voice.api.server.event.player.PlayerPermissionUpdateEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.player.VoicePlayerManager;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;
import su.plo.voice.proto.packets.tcp.clientbound.ActivationRegisterPacket;
import su.plo.voice.proto.packets.tcp.clientbound.ActivationUnregisterPacket;
import su.plo.voice.proto.packets.tcp.clientbound.ClientPacketTcpHandler;
import su.plo.voice.server.player.BaseVoicePlayer;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VoiceServerActivationManager implements ServerActivationManager {

    private static final String WILDCARD_ACTIVATION_PERMISSION = "pv.activation.*";

    private final PlasmoVoice voice;
    private final ConnectionManager<ClientPacketTcpHandler, ? extends VoicePlayer<?>> tcpConnections;
    private final AddonManager addons;
    private final VoicePlayerManager<?> players;
    private final Map<UUID, ServerActivation> activationById = Maps.newConcurrentMap();

    public VoiceServerActivationManager(@NotNull PlasmoVoice voice,
                                        @NotNull ConnectionManager<ClientPacketTcpHandler, ? extends VoicePlayer<?>> tcpConnections,
                                        @NotNull VoicePlayerManager<?> players) {
        this.voice = voice;
        this.tcpConnections = tcpConnections;
        this.addons = voice.getAddonManager();
        this.players = players;
    }

    @Override
    public Optional<ServerActivation> getActivationById(@NotNull UUID id) {
        return Optional.ofNullable(activationById.get(id));
    }

    @Override
    public Optional<ServerActivation> getActivationByName(@NotNull String name) {
        return Optional.ofNullable(activationById.get(VoiceActivation.generateId(name)));
    }

    @Override
    public Collection<ServerActivation> getActivations() {
        return activationById.values();
    }

    @Override
    public @NotNull ServerActivation.Builder createBuilder(@NotNull Object addonObject,
                                                           @NotNull String name,
                                                           @NotNull String translation,
                                                           @NotNull String icon,
                                                           @NotNull String permission,
                                                           int weight) {
        Optional<AddonContainer> addon = addons.getAddon(addonObject);
        if (!addon.isPresent()) throw new IllegalArgumentException("addonObject is not an addon");

        VoiceServerActivation activation = (VoiceServerActivation) activationById.get(VoiceActivation.generateId(name));
        if (activation != null) throw new IllegalStateException("Activation with name " + name + " already exists");

        return new VoiceServerActivationBuilder(addon.get(), name, translation, icon, permission, weight);
    }

    @Override
    public boolean unregister(@NotNull UUID id) {
        ServerActivation activation = activationById.get(id);
        if (activation != null) {
            ServerActivationUnregisterEvent event = new ServerActivationUnregisterEvent(activation);
            voice.getEventBus().call(event);
            if (event.isCancelled()) return false;

            activationById.remove(id);

            players.getPlayers()
                    .stream()
                    .filter(VoicePlayer::hasVoiceChat)
                    .forEach((player) -> ((BaseVoicePlayer<?>) player).removeActivationDistance(activation));

            tcpConnections.broadcast(
                    new ActivationUnregisterPacket(activation.getId()),
                    (player) -> player.getInstance().hasPermission(activation.getPermission())
            );

            return true;
        }

        return false;
    }

    @Override
    public boolean unregister(@NotNull String name) {
        return unregister(VoiceActivation.generateId(name));
    }

    @Override
    public boolean unregister(@NotNull ServerActivation activation) {
        return unregister(activation.getId());
    }

    @Override
    public void clear() {
        activationById.values().forEach(this::unregister);
        activationById.clear();
    }

    @EventSubscribe
    public void onPermissionUpdate(@NotNull PlayerPermissionUpdateEvent event) {
        VoicePlayer<?> player = event.getPlayer();
        String permission = event.getPermission();

        if (activationById.values()
                .stream()
                .noneMatch((activation) -> activation.getPermission().equals(permission))
        ) return;

        if (permission.equals(WILDCARD_ACTIVATION_PERMISSION)) {
            switch (player.getInstance().getPermission(WILDCARD_ACTIVATION_PERMISSION)) {
                case TRUE:
                    activationById.values().forEach((activation) ->
                            player.sendPacket(new ActivationRegisterPacket((VoiceActivation) activation))
                    );
                    break;
                case FALSE:
                    activationById.keySet().forEach((activationId) ->
                            player.sendPacket(new ActivationUnregisterPacket(activationId))
                    );
                    break;
                case UNDEFINED:
                    activationById.forEach((activationId, activation) -> {
                        if (player.getInstance().hasPermission(activation.getPermission())) {
                            player.sendPacket(new ActivationRegisterPacket((VoiceActivation) activation));
                        } else {
                            player.sendPacket(new ActivationUnregisterPacket(activationId));
                        }
                    });
                    break;
            }
            return;
        }

        String[] permissionSplit = permission.split("\\.");
        getActivationByName(permissionSplit[permissionSplit.length - 1]).ifPresent((activation) -> {
            if (player.getInstance().hasPermission(activation.getPermission())) {
                player.sendPacket(new ActivationRegisterPacket((VoiceActivation) activation));
            } else {
                player.sendPacket(new ActivationUnregisterPacket(activation.getId()));
            }
        });
    }

    @RequiredArgsConstructor
    class VoiceServerActivationBuilder implements ServerActivation.Builder {

        private final @NotNull AddonContainer addon;
        private final @NotNull String name;
        private final @NotNull String translation;
        private final @NotNull String icon;
        private final @NotNull String permission;
        private final int weight;

        private List<Integer> distances = Collections.emptyList();
        private int defaultDistance;
        private boolean transitive = true;
        private boolean proximity = true;
        private boolean stereoSupported = false;

        @Override
        public @NotNull ServerActivation.Builder setDistances(@NotNull List<Integer> distances) {
            this.distances = checkNotNull(distances);
            return this;
        }

        @Override
        public @NotNull ServerActivation.Builder setDefaultDistance(int defaultDistance) {
            this.defaultDistance = defaultDistance;
            return this;
        }

        @Override
        public @NotNull ServerActivation.Builder setTransitive(boolean transitive) {
            this.transitive = transitive;
            return this;
        }

        @Override
        public @NotNull ServerActivation.Builder setProximity(boolean proximity) {
            this.proximity = proximity;
            return this;
        }

        @Override
        public @NotNull ServerActivation.Builder setStereoSupported(boolean stereoSupported) {
            this.stereoSupported = stereoSupported;
            return this;
        }

        @Override
        public @NotNull ServerActivation build() {
            if (activationById.get(VoiceActivation.generateId(name)) != null)
                throw new IllegalStateException("Activation with name " + name + " already exists");

            VoiceServerActivation activation = new VoiceServerActivation(
                    addon,
                    name,
                    translation,
                    icon,
                    permission,
                    distances,
                    defaultDistance,
                    proximity,
                    transitive,
                    stereoSupported,
                    weight
            );

            ServerActivationRegisterEvent event = new ServerActivationRegisterEvent(activation);
            if (!voice.getEventBus().call(event))
                throw new IllegalStateException("Activation registration was cancelled");

            activationById.put(activation.getId(), activation);
            tcpConnections.broadcast(
                    new ActivationRegisterPacket(activation),
                    (player) -> player.getInstance().hasPermission(activation.getPermission())
            );

            return activation;
        }
    }
}
