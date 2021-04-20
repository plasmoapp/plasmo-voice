package su.plo.voice.socket;

import su.plo.voice.client.VoiceClient;
import su.plo.voice.sound.ThreadSoundQueue;

public class SocketClientPing extends Thread {
    private final SocketClientUDP socketUDP;
    public boolean timedOut = false;

    public SocketClientPing(SocketClientUDP socketUDP) {
        this.socketUDP = socketUDP;
    }

    @Override
    public void run() {
        VoiceClient.LOGGER.info("Start ping");

        while(!this.socketUDP.isClosed()) {
            try {
                SocketClientUDPQueue.audioChannels.values().stream().filter(ThreadSoundQueue::canKill).forEach(ThreadSoundQueue::closeAndKill);
                SocketClientUDPQueue.audioChannels.entrySet().removeIf(entry -> entry.getValue().isClosed());

                this.socketUDP.checkTimeout();
                Thread.sleep(1000L);
            } catch (InterruptedException ignored) {}
        }
    }
}
