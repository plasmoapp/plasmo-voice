package su.plo.voice.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.Perspective;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

// https://github.com/henkelmax/simple-voice-chat/blob/master/src/main/java/de/maxhenkel/voicechat/voice/common/Utils.java
public class Utils {
    public static double dbToPerc(double db) {
        return (db + 127D) / 127D;
    }

    public static double percToDb(double perc) {
        return (perc * 127D) - 127D;
    }

    public static float percentageToDB(float percentage) {
        return (float) (10D * Math.log(percentage));
    }

    public static short bytesToShort(byte b1, byte b2) {
        return (short) (((b2 & 0xFF) << 8) | (b1 & 0xFF));
    }

    public static int getActivationOffset(byte[] samples, double activationLevel) {
        int highestPos = -1;
        for (int i = 0; i < samples.length; i += 100) {
            double level = Utils.calculateAudioLevel(samples, i, Math.min(i + 100, samples.length));
            if (level >= activationLevel) {
                highestPos = i;
            }
        }
        return highestPos;
    }

    public static byte[] adjustVolumeMono(byte[] audio, float volume) {
        for (int i = 0; i < audio.length; i += 2) {
            short audioSample = bytesToShort(audio[i], audio[i + 1]);

            audioSample = (short) (audioSample * volume);

            audio[i] = (byte) audioSample;
            audio[i + 1] = (byte) (audioSample >> 8);

        }
        return audio;
    }

    public static byte[] shortToBytes(short s) {
        return new byte[]{(byte) (s & 0xFF), (byte) ((s >> 8) & 0xFF)};
    }

    public static double getHighestAudioLevel(byte[] samples) {
        double highest = -127D;
        for (int i = 0; i < samples.length; i += 100) {
            double level = Utils.calculateAudioLevel(samples, i, Math.min(i + 100, samples.length));
            if (level > highest) {
                highest = level;
            }
        }
        return highest;
    }

    public static double calculateAudioLevel(byte[] samples, int offset, int length) {
        double rms = 0D; // root mean square (RMS) amplitude

        for (int i = offset; i < length; i += 2) {
            double sample = (double) Utils.bytesToShort(samples[i], samples[i + 1]) / Short.MAX_VALUE;
            rms += sample * sample;
        }

        int sampleCount = length / 2;

        rms = (sampleCount == 0) ? 0 : Math.sqrt(rms / sampleCount);

        double db;

        if (rms > 0D) {
            db = Math.min(Math.max(20D * Math.log10(rms), -127D), 0D);
        } else {
            db = -127D;
        }

        return db;
    }

    public static byte[] convertToStereo(byte[] audio, float volumeLeft, float volumeRight) {
        byte[] stereo = new byte[audio.length * 2];
        for (int i = 0; i < audio.length; i += 2) {
            short audioSample = bytesToShort(audio[i], audio[i + 1]);//(short) (((audio[i + 1] & 0xff) << 8) | (audio[i] & 0xff));
            short left = (short) (audioSample * volumeLeft);
            short right = (short) (audioSample * volumeRight);
            stereo[i * 2] = (byte) left;
            stereo[i * 2 + 1] = (byte) (left >> 8);

            stereo[i * 2 + 2] = (byte) right;
            stereo[i * 2 + 3] = (byte) (right >> 8);
        }
        return stereo;
    }

    public static Pair<Float, Float> getStereoVolume(MinecraftClient minecraft, Vec3d soundPos, int maxDistance) {
        ClientPlayerEntity player = minecraft.player;
        Vec3d playerPos = player.getPos();
        Vec3d d = soundPos.subtract(playerPos).normalize();
        Vec2f diff = new Vec2f((float) d.x, (float) d.z);
        float diffAngle = angle(diff, new Vec2f(-1F, 0F));
        float angle = normalizeAngle(diffAngle - (player.getHeadYaw() % 360F));
        float dif = (float) (Math.abs(playerPos.y - soundPos.y) / maxDistance);


        float rot = angle / 180F;
        float perc = rot;
        if (rot < -0.5F) {
            perc = -(0.5F + (rot + 0.5F));
        } else if (rot > 0.5F) {
            perc = 0.5F - (rot - 0.5F);
        }
        perc = perc * (1 - dif);

        float left = perc < 0F ? Math.abs(perc * 1.4F) + 0.3F : 0.3F;
        float right = perc >= 0F ? (perc * 1.4F) + 0.3F : 0.3F;

        float fill = 1F - Math.max(left, right);
        left += fill;
        right += fill;

        if (minecraft.options.getPerspective().equals(Perspective.THIRD_PERSON_FRONT)) {
            return new ImmutablePair<>(right, left);
        }

        return new ImmutablePair<>(left, right);
    }

    private static float normalizeAngle(float angle) {
        angle = angle % 360F;
        if (angle <= -180F) {
            angle += 360F;
        } else if (angle > 180F) {
            angle -= 360F;
        }
        return angle;
    }

    private static float angle(Vec2f vec1, Vec2f vec2) {
        return (float) Math.toDegrees(Math.atan2(vec1.x * vec2.x + vec1.y * vec2.y, vec1.x * vec2.y - vec1.y * vec2.x));
    }
}
