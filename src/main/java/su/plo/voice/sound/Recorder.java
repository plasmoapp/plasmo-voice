package su.plo.voice.sound;

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

import javax.sound.sampled.*;
import java.io.IOException;

public class Recorder implements Runnable {
    public static int mtuSize = 900;
    public static int sampleRate = 24000;
    public static int frameSize = (sampleRate / 1000) * 2 * 20;
    public static AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
    public static AudioFormat stereoFormat = new AudioFormat(sampleRate, 16, 2, true, false);

    private long sequenceNumber = 0L;
    public boolean running;
    public Thread thread;

    private long lastSpeak;
    private byte[] lastBuffer;

    private TargetDataLine mic;
    private OpusEncoder encoder;

    public Recorder() {
        this.encoder = new OpusEncoder(sampleRate, frameSize, mtuSize, Opus.OPUS_APPLICATION_VOIP);
    }

    public void updateConfig(int rate) {
        if(rate != 8000 && rate != 12000 && rate != 24000 && rate != 48000) {
            VoiceClient.LOGGER.info("Incorrect sample rate");
            return;
        }

        if(this.thread != null) {
            this.running = false;
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {}
            }
        }

        format = new AudioFormat(rate, 16, 1, true, false);
        stereoFormat = new AudioFormat(rate, 16, 2, true, false);
        sampleRate = rate;
        frameSize = (sampleRate / 1000) * 2 * 20;

        this.encoder.close();
        this.encoder = new OpusEncoder(sampleRate, frameSize, mtuSize, Opus.OPUS_APPLICATION_VOIP);

