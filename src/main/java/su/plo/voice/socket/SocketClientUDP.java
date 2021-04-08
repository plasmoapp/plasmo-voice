package su.plo.voice.socket;

import su.plo.voice.client.VoiceClient;
import su.plo.voice.common.packets.Packet;
import su.plo.voice.common.packets.udp.PacketUDP;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class SocketClientUDP extends Thread {
    private final InetAddress addr;
    public final int port;
    private final DatagramSocket socket;
    public boolean authorized;
    private final SocketClientAuth auth;
    private final SocketClientUDPQueue clientQueue;
    public final SocketClientPing ping;

    public long keepAlive;

    public SocketClientUDP() throws IOException {
        this.addr = InetAddress.getByName(VoiceClient.serverConfig.ip);
        this.port = VoiceClient.serverConfig.port;
        this.socket = new DatagramSocket();
        this.socket.setTrafficClass(0x04); // IPTOS_RELIABILITY

        this.keepAlive = System.currentTimeMillis();

        this.auth = new SocketClientAuth(this);
        this.auth.start();

        this.clientQueue = new SocketClientUDPQueue(this);
        this.clientQueue.start();

        ping = new SocketClientPing(this);
        ping.start();
    }

    public void checkTimeout() {
        if (System.currentTimeMillis() - this.keepAlive > 7000L) {
            this.ping.timedOut = true;
        }

        if (System.currentTimeMillis() - this.keepAlive > 30000L) {
            this.ping.timedOut = true;
            VoiceClient.LOGGER.info("UDP timed out");
            VoiceClient.disconnect();
        }
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public void close() {
        VoiceClient.LOGGER.info("UDP closed");

        socket.close();
        if(this.auth != null) {
            if(!this.auth.isInterrupted()) {
                this.auth.interrupt();
            }
        }

        if(this.ping != null) {
            if(!this.ping.isInterrupted()) {
                this.ping.interrupt();
            }
        }

        if(this.clientQueue != null) {
            this.clientQueue.interrupt();
        }
    }

    public void send(Packet packet) throws IOException {
        byte[] data = PacketUDP.write(packet);
        socket.send(new DatagramPacket(data, data.length, addr, port));
    }

    @Override
    public void run() {
        try {
            while (!this.socket.isClosed()) {
                this.clientQueue.queue.add(PacketUDP.read(this.socket));

                synchronized (this.clientQueue) {
                    this.clientQueue.notify();
                }
            }
        } catch (SocketException e) {
            if(!e.getMessage().equals("socket closed")) {
                e.printStackTrace();
            }
        } catch (IOException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
