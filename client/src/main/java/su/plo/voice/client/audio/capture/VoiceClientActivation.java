package su.plo.voice.client.audio.capture;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import su.plo.slib.api.chat.component.McTextComponent;
import gg.essential.universal.UChat;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.Config;
import su.plo.config.entry.BooleanConfigEntry;
import su.plo.config.entry.ConfigEntry;
import su.plo.config.entry.IntConfigEntry;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.api.client.config.hotkey.Hotkey;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.util.AudioUtil;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.config.capture.ConfigClientActivation;
import su.plo.voice.client.config.hotkey.ConfigHotkeys;
import su.plo.voice.client.config.hotkey.HotkeyConfigEntry;
import su.plo.voice.proto.data.audio.capture.Activation;
import su.plo.voice.proto.data.audio.capture.CaptureInfo;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerActivationDistancesPacket;

import java.util.*;
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

    private final HotkeyConfigEntry pttKey;
    private final HotkeyConfigEntry toggleKey;

    private final HotkeyConfigEntry distanceIncreaseKey;
    private final HotkeyConfigEntry distanceDecreaseKey;

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
        ConfigHotkeys hotKeys = config.getKeyBindings();

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

            this.monoEncoder = voiceClient.getCodecManager().createEncoder(
                    encoderInfo,
                    captureInfo.getSampleRate(),
                    false,
                    captureInfo.getMtuSize()
            );
            this.stereoEncoder = voiceClient.getCodecManager().createEncoder(
                    encoderInfo,
                    captureInfo.getSampleRate(),
                    true,
                    captureInfo.getMtuSize()
            );
        }
    }

    @Override
    public @NotNull Type getType() {
        return configType.value();
    }

    @Override
    public @NotNull Hotkey getPttKey() {
        return pttKey.value();
    }

    public HotkeyConfigEntry getPttConfigEntry() {
        return pttKey;
    }

    @Override
    public @NotNull Hotkey getToggleKey() {
        return toggleKey.value();
    }

    @Override
    public @NotNull Hotkey getDistanceIncreaseKey() {
        return distanceIncreaseKey.value();
    }

    public HotkeyConfigEntry getDistanceIncreaseConfigEntry() {
        return distanceIncreaseKey;
    }

    @Override
    public @NotNull Hotkey getDistanceDecreaseKey() {
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

    public HotkeyConfigEntry getDistanceDecreaseConfigEntry() {
        return distanceDecreaseKey;
    }

    public HotkeyConfigEntry getToggleConfigEntry() {
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
    public void cleanup() {
        getMonoEncoder().ifPresent(AudioEncoder::close);
        getStereoEncoder().ifPresent(AudioEncoder::close);

        toggleKey.value().clearPressListener();
        distanceIncreaseKey.value().clearPressListener();
        distanceDecreaseKey.value().clearPressListener();
        configDistance.clearChangeListeners();

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

    private void onToggle(@NotNull Hotkey.Action action) {
        if (action != Hotkey.Action.DOWN || getType() == Type.PUSH_TO_TALK) return;
        configToggle.invert();

        UChat.actionBar(RenderUtil.getTextConverter().convert(
                McTextComponent.translatable(
                        "message.plasmovoice.activation.toggle",
                        McTextComponent.translatable(translation),
                        !configToggle.value()
                                ? McTextComponent.translatable("message.plasmovoice.on")
                                : McTextComponent.translatable("message.plasmovoice.off")
                )
        ));
    }

    private void onDistanceIncrease(@NotNull Hotkey.Action action) {
        if (action != Hotkey.Action.DOWN) return;

        int index = (distances.indexOf(getDistance()) + 1) % distances.size();
        configDistance.set(distances.get(index));

        sendDistanceChangedMessage();
    }

    private void onDistanceDecrease(@NotNull Hotkey.Action action) {
        if (action != Hotkey.Action.DOWN) return;

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
                McTextComponent.translatable(
                        "message.plasmovoice.distance_changed",
                        McTextComponent.translatable(translation),
                        getDistance()
                )
        ));
    }

    private HotkeyConfigEntry createHotKey(@NotNull ConfigHotkeys hotKeys,
                                           @NotNull Activation activation,
                                           @NotNull String suffix,
                                           boolean anyContext) {
        String keyName = "key.plasmovoice." + activation.getName() + "." + suffix;
        Optional<HotkeyConfigEntry> key = hotKeys.getConfigHotkey(keyName);
        if (!key.isPresent()) {
            hotKeys.register(keyName, ImmutableList.of(), "hidden", anyContext);
            key = hotKeys.getConfigHotkey(keyName);
        }

        if (!key.isPresent())
            throw new IllegalStateException("Failed to register keybinding " + activation.getName() + "." + suffix);

        return key.get();
    }
}