        this.start();
    }

    public void close() {
        this.running = false;
        this.sequenceNumber = 0L;
        this.lastBuffer = null;

        if(mic != null) {
            mic.stop();
            mic.flush();
            mic.close();
            thread = null;
            mic = null;
        }

        synchronized (this) {
            this.notify();
        }
    }

    public void run() {
        if(mic != null) {
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {}
            }
        }

        mic = DataLines.getMicrophone();
        if(mic == null) {
            VoiceClient.LOGGER.error("Failed to open mic");
            return;
        }

        try {
            mic.open(format);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        this.running = true;
        while (running) {
            final PlayerEntity player = MinecraftClient.getInstance().player;
            if(player == null) {
                this.running = false;
                break;
            }

            if(VoiceClient.socketUDP == null || VoiceClient.serverConfig == null) {
                this.running = false;
                break;
            }

            // muted
            if(MicTestButton.micActive) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException ignored) {}
                continue;
            }

            // muted
            if((VoiceClient.serverConfig.mutedClients.containsKey(player.getUuid()) || VoiceClient.muted)) {
                VoiceClient.speaking = false;
                VoiceClient.speakingPriority = false;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
                continue;
            }

            if(!VoiceClient.config.voiceActivation || VoiceClient.serverConfig.disableVoiceActivation) {
                pushToTalk();
            } else {
                voiceActivation();
            }
        }

        this.close();
    }

    private void voiceActivation() {
        InputUtil.Key priorityPushToTalkKey = KeyBindingHelper.getBoundKeyOf(VoiceClient.priorityPushToTalk);

        boolean priorityPressed = keyPressed(priorityPushToTalkKey) && VoiceClient.serverConfig.hasPriority
                && VoiceClient.serverConfig.priorityDistance > VoiceClient.serverConfig.maxDistance;

        if(!VoiceClient.speakingPriority && priorityPressed) {
            VoiceClient.speakingPriority = true;
        } else if(VoiceClient.speaking && VoiceClient.speakingPriority && !priorityPressed) {
            VoiceClient.speakingPriority = false;
        }

        int blockSize = frameSize;
        byte[] normBuffer = new byte[blockSize];

        mic.start();

        int read = mic.read(normBuffer, 0, blockSize);
        if (read == -1) {
            return;
        }

        Utils.adjustVolumeMono(normBuffer, (float) VoiceClient.config.microphoneAmplification);

        int offset = Utils.getActivationOffset(normBuffer, VoiceClient.config.voiceActivationThreshold);
        if(offset > 0) {
            this.lastSpeak = System.currentTimeMillis();

            if(!VoiceClient.speaking) {
                VoiceClient.speaking = true;
                if(this.lastBuffer != null) {
                    this.sendPacket(lastBuffer);
                }
                this.sendPacket(normBuffer);
                return;
            }
        } else if(VoiceClient.speaking && System.currentTimeMillis() - lastSpeak > 500L) {
            VoiceClient.speaking = false;
            VoiceClient.speakingPriority = false;
//            mic.stop();
//            mic.flush();

            this.sendPacket(normBuffer);
            this.sendEndPacket();
            return;
        }

        if(VoiceClient.speaking) {
            this.sendPacket(normBuffer);
        }

        this.lastBuffer = normBuffer;
    }

    private void pushToTalk() {
        InputUtil.Key pushToTalkKey = KeyBindingHelper.getBoundKeyOf(VoiceClient.pushToTalk);
        InputUtil.Key priorityPushToTalkKey = KeyBindingHelper.getBoundKeyOf(VoiceClient.priorityPushToTalk);

        boolean priorityPressed = keyPressed(priorityPushToTalkKey) && VoiceClient.serverConfig.hasPriority
                && VoiceClient.serverConfig.priorityDistance > VoiceClient.serverConfig.maxDistance;
        boolean pushToTalkPressed = keyPressed(pushToTalkKey) || priorityPressed;

        if(!VoiceClient.speakingPriority && priorityPressed) {
            VoiceClient.speakingPriority = true;
        } else if(VoiceClient.speaking && VoiceClient.speakingPriority && !priorityPressed && pushToTalkPressed) {
            VoiceClient.speakingPriority = false;
        }

        if(pushToTalkPressed && !VoiceClient.speaking) {
            VoiceClient.speaking = true;
            mic.flush();
            this.lastSpeak = System.currentTimeMillis();
        } else if(pushToTalkPressed) {
            this.lastSpeak = System.currentTimeMillis();
        } else if(VoiceClient.speaking && System.currentTimeMillis() - lastSpeak > 350L) {
            VoiceClient.speaking = false;
            VoiceClient.speakingPriority = false;
            mic.stop();
            mic.flush();

            this.sendEndPacket();
            return;
        }

        if(!VoiceClient.speaking) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {}
            return;
        }


        int blockSize = frameSize;
        byte[] normBuffer = new byte[blockSize];

        mic.start();
        int read = mic.read(normBuffer, 0, blockSize);
        if (read == -1) {
            return;
        }

        Utils.adjustVolumeMono(normBuffer, (float) VoiceClient.config.microphoneAmplification);
        this.sendPacket(normBuffer);
    }

    private void sendPacket(byte[] raw) {
        if(VoiceClient.socketUDP == null) {
            this.running = false;
            return;
        }

        try {
            if(!VoiceClient.socketUDP.isClosed()) {
                VoiceClient.socketUDP.send(new VoiceClientPacket(encoder.encode(raw), sequenceNumber++,
                        VoiceClient.speakingPriority
                                ? VoiceClient.serverConfig.priorityDistance
                                : VoiceClient.serverConfig.distance));
            } else {
                this.running = false;
            }
        } catch (IOException ignored) {
            this.running = false;
        }
    }

    private void sendEndPacket() {
        if(VoiceClient.socketUDP == null) {
            this.running = false;
            return;
        }

        if(!VoiceClient.socketUDP.isClosed()) {
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
        } else if(key.getCategory() == InputUtil.Type.MOUSE && key.getCode() != InputUtil.UNKNOWN_KEY.getCode()) {
            pushToTalkPressed = VoiceClient.mouseKeyPressed.contains(key.getCode());
        }

        return pushToTalkPressed;
    }

    public void start() {
        if(this.thread != null) {
            this.running = false;
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {}
            }
        }

        this.thread = new Thread(this, "Input Device Recorder");
        this.thread.start();
    }
}
