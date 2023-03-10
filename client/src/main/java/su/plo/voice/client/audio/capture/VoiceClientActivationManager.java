package su.plo.voice.client.audio.capture;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.IntConfigEntry;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.api.client.audio.capture.ClientActivationManager;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.client.event.audio.capture.ClientActivationRegisteredEvent;
import su.plo.voice.api.client.event.audio.capture.ClientActivationUnregisteredEvent;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.config.capture.ConfigClientActivation;
import su.plo.voice.client.render.voice.VoiceIconUtil;
import su.plo.voice.proto.data.audio.capture.Activation;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerActivationDistancesPacket;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
public final class VoiceClientActivationManager implements ClientActivationManager {

    private static final Logger LOGGER = LogManager.getLogger();

    private final PlasmoVoiceClient voiceClient;
    private final ClientConfig config;

    private ClientActivation parentActivation;

    private final List<ClientActivation> activations = new CopyOnWriteArrayList<>();
    private final Map<UUID, ClientActivation> activationById = Maps.newConcurrentMap();

    private boolean initialized = false;

    @Override
    public Optional<ClientActivation> getParentActivation() {
        return Optional.ofNullable(parentActivation);
    }

    @Override
    public @NotNull ClientActivation register(@NotNull ClientActivation activation) {
        unregister(activation.getId());

        int index;
        for (index = 0; index < activations.size(); index++) {
            ClientActivation act = activations.get(index);
            if (activation.getWeight() >= act.getWeight()) break;
        }

        activations.add(index, activation);
        activationById.put(activation.getId(), activation);

        if (activation.getId().equals(VoiceActivation.PROXIMITY_ID)) {
            ConfigClientActivation activationConfig = config.getActivations().getActivation(activation.getId(), activation);

            if (activationConfig.getConfigType().value() == ClientActivation.Type.INHERIT) {
                LOGGER.warn("Proximity activation type cannot be INHERIT. Changed to PUSH_TO_TALK");
                activationConfig.getConfigType().set(ClientActivation.Type.PUSH_TO_TALK);
            }

            this.parentActivation = activation;
            if (!initialized &&
                    config.getAdvanced().getVisualizeVoiceDistance().value() &&
                    config.getAdvanced().getVisualizeVoiceDistanceOnJoin().value()) {
                voiceClient.getDistanceVisualizer().render(
                        activation.getDistance(),
                        0x00a000,
                        null
                );
            }
        }

        voiceClient.getEventBus().call(new ClientActivationRegisteredEvent(activation));
        return activation;
    }

    @Override
    public @NotNull Collection<ClientActivation> register(@NotNull Collection<Activation> activations) {
        ClientConfig.Server serverConfig = getServerConfig();

        for (Activation serverActivation : activations) {
            ConfigClientActivation activationConfig = config.getActivations().getActivation(serverActivation.getId(), serverActivation);
            IntConfigEntry activationDistance = serverConfig.getActivationDistance(serverActivation.getId(), serverActivation);
            activationDistance.setDefault(
                    serverActivation.getDefaultDistance(),
                    serverActivation.getMinDistance(),
                    serverActivation.getMaxDistance()
            );

            String icon = VoiceIconUtil.INSTANCE.getIcon(
                    serverActivation.getIcon(),
                    new ResourceLocation("plasmovoice:textures/addons/activations/" + serverActivation.getName())
            );

            ClientActivation activation = register(new VoiceClientActivation(
                    voiceClient,
                    config,
                    activationConfig,
                    activationDistance,
                    serverActivation,
                    icon
            ));

            voiceClient.getServerConnection().ifPresent((connection) -> connection.sendPacket(
                    new PlayerActivationDistancesPacket(new HashMap<UUID, Integer>() {{
                        put(activation.getId(), activation.getDistance());
                    }})
            ));
        }

        if (parentActivation == null) {
            this.parentActivation = createParentActivation(serverConfig);
        }

        this.initialized = true;
        return getActivations();
    }

    @Override
    public Optional<ClientActivation> getActivationById(@NotNull UUID id) {
        return Optional.ofNullable(activationById.get(id));
    }

    @Override
    public Optional<ClientActivation> getActivationByName(@NotNull String name) {
        return getActivationById(VoiceActivation.generateId(name));
    }

    @Override
    public Collection<ClientActivation> getActivations() {
        return activations;
    }

    @Override
    public boolean unregister(@NotNull UUID id) {
        ClientActivation activation = activationById.remove(id);
        if (activation != null) {
            activation.closeEncoders();

            if (id.equals(VoiceActivation.PROXIMITY_ID)) {
                this.parentActivation = createParentActivation(getServerConfig());
            }

            boolean removed = activations.remove(activation);
            voiceClient.getEventBus().call(new ClientActivationUnregisteredEvent(activation));
            return removed;
        }

        return false;
    }

    @Override
    public boolean unregister(@NotNull String name) {
        return unregister(VoiceActivation.generateId(name));
    }

    @Override
    public boolean unregister(@NotNull ClientActivation activation) {
        return unregister(activation.getId());
    }

    @Override
    public void clear() {
        activations.clear();
        activationById.clear();
        this.initialized = false;
    }

    private ClientConfig.Server getServerConfig() {
        return config.getServers().getById(
                voiceClient.getServerInfo()
                        .map(ServerInfo::getServerId)
                        .orElseThrow(() -> new IllegalStateException("Not connected"))
        ).orElseThrow(() -> new IllegalStateException("Server config is empty"));
    }

    private VoiceClientActivation createParentActivation(@NotNull ClientConfig.Server serverConfig) {
        Activation serverActivation = new VoiceActivation(
                VoiceActivation.PROXIMITY_NAME,
                "pv.activation.parent",
                "",
                Collections.emptyList(),
                0,
                false,
                false,
                true,
                null,
                1
        );

        ConfigClientActivation activationConfig = config.getActivations().getActivation(serverActivation.getId(), serverActivation);
        IntConfigEntry activationDistance = serverConfig.getActivationDistance(serverActivation.getId(), serverActivation);
        activationDistance.setDefault(0, 0, 0);
        if (activationConfig.getConfigType().value() == ClientActivation.Type.INHERIT) {
            LOGGER.warn("Proximity activation type cannot be INHERIT. Changed to PUSH_TO_TALK");
            activationConfig.getConfigType().set(ClientActivation.Type.PUSH_TO_TALK);
        }

        return new VoiceClientActivation(
                voiceClient,
                config,
                activationConfig,
                activationDistance,
                serverActivation,
                ""
        );
    }
}
