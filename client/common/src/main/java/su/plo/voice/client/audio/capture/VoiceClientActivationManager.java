package su.plo.voice.client.audio.capture;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.api.client.audio.capture.ClientActivationManager;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.config.capture.ConfigClientActivation;
import su.plo.voice.config.entry.IntConfigEntry;
import su.plo.voice.proto.data.audio.capture.Activation;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
public final class VoiceClientActivationManager implements ClientActivationManager {

    private static final Logger LOGGER = LogManager.getLogger();

    private final MinecraftClientLib minecraft;
    private final PlasmoVoiceClient voiceClient;
    private final ClientConfig config;

    private ClientActivation parentActivation;

    private final List<ClientActivation> activations = new CopyOnWriteArrayList<>();
    private final Map<UUID, ClientActivation> activationById = Maps.newConcurrentMap();

    @Override
    public Optional<ClientActivation> getParentActivation() {
        return Optional.ofNullable(parentActivation);
    }

    @Override
    public @NotNull ClientActivation register(@NotNull ClientActivation activation) {
        int index;
        for (index = 0; index < activations.size(); index++) {
            ClientActivation act = activations.get(index);
            if (activation.getWeight() >= act.getWeight()) break;
        }

        activations.add(index, activation);
        activationById.put(activation.getId(), activation);

        return activation;
    }

    @Override
    public @NotNull Collection<ClientActivation> register(@NotNull UUID serverId, @NotNull Collection<Activation> activations) {
        Optional<ClientConfig.Server> serverConfig = config.getServers().getById(serverId);
        if (!serverConfig.isPresent()) throw new IllegalStateException("Server config is empty");

        for (Activation serverActivation : activations) {
            ConfigClientActivation activationConfig = config.getActivations().getActivation(serverActivation.getId(), serverActivation);
            IntConfigEntry activationDistance = serverConfig.get().getActivationDistance(serverActivation.getId(), serverActivation);
            activationDistance.setDefault(
                    serverActivation.getDefaultDistance(),
                    serverActivation.getMinDistance(),
                    serverActivation.getMaxDistance()
            );

            ClientActivation activation = register(new VoiceClientActivation(
                    minecraft,
                    voiceClient,
                    config,
                    activationConfig,
                    activationDistance,
                    serverActivation
            ));

            if (activation.getId().equals(VoiceActivation.PROXIMITY_ID)) {
                if (activationConfig.getConfigType().value() == ClientActivation.Type.INHERIT) {
                    LOGGER.warn("Proximity activation type cannot be INHERIT. Changed to PUSH_TO_TALK");
                    activationConfig.getConfigType().set(ClientActivation.Type.PUSH_TO_TALK);
                }

                this.parentActivation = activation;
            }
        }

        if (parentActivation == null) {
            Activation serverActivation = new VoiceActivation(
                    VoiceActivation.PROXIMITY_NAME,
                    "key.plasmovoice.parent",
                    "",
                    Collections.emptyList(),
                    0,
                    1
            );

            ConfigClientActivation activationConfig = config.getActivations().getActivation(serverActivation.getId(), serverActivation);
            IntConfigEntry activationDistance = serverConfig.get().getActivationDistance(serverActivation.getId(), serverActivation);
            activationDistance.setDefault(0, 0, 0);
            if (activationConfig.getConfigType().value() == ClientActivation.Type.INHERIT) {
                LOGGER.warn("Proximity activation type cannot be INHERIT. Changed to PUSH_TO_TALK");
                activationConfig.getConfigType().set(ClientActivation.Type.PUSH_TO_TALK);
            }

            this.parentActivation = new VoiceClientActivation(
                    minecraft,
                    voiceClient,
                    config,
                    activationConfig,
                    activationDistance,
                    serverActivation
            );
        }

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
        if (activation != null) return activations.remove(activation);
        return false;
    }

    @Override
    public boolean unregister(@NotNull String name) {
        return unregister(VoiceActivation.generateId(name));
    }

    @Override
    public boolean unregister(@NotNull ClientActivation activation) {
        if (activations.remove(activation)) {
            activationById.remove(activation.getId());
            return true;
        }

        return false;
    }
}
