package su.plo.voice.server.audio.source;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.server.world.ServerPos3d;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.audio.source.ServerAudioSource;
import su.plo.voice.api.server.event.audio.source.ServerSourceAudioPacketEvent;
import su.plo.voice.api.server.event.audio.source.ServerSourcePacketEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.socket.UdpConnection;
import su.plo.voice.proto.data.audio.source.SourceInfo;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.clientbound.SourceInfoPacket;
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public abstract class BaseServerSource<S extends SourceInfo> implements ServerAudioSource<S> {

    protected final PlasmoVoiceServer voiceServer;
    @Getter
    protected final AddonContainer addon;
    @Getter
    protected final UUID id;
    protected final String codec;

    @Getter
    protected @NotNull ServerSourceLine line;
    @Getter
    protected boolean iconVisible = true;
    @Setter
    protected int angle;
    protected boolean stereo;

    protected final AtomicBoolean dirty = new AtomicBoolean(true);
    protected final AtomicInteger state = new AtomicInteger(1);

    private final List<Predicate<VoicePlayer>> filters = new CopyOnWriteArrayList<>();
    private final ServerPos3d playerPosition = new ServerPos3d();

    public BaseServerSource(@NotNull PlasmoVoiceServer voiceServer,
                            @NotNull AddonContainer addon,
                            @NotNull UUID id,
                            @NotNull ServerSourceLine line,
                            @Nullable String codec,
                            boolean stereo) {
        this.voiceServer = voiceServer;
        this.addon = addon;
        this.id = id;
        this.line = line;
        this.codec = codec;
        this.stereo = stereo;
    }

    @Override
    public int getState() {
        return state.get();
    }

    @Override
    public synchronized void setLine(@NotNull ServerSourceLine line) {
        if (!this.line.equals(line)) {
            this.line = line;
            setDirty();
            increaseSourceState();
        }
    }

    @Override
    public synchronized void setStereo(boolean stereo) {
        if (this.stereo != stereo) {
            this.stereo = stereo;
            setDirty();
            increaseSourceState();
        }
    }

    @Override
    public synchronized void setIconVisible(boolean visible) {
        if (this.iconVisible != visible) {
            this.iconVisible = visible;
            setDirty();
            increaseSourceState();
        }
    }

    @Override
    public void addFilter(Predicate<VoicePlayer> filter) {
        if (filters.contains(filter)) throw new IllegalArgumentException("Filter already exist");
        filters.add(filter);
    }

    @Override
    public void removeFilter(Predicate<VoicePlayer> filter) {
        filters.remove(filter);
    }

    @Override
    public void sendAudioPacket(SourceAudioPacket packet, short distance) {
        ServerSourceAudioPacketEvent event = new ServerSourceAudioPacketEvent(this, packet, distance);
        voiceServer.getEventBus().call(event);
        if (event.isCancelled()) return;

        distance = event.getDistance();

        packet.setSourceState((byte) state.get());

        if (dirty.compareAndSet(true, false))
            sendPacket(new SourceInfoPacket(getInfo()), distance);

        distance *= 2;

        ServerPos3d sourcePosition = getPosition();
        double distanceSquared = distance * distance;

        L:
        for (UdpConnection connection : voiceServer.getUdpConnectionManager().getConnections()) {
            for (Predicate<VoicePlayer> filter : filters) {
                if (!filter.test(connection.getPlayer())) continue L;
            }

            connection.getPlayer().getInstance().getServerPosition(playerPosition);
            if (sourcePosition.distanceSquared(playerPosition) <= distanceSquared) {
                connection.sendPacket(packet);
            }
        }
    }

    @Override
    public void sendPacket(Packet<?> packet, short distance) {
        ServerSourcePacketEvent event = new ServerSourcePacketEvent(this, packet, distance);
        voiceServer.getEventBus().call(event);
        if (event.isCancelled()) return;

        distance = (short) (event.getDistance() * 2);

        ServerPos3d sourcePosition = getPosition();
        double distanceSquared = distance * distance;

        L:
        for (UdpConnection connection : voiceServer.getUdpConnectionManager().getConnections()) {
            for (Predicate<VoicePlayer> filter : filters) {
                if (!filter.test(connection.getPlayer())) continue L;
            }

            connection.getPlayer().getInstance().getServerPosition(playerPosition);
            if (sourcePosition.distanceSquared(playerPosition) <= distanceSquared) {
                connection.getPlayer().sendPacket(packet);
            }
        }
    }

    @Override
    public void setDirty() {
        dirty.set(true);
    }

    protected void increaseSourceState() {
        state.updateAndGet((operand) -> {
            int value = operand + 1;
            return value > Byte.MAX_VALUE ? Byte.MIN_VALUE : value;
        });
    }
}
