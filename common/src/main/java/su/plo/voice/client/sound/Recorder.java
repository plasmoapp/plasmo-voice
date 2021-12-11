package su.plo.voice.client.sound;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.sound.capture.AlCaptureDevice;
import su.plo.voice.client.sound.capture.CaptureDevice;
import su.plo.voice.client.sound.capture.JavaxCaptureDevice;
import su.plo.voice.client.sound.openal.CustomSoundEngine;
import su.plo.voice.client.sound.openal.CustomSource;
import su.plo.voice.client.sound.opus.OpusEncoder;
import su.plo.voice.client.utils.AudioUtils;
import su.plo.voice.common.packets.udp.VoiceClientPacket;
import su.plo.voice.common.packets.udp.VoiceEndClientPacket;
import su.plo.voice.rnnoise.Bytes;
import su.plo.voice.rnnoise.Denoiser;
import tomp2p.opuswrapper.Opus;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class Recorder implements Runnable {
    private final Minecraft client = Minecraft.getInstance();

    @Getter
    private static final int mtuSize = 1024;
    @Getter
    private static int sampleRate = 24000;
    @Getter
    private static int frameSize = (sampleRate / 1000) * 2 * 20;
    @Getter
    private static AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);

    @Setter
    @Getter
    private boolean running;
    @Getter
    private boolean available;
    @Getter
    private Thread thread;

    private CaptureDevice microphone;
    private OpusEncoder encoder;

    // RNNoise
    private Denoiser denoiser;
    // Limiter to fix RNNoise clipping
    private final Limiter limiter = new Limiter(-6.0F);

    private int jopusMode;

    private long sequenceNumber = 0L;
    private long lastSpeak;
    private byte[] lastBuffer;

    // test
    private CustomSource source;

    public Recorder() {
        if (VoiceClient.getClientConfig().rnNoise.get()) {
            this.denoiser = new Denoiser();
        }

        VoiceClient.getSoundEngine().onInitialize(engine -> {
            if (engine.isSoundPhysics()) {
                this.source = engine.createSource();
                source.setLooping(false);
                source.setRelative(false);
                source.setReverbOnly(true);
            }
        });

        VoiceClient.getSoundEngine().onClose(() -> source.close());

        jopusMode = Opus.OPUS_APPLICATION_VOIP;
//        if (VoiceClient.getClientConfig().jopusMode.get().equals("audio")) {
//            jopusMode = Opus.OPUS_APPLICATION_AUDIO;
//        } else if (VoiceClient.getClientConfig().jopusMode.get().equals("low-delay")) {
//            jopusMode = Opus.OPUS_APPLICATION_RESTRICTED_LOWDELAY;
//        } else {
//            jopusMode = Opus.OPUS_APPLICATION_VOIP;
//        }
    }

