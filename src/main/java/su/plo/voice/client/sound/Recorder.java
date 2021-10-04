package su.plo.voice.client.sound;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.sound.opus.OpusEncoder;
import su.plo.voice.client.utils.AudioUtils;
import su.plo.voice.common.packets.udp.VoiceClientPacket;
import su.plo.voice.common.packets.udp.VoiceEndClientPacket;
import su.plo.voice.rnnoise.Denoiser;
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
    private Denoiser denoiser;

    private long sequenceNumber = 0L;
    private long lastSpeak;
    private byte[] lastBuffer;

    private final Minecraft client = Minecraft.getInstance();

    public Recorder() {
        this.encoder = new OpusEncoder(sampleRate, frameSize, mtuSize, Opus.OPUS_APPLICATION_VOIP);
        if (VoiceClient.getClientConfig().rnNoise.get()) {
            this.denoiser = new Denoiser();
        }
    }

    public void toggleRnNoise() {
        synchronized (this) {
            if (this.denoiser != null) {
                this.denoiser.close();
                this.denoiser = null;
            } else {
                this.denoiser = new Denoiser();
            }
        }
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
        if (this.encoder != null) {
            this.encoder.close();
        }

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
            final LocalPlayer player = client.player;
            if (player == null) {
                this.running = false;
                break;
            }

            if (!VoiceClient.isConnected()) {
                this.running = false;
                break;
            }

            // muted
            if ((VoiceClient.getServerConfig().getMuted().containsKey(player.getUUID()) || VoiceClient.getClientConfig().microphoneMuted.get()
                    || VoiceClient.getClientConfig().speakerMuted.get())) {
                VoiceClient.setSpeaking(false);
                VoiceClient.setSpeakingPriority(false);

                if (client.screen instanceof VoiceSettingsScreen) {
                    readBuffer();
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                }
                continue;
            }

            if (!VoiceClient.getClientConfig().voiceActivation.get() ||
                    VoiceClient.getServerConfig().isVoiceActivationDisabled()) {
                pushToTalk();
            } else {
                voiceActivation();
            }
        }

        this.close();
    }

    private void voiceActivation() {
        boolean priorityPressed = VoiceClient.getClientConfig().keyBindings.pushToTalk.get().isPressed()
                && VoiceClient.getServerConfig().isPriority()
                && VoiceClient.getServerConfig().getPriorityDistance() > VoiceClient.getServerConfig().getMaxDistance();

        byte[] normBuffer = readBuffer();
        assert normBuffer != null;

        if (VoiceClient.isMicrophoneLoopback()) {
            if (VoiceClient.isSpeaking()) {
                VoiceClient.setSpeaking(false);
                VoiceClient.setSpeakingPriority(false);
            }
            return;
        }

        if (!VoiceClient.isSpeakingPriority() && priorityPressed) {
            VoiceClient.setSpeakingPriority(true);
        } else if (VoiceClient.isSpeaking() && VoiceClient.isSpeakingPriority() && !priorityPressed) {
            VoiceClient.setSpeakingPriority(false);
        }

        boolean activated = System.currentTimeMillis() - lastSpeak <= 500L;
        int offset = AudioUtils.getActivationOffset(normBuffer, VoiceClient.getClientConfig().voiceActivationThreshold.get());
        if (offset > 0 || activated) {
            if (offset > 0) {
                this.lastSpeak = System.currentTimeMillis();
            }

            if (!VoiceClient.isSpeaking()) {
                VoiceClient.setSpeaking(true);
                if (this.lastBuffer != null) {
                    this.sendPacket(lastBuffer);
                }
                this.sendPacket(normBuffer);
                return;
            }
        } else if (VoiceClient.isSpeaking() && !activated) {
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
        boolean priorityPressed = VoiceClient.getClientConfig().keyBindings.priorityPushToTalk.get().isPressed()
                && VoiceClient.getServerConfig().isPriority()
                && VoiceClient.getServerConfig().getPriorityDistance() > VoiceClient.getServerConfig().getMaxDistance();
        boolean pushToTalkPressed = VoiceClient.getClientConfig().keyBindings.pushToTalk.get().isPressed()
                || priorityPressed;

        if (!VoiceClient.isMicrophoneLoopback()) {
            if (!VoiceClient.isSpeakingPriority() && priorityPressed) {
                VoiceClient.setSpeakingPriority(true);
            } else if (VoiceClient.isSpeaking() && VoiceClient.isSpeakingPriority() && !priorityPressed && pushToTalkPressed) {
                VoiceClient.setSpeakingPriority(false);
            }
        }

        if (pushToTalkPressed && !VoiceClient.isSpeaking() && !VoiceClient.isMicrophoneLoopback()) {
            VoiceClient.setSpeaking(true);
            microphone.flush();
            this.lastSpeak = System.currentTimeMillis();
        } else if (pushToTalkPressed && !VoiceClient.isMicrophoneLoopback()) {
            this.lastSpeak = System.currentTimeMillis();
        } else if (VoiceClient.isSpeaking() && (System.currentTimeMillis() - lastSpeak > 350L || VoiceClient.isMicrophoneLoopback())) {
            VoiceClient.setSpeaking(false);
            VoiceClient.setSpeakingPriority(false);
            microphone.stop();
            microphone.flush();

            this.sendEndPacket();
            return;
        }

        if (!VoiceClient.isSpeaking()) {
            if (client.screen instanceof VoiceSettingsScreen) {
                readBuffer();
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
            }
            return;
        }

        byte[] normBuffer = readBuffer();
        assert normBuffer != null;

        this.sendPacket(normBuffer);
    }

    private byte[] readBuffer() {
        synchronized (this) {
            if (this.encoder == null || this.encoder.isClosed()) {
                return null;
            }

            int blockSize = frameSize;
            byte[] normBuffer = new byte[blockSize];

            microphone.start();
            int read = microphone.read(normBuffer, 0, blockSize);
            if (read == -1) {
                return null;
            }

            AudioUtils.adjustVolume(normBuffer, VoiceClient.getClientConfig().microphoneAmplification.get().floatValue());

            if (this.denoiser != null) {
                normBuffer = this.denoiser.process(normBuffer);
            }

            if (client.screen instanceof VoiceSettingsScreen screen) {
                screen.setMicrophoneValue(normBuffer);
            }

            return normBuffer;
        }
    }

    private void sendPacket(byte[] raw) {
        if (VoiceClient.isMicrophoneLoopback()) {
            return;
        }

        if (!VoiceClient.isConnected()) {
            this.running = false;
            return;
        }

        try {
            if (!VoiceClient.socketUDP.isClosed()) {
                VoiceClient.socketUDP.send(new VoiceClientPacket(
                        encoder.encode(raw),
                        sequenceNumber++,
                        VoiceClient.isSpeakingPriority()
                                ? VoiceClient.getServerConfig().getPriorityDistance()
                                : VoiceClient.getServerConfig().getDistance()
                ));
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
