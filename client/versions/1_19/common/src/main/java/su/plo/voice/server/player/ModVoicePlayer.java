package su.plo.voice.server.player;

import io.netty.buffer.Unpooled;
import lombok.ToString;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.event.connection.TcpPacketSendEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.pos.ServerPos3d;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;
import su.plo.voice.server.ModVoiceServer;

import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@ToString(of = "player")
public final class ModVoicePlayer extends BaseVoicePlayer {

    private final ServerPlayer player;

    public ModVoicePlayer(@NotNull PlasmoVoiceServer voiceServer,
                          @NotNull PermissionSupplier permissions,
                          @NotNull ServerPlayer player) {
        super(voiceServer, permissions);
        this.player = checkNotNull(player, "player");
    }

    @Override
    public int getId() {
        return player.getId();
    }

    @Override
    public @NotNull UUID getUUID() {
        return player.getUUID();
    }

    @Override
    public <T> T getObject() {
        return (T) player;
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        byte[] encoded = PacketTcpCodec.encode(packet);

        TcpPacketSendEvent event = new TcpPacketSendEvent(this, packet);
        voiceServer.getEventBus().call(event);
        if (event.isCancelled()) return;

        player.connection.send(new ClientboundCustomPayloadPacket(
                ModVoiceServer.CHANNEL,
                new FriendlyByteBuf(Unpooled.wrappedBuffer(encoded))
        ));

        LogManager.getLogger().info("Channel packet {} sent to {}", packet, this);
    }

    @Override
    public void sendTranslatableMessage(@NotNull String translatable, Object... args) {
        player.sendSystemMessage(Component.translatable(translatable, args));
    }

    @Override
    public void sendMessage(@NotNull String message) {
        player.sendSystemMessage(Component.literal(message));
    }

    @Override
    public @NotNull ServerPos3d getPosition() {
        return new ServerPos3d(
                voiceServer.getWorldManager().wrap(player.getLevel()),
                player.position().x(),
                player.position().y(),
                player.position().z(),
                player.getXRot(),
                player.getYRot()
        );
    }

    @Override
    public @NotNull ServerPos3d getPosition(@NotNull ServerPos3d position) {
        position.setWorld(voiceServer.getWorldManager().wrap(player.getLevel()));

        position.setX(player.position().x());
        position.setY(player.position().y());
        position.setZ(player.position().z());

        position.setYaw(player.getXRot());
        position.setPitch(player.getYRot());

        return position;
    }

    @Override
    public boolean canSee(@NotNull VoicePlayer player) {
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return permissions.hasPermission(player, permission);
    }

    @Override
    public @NotNull String getNick() {
        return player.getGameProfile().getName();
    }

    @Override
    public boolean hasVoiceChat() {
        return voiceServer.getUdpConnectionManager()
                .getConnectionByUUID(getUUID())
                .isPresent();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object != null && this.getClass() == object.getClass()) {
            ModVoicePlayer world = (ModVoicePlayer) object;
            return this.player == world.player;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.player);
    }
}