//    public synchronized void updateJopusMode() {
//        if (VoiceClient.getClientConfig().jopusMode.get().equals("audio")) {
//            jopusMode = Opus.OPUS_APPLICATION_AUDIO;
//        } else if (VoiceClient.getClientConfig().jopusMode.get().equals("low-delay")) {
//            jopusMode = Opus.OPUS_APPLICATION_RESTRICTED_LOWDELAY;
//        } else {
//            jopusMode = Opus.OPUS_APPLICATION_VOIP;
//        }
//
//        if (this.encoder != null) {
//            this.encoder.close();
//        }
//        System.out.println(jopusMode);
//        this.encoder = new OpusEncoder(sampleRate, frameSize, mtuSize, jopusMode);
//    }

    public synchronized void toggleRnNoise() {
        if (this.denoiser != null) {
            this.denoiser.close();
            this.denoiser = null;
        } else {
            this.denoiser = new Denoiser();
        }
    }

    /**
     * Method to update sample rate after vreload sent by server
     * @param rate New sample rate
     */
    public void updateSampleRate(int rate) {
        if (rate == Recorder.getSampleRate()) {
            return;
        }

        if (rate != 8000 && rate != 12000 && rate != 24000 && rate != 48000) {
            VoiceClient.LOGGER.info("Incorrect sample rate");
            return;
        }

        if (this.thread != null) {
            this.waitForClose().thenRun(() -> updateSampleRateSync(rate));
        } else {
            updateSampleRateSync(rate);
        }
    }

    private void updateSampleRateSync(int rate) {
        format = new AudioFormat(rate, 16, 1, true, false);
        sampleRate = rate;
        frameSize = (sampleRate / 1000) * 2 * 20;

        if (this.encoder != null) {
            this.encoder.close();
        }
        this.encoder = new OpusEncoder(sampleRate, frameSize, mtuSize, jopusMode);

        if (VoiceClient.isConnected()) {
            this.start();
        }
    }

    /**
     * Interrupt thread, closes capture device and opus encoder
     */
    public void close() {
        this.running = false;
        this.sequenceNumber = 0L;
        this.lastBuffer = null;
        if (this.encoder != null) {
            this.encoder.close();
        }

        if (microphone.isOpen()) {
            microphone.stop();
            microphone.close();
            thread = null;
        }

        synchronized (this) {
            this.notifyAll();
        }
    }

    public void run() {
        if (microphone != null && microphone.isOpen()) {
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }

        this.available = true;

        try {
            // sometimes openal mic doesn't work at all,
            // so I just made old javax capture system
            if (VoiceClient.getClientConfig().javaxCapture.get()) {
                VoiceClient.LOGGER.info("Using javax capture device");
                microphone = new JavaxCaptureDevice();
            } else {
                microphone = new AlCaptureDevice();
            }

            microphone.open();
        } catch (IllegalStateException e) {
            VoiceClient.LOGGER.info("Failed to open OpenAL capture device, falling back to javax capturing");
            if (microphone instanceof AlCaptureDevice) {
                VoiceClient.getClientConfig().javaxCapture.set(true);

                microphone = new JavaxCaptureDevice();

                try {
                    microphone.open();
                } catch (IllegalStateException ignored) {
                    VoiceClient.getClientConfig().javaxCapture.set(false);
                    VoiceClient.LOGGER.info("Capture device not available on this system");
                    this.available = false;
                    return;
                }
            }
        }

        if (this.encoder == null || this.encoder.isClosed()) {
            this.encoder = new OpusEncoder(sampleRate, frameSize, mtuSize, jopusMode);
        }

        this.running = true;
        while (running) {
            final LocalPlayer player = client.player;
            if (player == null) {
                this.running = false;
                break;
            }

            byte[] normBuffer = readBuffer();

            if (!VoiceClient.isConnected()) {
                this.running = false;
                break;
            }

            // muted
            if ((VoiceClient.getServerConfig().getMuted().containsKey(player.getUUID()) || VoiceClient.getClientConfig().microphoneMuted.get()
                    || VoiceClient.getClientConfig().speakerMuted.get())) {
                VoiceClient.setSpeaking(false);
                VoiceClient.setSpeakingPriority(false);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                continue;
            }

            if (normBuffer == null) {
                continue;
            }

            if (!VoiceClient.getClientConfig().voiceActivation.get() ||
                    VoiceClient.getServerConfig().isVoiceActivationDisabled()) {
                pushToTalk(normBuffer);
            } else {
                voiceActivation(normBuffer);
            }
        }

        this.close();
    }

    private void voiceActivation(byte[] normBuffer) {
        boolean priorityPressed = VoiceClient.getClientConfig().keyBindings.priorityPushToTalk.get().isPressed()
                && VoiceClient.getServerConfig().isPriority()
                && VoiceClient.getServerConfig().getPriorityDistance() > VoiceClient.getServerConfig().getMaxDistance();

        if (VoiceClient.isMicrophoneLoopback()) {
            if (VoiceClient.isSpeaking()) {
                VoiceClient.setSpeaking(false);
                VoiceClient.setSpeakingPriority(false);
            }
            return;
        }

        boolean activated = System.currentTimeMillis() - lastSpeak <= 500L;
        int offset = AudioUtils.getActivationOffset(normBuffer, VoiceClient.getClientConfig().voiceActivationThreshold.get());

        if (!VoiceClient.isSpeakingPriority() && priorityPressed) {
            VoiceClient.setSpeakingPriority(true);
        }

        if (priorityPressed && !VoiceClient.isSpeaking()) {
            VoiceClient.setSpeaking(true);
            this.lastSpeak = System.currentTimeMillis();
        } else if (priorityPressed && !VoiceClient.isMicrophoneLoopback()) {
            this.lastSpeak = System.currentTimeMillis();
        } else if (VoiceClient.isSpeakingPriority() &&
                !priorityPressed &&
                (System.currentTimeMillis() - lastSpeak > 350L || VoiceClient.isMicrophoneLoopback()) &&
                offset <= 0) {
            VoiceClient.setSpeaking(false);
            VoiceClient.setSpeakingPriority(false);

            this.sendEndPacket();
            return;
        } else {
            if (offset > 0 || activated) {
                if (offset > 0) {
                    this.lastSpeak = System.currentTimeMillis();

                    if (VoiceClient.isSpeakingPriority() &&
                            !priorityPressed) {
                        VoiceClient.setSpeakingPriority(false);
                    }
                }

                if (!VoiceClient.isSpeaking()) {
                    VoiceClient.setSpeaking(true);
                    if (this.lastBuffer != null) {
                        this.sendPacket(lastBuffer);
                    }
                    this.sendPacket(normBuffer);
                    return;
                }
            } else if (VoiceClient.isSpeaking()) {
                VoiceClient.setSpeaking(false);
                VoiceClient.setSpeakingPriority(false);

                this.sendPacket(normBuffer);
                this.sendEndPacket();
                return;
            }
        }

        if (VoiceClient.isSpeaking()) {
            this.sendPacket(normBuffer);
        }

        this.lastBuffer = normBuffer;
    }

    private void pushToTalk(byte[] normBuffer) {
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
            this.lastSpeak = System.currentTimeMillis();
        } else if (pushToTalkPressed && !VoiceClient.isMicrophoneLoopback()) {
            this.lastSpeak = System.currentTimeMillis();
        } else if (VoiceClient.isSpeaking() && (System.currentTimeMillis() - lastSpeak > 350L || VoiceClient.isMicrophoneLoopback())) {
            VoiceClient.setSpeaking(false);
            VoiceClient.setSpeakingPriority(false);

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

        this.sendPacket(normBuffer);
    }

    /**
     * Capture samples sync
     * @return captured samples
     */
    private synchronized byte[] readBuffer() {
        if (this.encoder == null || this.encoder.isClosed()) {
            return null;
        }

        microphone.start();
        byte[] normBuffer = microphone.read(frameSize);
        if (normBuffer == null) {
            return null;
        }

        if (this.denoiser != null) {
            float[] floats = AudioUtils.bytesToFloats(normBuffer);
            limiter.limit(floats);
            floats = Bytes.toFloatArray(AudioUtils.floatsToBytes(floats));

            normBuffer = Bytes.toByteArray(this.denoiser.process(floats));
        }

        if (client.screen instanceof VoiceSettingsScreen screen) {
            screen.setMicrophoneValue(normBuffer);
        }

        return normBuffer;
    }

    private void sendPacket(final byte[] raw) {
        if (VoiceClient.isMicrophoneLoopback()) {
            return;
        }

        if (CustomSoundEngine.soundPhysicsPlaySoundNew != null) {
            VoiceClient.getSoundEngine().runInContext(() -> {
                Vec3 pos = client.player.position();

                source.setMaxDistance(VoiceClient.getServerConfig().getDistance(), 0.95F);
                source.setPosition(pos);
                source.setVolume(VoiceClient.getClientConfig().voiceVolume.get().floatValue());
                source.write(raw);
            });
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

        this.encoder.reset();

        if (!VoiceClient.socketUDP.isClosed()) {
            try {
                VoiceClient.socketUDP.send(new VoiceEndClientPacket());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Starts capture thread
     */
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

    /**
     * Waiting capture device to close
     */
    // todo is it necessary at all?
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
