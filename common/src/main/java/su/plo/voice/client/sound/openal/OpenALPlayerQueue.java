package su.plo.voice.client.sound.openal;

import lombok.SneakyThrows;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.openal.EXTThreadLocalContext;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.socket.SocketClientUDPQueue;
import su.plo.voice.client.sound.AbstractSoundQueue;
import su.plo.voice.client.sound.Occlusion;
import su.plo.voice.common.packets.udp.VoiceServerPacket;

import java.util.UUID;

public class OpenALPlayerQueue extends AbstractSoundQueue {
    public OpenALPlayerQueue(UUID from) {
        super(from);
        this.start();
    }

    @SneakyThrows
    @Override
    public void run() {
        EXTThreadLocalContext.alcSetThreadContext(VoiceClient.getSoundEngine().getContextPointer());

        this.source = VoiceClient.getSoundEngine().createSource();
        this.source.setPitch(1.0F);
        this.source.setLooping(false);
        this.source.setRelative(false);
        if (VoiceClient.getClientConfig().directionalSources.get()) {
            this.source.setAngle(VoiceClient.getClientConfig().directionalSourcesAngle.get());
        }
        AlUtil.checkErrors("Create custom source");

        while(!stopped) {
            if (!this.queue.isEmpty()) {
                VoiceServerPacket packet = this.pollQueue();
                if (packet == null) {
                    continue;
                }

                lastPacketTime = System.currentTimeMillis();

                if (packet.getData().length == 0) {
                    lastSequenceNumber = -1L;
                    lastOcclusion = -1;
                    continue;
                }

                if (VoiceClient.getClientConfig().speakerMuted.get() ||
                        VoiceClient.getClientConfig().voiceVolume.get() == 0.0f) {
                    continue;
                }

                if (this.lastSequenceNumber >= 0 && packet.getSequenceNumber() <= this.lastSequenceNumber) {
                    continue;
                }

                if(minecraft.screen instanceof VoiceSettingsScreen screen && screen.getSource() != null) {
                    continue;
                }

                Player player = minecraft.level.getPlayerByUUID(this.from);
                if(player == null) {
                    continue;
                }

                Player clientPlayer = minecraft.player;

                boolean isPriority = packet.getDistance() > VoiceClient.getServerConfig().getMaxDistance();
                float distance = (float) player.position().distanceTo(clientPlayer.position());
                float percentage = (float) VoiceClient.getClientConfig().getPlayerVolume(player.getUUID(), isPriority);

                if (percentage == 0.0F) {
                    continue;
                }

                int maxDistance = packet.getDistance();
                if(distance > maxDistance) {
                    lastSequenceNumber = -1L;
                    lastOcclusion = -1;
                    continue;
                }

                int fadeDistance = isPriority
                        ? maxDistance / VoiceClient.getServerConfig().getPriorityFadeDivisor()
                        : maxDistance / VoiceClient.getServerConfig().getFadeDivisor();

                if(!VoiceClient.getSoundEngine().isSoundPhysics() && VoiceClient.getClientConfig().occlusion.get()) {
                    double occlusion = Occlusion.getOccludedPercent(player.level, clientPlayer, player.position());
                    if(lastOcclusion >= 0) {
                        if(occlusion > lastOcclusion) {
                            lastOcclusion = Math.max(lastOcclusion + 0.05, 0.0D);
                        } else {
                            lastOcclusion = Math.max(lastOcclusion - 0.05, occlusion);
                        }

                        occlusion = lastOcclusion;
                    }
                    percentage *= (float) (1D - occlusion);
                    if(lastOcclusion == -1) {
                        lastOcclusion = occlusion;
                    }
                }

                source.setPosition(player.position());
                source.setDirection(player.getLookAngle());
                source.setVolume(percentage);
                source.prePlay();

                source.setFadeDistance(fadeDistance);
                source.setMaxDistance(maxDistance, 0.95F);


                if (lastSequenceNumber >= 0) {
                    int packetsToCompensate = (int) (packet.getSequenceNumber() - (lastSequenceNumber + 1));
                    if (packetsToCompensate <= 4) {
                        for (int i = 0; i < packetsToCompensate; i++) {
                            this.source.write(opusDecoder.decode(null));
                        }
                    }
                }

                byte[] decoded = opusDecoder.decode(packet.getData());
                if (VoiceClient.getClientConfig().compressor.get()) {
                    decoded = compressor.compress(decoded);
                }

                this.source.write(decoded);

                this.lastSequenceNumber = packet.getSequenceNumber();
            } else {
                if (canClose()) {
                    lastSequenceNumber = -1L;
                    lastOcclusion = -1;
                    SocketClientUDPQueue.talking.remove(this.from);
                    source.pause();
                }

                try {
                    synchronized (this) {
                        this.wait(10L);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if(this.source != null) {
            this.source.close();
        }

        EXTThreadLocalContext.alcSetThreadContext(0L);
    }
}
