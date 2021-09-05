package su.plo.voice.sound;

import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.common.packets.udp.VoiceClientPacket;
import su.plo.voice.common.packets.udp.VoiceEndClientPacket;
import su.plo.voice.gui.settings.MicTestButton;
import su.plo.voice.utils.Utils;
import tomp2p.opuswrapper.Opus;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class Recorder implements Runnable {
    @Getter
    private static final int mtuSize = 900;
    @Getter
    private static int sampleRate = 24000;
    @Getter
    private static int frameSize = (sampleRate / 1000) * 2 * 20;
    @Getter
    private static AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
    @Getter
    private static AudioFormat stereoFormat = new AudioFormat(sampleRate, 16, 2, true, false);

    @Setter
    @Getter
    private boolean running;
    @Getter
    private Thread thread;

    @Getter
    private TargetDataLine microphone;
    private OpusEncoder encoder;

    private long sequenceNumber = 0L;
    private long lastSpeak;
    private byte[] lastBuffer;

    public Recorder() {
        this.encoder = new OpusEncoder(sampleRate, frameSize, mtuSize, Opus.OPUS_APPLICATION_VOIP);
    }

    public void updateConfig(int rate) {
        if (rate != 8000 && rate != 12000 && rate != 24000 && rate != 48000) {
            VoiceClient.LOGGER.info("Incorrect sample rate");
            return;
        }

        if (this.thread != null) {
            this.waitForClose().thenRun(() -> {
                format = new AudioFormat(rate, 16, 1, true, false);
                stereoFormat = new AudioFormat(rate, 16, 2, true, false);
                sampleRate = rate;
                frameSize = (sampleRate / 1000) * 2 * 20;

                this.encoder.close();
                this.encoder = new OpusEncoder(sampleRate, frameSize, mtuSize, Opus.OPUS_APPLICATION_VOIP);

                if (VoiceClient.isConnected()) {
                    this.start();
                }
            });
        } else {
            format = new AudioFormat(rate, 16, 1, true, false);
            stereoFormat = new AudioFormat(rate, 16, 2, true, false);
            sampleRate = rate;
            frameSize = (sampleRate / 1000) * 2 * 20;

            this.encoder.close();
            this.encoder = new OpusEncoder(sampleRate, frameSize, mtuSize, Opus.OPUS_APPLICATION_VOIP);

            if (VoiceClient.isConnected()) {
                this.start();
            }
        }
    }

    public void close() {
        this.running = false;
        this.sequenceNumber = 0L;
        this.lastBuffer = null;

        if (microphone != null) {
            microphone.stop();
            microphone.flush();
            microphone.close();
            thread = null;
            microphone = null;
        }

        synchronized (this) {
            this.notifyAll();
        }
    }

    public void run() {
        if (microphone != null) {
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }

        microphone = DataLines.getMicrophone();
        if (microphone == null) {
            VoiceClient.LOGGER.error("Failed to open mic");
            return;
        }

        try {
            microphone.open(format);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return;
        }

        this.running = true;
        while (running) {
            final PlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) {
                this.running = false;
                break;
            }

            if (!VoiceClient.isConnected()) {
                this.running = false;
                break;
            }

            // muted
            if (MicTestButton.micActive) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException ignored) {
                }
                continue;
            }

            // muted
            if ((VoiceClient.getServerConfig().getMuted().containsKey(player.getUuid()) || VoiceClient.isMuted())) {
                VoiceClient.setSpeaking(false);
                VoiceClient.setSpeakingPriority(false);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                continue;
            }

            if (!VoiceClient.getClientConfig().isVoiceActivation() ||
                    VoiceClient.getServerConfig().isVoiceActivationDisabled()) {
                pushToTalk();
            } else {
                voiceActivation();
            }
        }

        this.close();
    }

    private void voiceActivation() {
        InputUtil.Key priorityPushToTalkKey = KeyBindingHelper.getBoundKeyOf(VoiceClient.priorityPushToTalk);

        boolean priorityPressed = keyPressed(priorityPushToTalkKey) && VoiceClient.getServerConfig().isPriority()
                && VoiceClient.getServerConfig().getPriorityDistance() > VoiceClient.getServerConfig().getMaxDistance();

        if (!VoiceClient.isSpeakingPriority() && priorityPressed) {
            VoiceClient.setSpeakingPriority(true);
        } else if (VoiceClient.isSpeaking() && VoiceClient.isSpeakingPriority() && !priorityPressed) {
            VoiceClient.setSpeakingPriority(false);
        }

        byte[] normBuffer = readBuffer();
        assert normBuffer != null;

        int offset = Utils.getActivationOffset(normBuffer, VoiceClient.getClientConfig().getVoiceActivationThreshold());
        if (offset > 0) {
            this.lastSpeak = System.currentTimeMillis();

            if (!VoiceClient.isSpeaking()) {
                VoiceClient.setSpeaking(true);
                if (this.lastBuffer != null) {
                    this.sendPacket(lastBuffer);
                }
                this.sendPacket(normBuffer);
                return;
            }
        } else if (VoiceClient.isSpeaking() && System.currentTimeMillis() - lastSpeak > 500L) {
            VoiceClient.setSpeaking(false);
            VoiceClient.setSpeakingPriority(false);

            this.sendPacket(normBuffer);
            this.sendEndPacket();
            return;
        }

        if (VoiceClient.isSpeaking()) {
            this.sendPacket(normBuffer);
        }

        this.lastBuffer = normBuffer;
    }

    private void pushToTalk() {
        InputUtil.Key pushToTalkKey = KeyBindingHelper.getBoundKeyOf(VoiceClient.pushToTalk);
        InputUtil.Key priorityPushToTalkKey = KeyBindingHelper.getBoundKeyOf(VoiceClient.priorityPushToTalk);

        boolean priorityPressed = keyPressed(priorityPushToTalkKey) && VoiceClient.getServerConfig().isPriority()
                && VoiceClient.getServerConfig().getPriorityDistance() > VoiceClient.getServerConfig().getMaxDistance();
        boolean pushToTalkPressed = keyPressed(pushToTalkKey) || priorityPressed;

        if (!VoiceClient.isSpeakingPriority() && priorityPressed) {
            VoiceClient.setSpeakingPriority(true);
        } else if (VoiceClient.isSpeaking() && VoiceClient.isSpeakingPriority() && !priorityPressed && pushToTalkPressed) {
            VoiceClient.setSpeakingPriority(false);
        }

        if (pushToTalkPressed && !VoiceClient.isSpeaking()) {
            VoiceClient.setSpeaking(true);
            microphone.flush();
            this.lastSpeak = System.currentTimeMillis();
        } else if (pushToTalkPressed) {
            this.lastSpeak = System.currentTimeMillis();
        } else if (VoiceClient.isSpeaking() && System.currentTimeMillis() - lastSpeak > 350L) {
            VoiceClient.setSpeaking(false);
            VoiceClient.setSpeakingPriority(false);
            microphone.stop();
            microphone.flush();

            this.sendEndPacket();
            return;
        }

        if (!VoiceClient.isSpeaking()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
            return;
        }

        byte[] normBuffer = readBuffer();
        assert normBuffer != null;
        this.sendPacket(normBuffer);
    }

    private byte[] readBuffer() {
        int blockSize = frameSize;
        byte[] normBuffer = new byte[blockSize];

        microphone.start();
        int read = microphone.read(normBuffer, 0, blockSize);
        if (read == -1) {
            return null;
        }

        Utils.adjustVolumeMono(normBuffer, (float) VoiceClient.getClientConfig().getMicrophoneAmplification());

        return normBuffer;
    }

    private void sendPacket(byte[] raw) {
        if (!VoiceClient.isConnected()) {
            this.running = false;
            return;
        }

        try {
            if (!VoiceClient.socketUDP.isClosed()) {
                VoiceClient.socketUDP.send(new VoiceClientPacket(encoder.encode(raw), sequenceNumber++,
                        VoiceClient.isSpeakingPriority()
                                ? VoiceClient.getServerConfig().getPriorityDistance()
                                : VoiceClient.getServerConfig().getDistance()));
            } else {
                this.running = false;
            }
        } catch (IOException ignored) {
            this.running = false;
        }
    }

    private void sendEndPacket() {
        if (!VoiceClient.isConnected()) {
            this.running = false;
            return;
        }

        if (!VoiceClient.socketUDP.isClosed()) {
            try {
                VoiceClient.socketUDP.send(new VoiceEndClientPacket());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean keyPressed(InputUtil.Key key) {
        boolean pushToTalkPressed = false;
        if (key.getCategory() == InputUtil.Type.KEYSYM && key.getCode() != InputUtil.UNKNOWN_KEY.getCode()) {
            pushToTalkPressed = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), key.getCode());
        } else if (key.getCategory() == InputUtil.Type.MOUSE && key.getCode() != InputUtil.UNKNOWN_KEY.getCode()) {
            pushToTalkPressed = VoiceClient.mouseKeyPressed.contains(key.getCode());
        }

        return pushToTalkPressed;
    }

    public void start() {
        if (this.thread != null) {
            this.waitForClose().thenRun(() -> {
                this.thread = new Thread(this, "Input Device Recorder");
                this.thread.start();
            });
        } else {
            this.thread = new Thread(this, "Input Device Recorder");
            this.thread.start();
        }
    }

    public CompletableFuture<Void> waitForClose() {
        return CompletableFuture.runAsync(() -> {
            this.running = false;
            synchronized (this) {
                try {
                    this.wait(1000L); // wait for 1 sec and just ignore it if notify not called
                } catch (InterruptedException ignored) {
                }
            }

            if (this.thread != null) {
                if (!this.thread.isInterrupted()) {
                    this.thread.interrupt();
                }
            }
        });
    }
}
