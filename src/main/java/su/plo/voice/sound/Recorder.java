package su.plo.voice.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import su.plo.voice.Voice;
import su.plo.voice.common.packets.udp.VoiceClientPacket;
import su.plo.voice.common.packets.udp.VoiceEndClientPacket;
import su.plo.voice.event.ClientInputEvent;
import su.plo.voice.gui.settings.MicTestButton;
import su.plo.voice.utils.Utils;
import tomp2p.opuswrapper.Opus;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

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
            Voice.LOGGER.info("Incorrect sample rate");
            return;
        }

        if(this.thread != null) {
            this.waitForClose().thenRun(() -> {
                format = new AudioFormat(rate, 16, 1, true, false);
                stereoFormat = new AudioFormat(rate, 16, 2, true, false);
                sampleRate = rate;
                frameSize = (sampleRate / 1000) * 2 * 20;

                this.encoder.close();
                this.encoder = new OpusEncoder(sampleRate, frameSize, mtuSize, Opus.OPUS_APPLICATION_VOIP);

                this.start();
            });
        } else {
            format = new AudioFormat(rate, 16, 1, true, false);
            stereoFormat = new AudioFormat(rate, 16, 2, true, false);
            sampleRate = rate;
            frameSize = (sampleRate / 1000) * 2 * 20;

            this.encoder.close();
            this.encoder = new OpusEncoder(sampleRate, frameSize, mtuSize, Opus.OPUS_APPLICATION_VOIP);

            this.start();
        }
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
            this.notifyAll();
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
            Voice.LOGGER.error("Failed to open mic");
            return;
        }

        try {
            mic.open(format);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        this.running = true;
        while (running) {
            final PlayerEntity player = Minecraft.getInstance().player;
            if(player == null) {
                this.running = false;
                break;
            }

            if(Voice.socketUDP == null || Voice.serverConfig == null) {
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
            if((Voice.serverConfig.mutedClients.containsKey(player.getUUID()) || Voice.muted)) {
                Voice.speaking = false;
                Voice.speakingPriority = false;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
                continue;
            }

            if(!Voice.config.voiceActivation || Voice.serverConfig.disableVoiceActivation) {
                pushToTalk();
            } else {
                voiceActivation();
            }
        }

        this.close();
    }

    private void voiceActivation() {
        boolean priorityPressed = ClientInputEvent.priorityPushToTalkPressed && Voice.serverConfig.hasPriority
                && Voice.serverConfig.priorityDistance > Voice.serverConfig.maxDistance;

        if(!Voice.speakingPriority && priorityPressed) {
            Voice.speakingPriority = true;
        } else if(Voice.speaking && Voice.speakingPriority && !priorityPressed) {
            Voice.speakingPriority = false;
        }

        int blockSize = frameSize;
        byte[] normBuffer = new byte[blockSize];

        mic.start();

        int read = mic.read(normBuffer, 0, blockSize);
        if (read == -1) {
            return;
        }

        Utils.adjustVolumeMono(normBuffer, (float) Voice.config.microphoneAmplification);

        int offset = Utils.getActivationOffset(normBuffer, Voice.config.voiceActivationThreshold);
        if(offset > 0) {
            this.lastSpeak = System.currentTimeMillis();

            if(!Voice.speaking) {
                Voice.speaking = true;
                if(this.lastBuffer != null) {
                    this.sendPacket(lastBuffer);
                }
                this.sendPacket(normBuffer);
                return;
            }
        } else if(Voice.speaking && System.currentTimeMillis() - lastSpeak > 500L) {
            Voice.speaking = false;
            Voice.speakingPriority = false;
//            mic.stop();
//            mic.flush();

            this.sendPacket(normBuffer);
            this.sendEndPacket();
            return;
        }

        if(Voice.speaking) {
            this.sendPacket(normBuffer);
        }

        this.lastBuffer = normBuffer;
    }

    private void pushToTalk() {
        boolean priorityPressed = ClientInputEvent.priorityPushToTalkPressed && Voice.serverConfig.hasPriority
                && Voice.serverConfig.priorityDistance > Voice.serverConfig.maxDistance;
        boolean pushToTalkPressed = ClientInputEvent.pushToTalkPressed || priorityPressed;

        if(!Voice.speakingPriority && priorityPressed) {
            Voice.speakingPriority = true;
        } else if(Voice.speaking && Voice.speakingPriority && !priorityPressed && pushToTalkPressed) {
            Voice.speakingPriority = false;
        }

        if(pushToTalkPressed && !Voice.speaking) {
            Voice.speaking = true;
            mic.flush();
            this.lastSpeak = System.currentTimeMillis();
        } else if(pushToTalkPressed) {
            this.lastSpeak = System.currentTimeMillis();
        } else if(Voice.speaking && System.currentTimeMillis() - lastSpeak > 350L) {
            Voice.speaking = false;
            Voice.speakingPriority = false;
            mic.stop();
            mic.flush();

            this.sendEndPacket();
            return;
        }

        if(!Voice.speaking) {
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

        Utils.adjustVolumeMono(normBuffer, (float) Voice.config.microphoneAmplification);
        this.sendPacket(normBuffer);
    }

    private void sendPacket(byte[] raw) {
        if(Voice.socketUDP == null) {
            this.running = false;
            return;
        }

        try {
            if(!Voice.socketUDP.isClosed()) {
                Voice.socketUDP.send(new VoiceClientPacket(encoder.encode(raw), sequenceNumber++,
                        Voice.speakingPriority
                                ? Voice.serverConfig.priorityDistance
                                : Voice.serverConfig.distance));
            } else {
                this.running = false;
            }
        } catch (IOException ignored) {
            this.running = false;
        }
    }

    private void sendEndPacket() {
        if(Voice.socketUDP == null) {
            this.running = false;
            return;
        }

        if(!Voice.socketUDP.isClosed()) {
            try {
                Voice.socketUDP.send(new VoiceEndClientPacket());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        if(this.thread != null) {
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
                } catch (InterruptedException ignored) {}
            }

            if(this.thread != null) {
                if (!this.thread.isInterrupted()) {
                    this.thread.interrupt();
                }
            }
        });
    }
}
