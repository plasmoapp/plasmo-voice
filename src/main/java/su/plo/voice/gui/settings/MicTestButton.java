package su.plo.voice.gui.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.text.TranslationTextComponent;
import su.plo.voice.Voice;
import su.plo.voice.sound.DataLines;
import su.plo.voice.sound.OpusDecoder;
import su.plo.voice.sound.OpusEncoder;
import su.plo.voice.sound.Recorder;
import su.plo.voice.utils.Utils;
import tomp2p.opuswrapper.Opus;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class MicTestButton extends AbstractButton {
    public static boolean micActive;
    private final MicListener micListener;
    private VoiceThread thread;

    public MicTestButton(int x, int y, int width, int height, MicListener micListener) {
        super(x, y, width, height, new TranslationTextComponent("gui.plasmo_voice.mic_test_off"));

        micActive = false;
        this.micListener = micListener;
        updateText();
    }

    @Override
    public void onPress() {
        if(micActive) {
            if(thread != null) {
                thread.close();
            }
            micActive = false;
        } else {
            try {
                thread = new VoiceThread();
                thread.start();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
            micActive = true;
        }
        this.updateText();
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y, float partialTicks) {
        super.render(matrixStack, x, y, partialTicks);
        if (thread != null) {
            thread.updateLastRender();
        }
    }

    private void updateText() {
        if (!visible) {
            setMessage(new TranslationTextComponent("gui.plasmo_voice.mic_test_unavailable"));
            return;
        }
        if (micActive) {
            setMessage(new TranslationTextComponent("gui.plasmo_voice.mic_test_off"));
        } else {
            setMessage(new TranslationTextComponent("gui.plasmo_voice.mic_test_on"));
        }
    }

    private class VoiceThread extends Thread {
        private final TargetDataLine mic;
        private final SourceDataLine speaker;
        private final FloatControl gainControl;
        private boolean running;
        private long lastRender;

        private final OpusEncoder encoder;
        private final OpusDecoder decoder;

        public VoiceThread() throws LineUnavailableException {
            this.running = true;
            setDaemon(true);

            mic = DataLines.getMicrophone();
            if (mic == null) {
                throw new LineUnavailableException("No microphone");
            }
            speaker = DataLines.getSpeaker();
            if (speaker == null) {
                throw new LineUnavailableException("No speaker");
            }
            speaker.open(Recorder.format);
            speaker.start();

            gainControl = (FloatControl) speaker.getControl(FloatControl.Type.MASTER_GAIN);

            encoder = new OpusEncoder(Recorder.sampleRate, Recorder.frameSize, Recorder.mtuSize, Opus.OPUS_APPLICATION_VOIP);
            decoder = new OpusDecoder(Recorder.sampleRate, Recorder.frameSize, Recorder.mtuSize);

            updateLastRender();
        }

        public void updateLastRender() {
            lastRender = System.currentTimeMillis();
        }

        @Override
        public void run() {
            if(Voice.recorder.thread != null) {
                Voice.recorder.running = false;
                synchronized (Voice.recorder) {
                    try {
                        Voice.recorder.wait();
                    } catch (InterruptedException ignored) {}
                }
            }

            try {
                mic.open(Recorder.format);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
            mic.start();

            int blockSize = Recorder.frameSize;
            byte[] normBuffer = new byte[blockSize];

            while (running) {
                if (System.currentTimeMillis() - lastRender > 500L) {
                    close();
                    return;
                }

                int read = mic.read(normBuffer, 0, blockSize);

                if (read == -1) {
                    break;
                }

                Utils.adjustVolumeMono(normBuffer, (float) Voice.config.microphoneAmplification);
                micListener.onMicValue(Utils.dbToPerc(Utils.getHighestAudioLevel(normBuffer)));

                byte[] encoded = encoder.encode(normBuffer);

                gainControl.setValue(Math.min(Math.max(Utils.percentageToDB((float) Voice.config.voiceVolume), gainControl.getMinimum()), gainControl.getMaximum()));

                byte[] decoded = decoder.decode(encoded);
                speaker.write(decoded, 0, decoded.length);
            }
        }

        public void close() {
            running = false;
            speaker.stop();
            speaker.flush();
            speaker.close();
            mic.stop();
            mic.flush();
            mic.close();
            micListener.onMicValue(0D);

            Voice.recorder.start();
        }
    }

    public interface MicListener {
        void onMicValue(double perc);
    }
}
