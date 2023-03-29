package su.plo.voice.client.audio.capture;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import gg.essential.universal.UChat;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.Config;
import su.plo.config.entry.BooleanConfigEntry;
import su.plo.config.entry.ConfigEntry;
import su.plo.config.entry.IntConfigEntry;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.api.client.config.keybind.KeyBinding;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.util.AudioUtil;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.config.capture.ConfigClientActivation;
import su.plo.voice.client.config.keybind.ConfigKeyBindings;
import su.plo.voice.client.config.keybind.KeyBindingConfigEntry;
import su.plo.voice.proto.data.audio.capture.Activation;
import su.plo.voice.proto.data.audio.capture.CaptureInfo;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerActivationDistancesPacket;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Config
public final class VoiceClientActivation
        extends VoiceActivation
        implements ClientActivation {

    private final PlasmoVoiceClient voiceClient;
    private final VoiceClientConfig config;

    private final IntConfigEntry configDistance;
    private final ConfigEntry<ClientActivation.Type> configType;
    private final BooleanConfigEntry configToggle;

    private final KeyBindingConfigEntry pttKey;
    private final KeyBindingConfigEntry toggleKey;

    private final KeyBindingConfigEntry distanceIncreaseKey;
    private final KeyBindingConfigEntry distanceDecreaseKey;

    private final AtomicBoolean disabled = new AtomicBoolean(false);

    @Getter
    private boolean active;
    @Getter
    private long lastActivation;

    private @Nullable AudioEncoder monoEncoder;
    private @Nullable AudioEncoder stereoEncoder;

    public VoiceClientActivation(@NotNull PlasmoVoiceClient voiceClient,
                                 @NotNull VoiceClientConfig config,
                                 @NotNull ConfigClientActivation activationConfig,
                                 @NotNull IntConfigEntry activationDistance,
                                 @NotNull Activation activation,
                                 @NotNull String icon) {
        super(
                activation.getName(),
                activation.getTranslation(),
                icon,
                new ArrayList<>(activation.getDistances()),
                activation.getDefaultDistance(),
                activation.isProximity(),
                activation.isStereoSupported(),
                activation.isTransitive(),
                activation.getEncoderInfo().orElse(null),
                activation.getWeight()
        );

        this.voiceClient = voiceClient;
        this.config = config;
        ConfigKeyBindings hotKeys = config.getKeyBindings();

        // load values from config
        this.configDistance = activationDistance;
        this.configType = activationConfig.getConfigType();
        this.configToggle = activationConfig.getConfigToggle();

        this.pttKey = createHotKey(hotKeys, activation, "ptt", true);
        this.toggleKey = createHotKey(hotKeys, activation, "toggle", false);

        this.distanceIncreaseKey = createHotKey(hotKeys, activation, "distance_increase", false);
        this.distanceDecreaseKey = createHotKey(hotKeys, activation, "distance_decrease", false);

        toggleKey.value().clearPressListener();
        toggleKey.value().addPressListener(this::onToggle);

        distanceIncreaseKey.value().clearPressListener();
        distanceIncreaseKey.value().addPressListener(this::onDistanceIncrease);

        distanceDecreaseKey.value().clearPressListener();
        distanceDecreaseKey.value().addPressListener(this::onDistanceDecrease);

        configDistance.clearChangeListeners();
        configDistance.addChangeListener(this::onDistanceChange);

        if (encoderInfo != null) {
            CaptureInfo captureInfo = voiceClient.getServerInfo()
                    .map(ServerInfo::getVoiceInfo)
                    .map(ServerInfo.VoiceInfo::getCaptureInfo)
                    .orElseThrow(() -> new IllegalStateException("not connected to voice server"));

            int sampleRate = captureInfo.getSampleRate();

            this.monoEncoder = voiceClient.getCodecManager().createEncoder(
                    encoderInfo,
                    captureInfo.getSampleRate(),
                    false,
                    (sampleRate / 1_000) * 20,
                    captureInfo.getMtuSize()
            );
            this.stereoEncoder = voiceClient.getCodecManager().createEncoder(
                    encoderInfo,
                    captureInfo.getSampleRate(),
                    true,
                    (sampleRate / 1_000) * 20,
                    captureInfo.getMtuSize()
            );
        }
    }

    @Override
    public Type getType() {
        return configType.value();
    }

    @Override
    public KeyBinding getPttKey() {
        return pttKey.value();
    }

    public KeyBindingConfigEntry getPttConfigEntry() {
        return pttKey;
    }

    @Override
    public KeyBinding getToggleKey() {
        return toggleKey.value();
    }

    @Override
    public KeyBinding getDistanceIncreaseKey() {
        return distanceIncreaseKey.value();
    }

    public KeyBindingConfigEntry getDistanceIncreaseConfigEntry() {
        return distanceIncreaseKey;
    }

    @Override
    public KeyBinding getDistanceDecreaseKey() {
        return distanceDecreaseKey.value();
    }

    @Override
    public Optional<AudioEncoder> getMonoEncoder() {
        return Optional.ofNullable(monoEncoder);
    }

    @Override
    public Optional<AudioEncoder> getStereoEncoder() {
        return Optional.ofNullable(stereoEncoder);
    }

    public KeyBindingConfigEntry getDistanceDecreaseConfigEntry() {
        return distanceDecreaseKey;
    }

    public KeyBindingConfigEntry getToggleConfigEntry() {
        return toggleKey;
    }

    @Override
    public void setDisabled(boolean disabled) {
        this.disabled.set(disabled);
    }

    @Override
    public boolean isDisabled() {
        return (getType() == Type.VOICE && configToggle.value()) || disabled.get();
    }

    @Override
    public int getDistance() {
        return configDistance.value();
    }

    @Override
    public @NotNull Result process(short[] samples, @Nullable Result result) {
        if (isDisabled()) {
            if (this.active) {
                this.active = false;
                return Result.END;
            }

            return Result.NOT_ACTIVATED;
        }

        if (getType() == Type.PUSH_TO_TALK) {
            return handlePTT();
        } else if (getType() == Type.VOICE) {
            return handleVoice(samples, result);
        } else if (getType() == Type.INHERIT) {
            return handleInherit(result);
        }

        return Result.NOT_ACTIVATED;
    }

    @Override
    public void reset() {
        this.active = false;
        this.lastActivation = 0L;
    }

    @Override
    public void closeEncoders() {
        getMonoEncoder().ifPresent(AudioEncoder::close);
        getStereoEncoder().ifPresent(AudioEncoder::close);

        this.monoEncoder = null;
        this.stereoEncoder = null;
    }

    private @NotNull Result handlePTT() {
        boolean pressed = getPttKey().isPressed();

        if (pressed) {
            if (!active) this.active = true;
            this.lastActivation = System.currentTimeMillis();
        } else if (active && (System.currentTimeMillis() - lastActivation > 350L)) {
            this.active = false;

            return Result.END;
        }

        return active ? Result.ACTIVATED : Result.NOT_ACTIVATED;
    }

    private @NotNull Result handleVoice(short[] samples, @Nullable Result result) {
        if (configToggle.value()) {
            if (active) {
                this.active = false;
                return Result.END;
            }

            return Result.NOT_ACTIVATED;
        }

        if (result != null) {
            if (result == Result.ACTIVATED) {
                this.active = true;
                this.lastActivation = System.currentTimeMillis();
                return result;
            } else if (result == Result.END) {
                this.active = false;
                return result;
            }
        }

        boolean lastActivated = System.currentTimeMillis() - lastActivation <= 500L;
        boolean voiceDetected = AudioUtil.containsMinAudioLevel(samples, config.getVoice().getActivationThreshold().value());
        if (lastActivated || voiceDetected) {
            if (voiceDetected) this.lastActivation = System.currentTimeMillis();
            if (!active) this.active = true;

            return Result.ACTIVATED;
        }

        if (active) {
            this.active = false;
            return Result.END;
        }

        return Result.NOT_ACTIVATED;
    }

    private @NotNull Result handleInherit(@Nullable Result result) {
        if (result == null) return Result.NOT_ACTIVATED;

        if (configToggle.value()) {
            if (active) {
                this.active = false;
                return Result.END;
            }

            return Result.NOT_ACTIVATED;
        }

        this.active = result == Result.ACTIVATED;
        if (active) this.lastActivation = System.currentTimeMillis();

        return result;
    }

    private void onToggle(@NotNull KeyBinding.Action action) {
        if (action != KeyBinding.Action.DOWN || getType() == Type.PUSH_TO_TALK) return;
        configToggle.invert();

        UChat.actionBar(RenderUtil.getTextConverter().convert(
                MinecraftTextComponent.translatable(
                        "message.plasmovoice.activation.toggle",
                        MinecraftTextComponent.translatable(translation),
                        !configToggle.value()
                                ? MinecraftTextComponent.translatable("message.plasmovoice.on")
                                : MinecraftTextComponent.translatable("message.plasmovoice.off")
                )
        ));
    }

    private void onDistanceIncrease(@NotNull KeyBinding.Action action) {
        if (action != KeyBinding.Action.DOWN) return;

        int index = (distances.indexOf(getDistance()) + 1) % distances.size();
        configDistance.set(distances.get(index));

        sendDistanceChangedMessage();
    }

    private void onDistanceDecrease(@NotNull KeyBinding.Action action) {
        if (action != KeyBinding.Action.DOWN) return;

        int index = distances.indexOf(getDistance()) - 1;
        if (index < 0) {
            index = distances.size() - 1;
        }
        configDistance.set(distances.get(index));

        sendDistanceChangedMessage();
    }

    private void onDistanceChange(int distance) {
        voiceClient.getServerConnection()
                .ifPresent((connection) -> {
                    Map<UUID, Integer> distanceByActivationId = Maps.newHashMap();
                    distanceByActivationId.put(id, distance);

                    connection.sendPacket(new PlayerActivationDistancesPacket(distanceByActivationId));
                });
    }

    private void sendDistanceChangedMessage() {
        UChat.actionBar(RenderUtil.getTextConverter().convert(
                MinecraftTextComponent.translatable(
                        "message.plasmovoice.distance_changed",
                        MinecraftTextComponent.translatable(translation),
                        getDistance()
                )
        ));
    }

    private KeyBindingConfigEntry createHotKey(@NotNull ConfigKeyBindings hotKeys,
                                               @NotNull Activation activation,
                                               @NotNull String suffix,
                                               boolean anyContext) {
        String keyName = "key.plasmovoice." + activation.getName() + "." + suffix;
        Optional<KeyBindingConfigEntry> key = hotKeys.getConfigKeyBinding(keyName);
        if (!key.isPresent()) {
            hotKeys.register(keyName, ImmutableList.of(), "hidden", anyContext);
            key = hotKeys.getConfigKeyBinding(keyName);
        }

        if (!key.isPresent())
            throw new IllegalStateException("Failed to register keybinding " + activation.getName() + "." + suffix);

        return key.get();
    }
}
