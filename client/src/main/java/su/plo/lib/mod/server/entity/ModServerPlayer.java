package su.plo.lib.mod.server.entity;

import com.google.common.collect.Sets;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.server.chat.ServerTextConverter;
import su.plo.lib.api.server.entity.MinecraftServerEntity;
import su.plo.lib.api.server.entity.MinecraftServerPlayerEntity;
import su.plo.lib.api.server.permission.PermissionTristate;
import su.plo.lib.api.server.world.MinecraftServerWorld;
import su.plo.lib.api.server.world.ServerPos3d;
import su.plo.lib.mod.client.texture.ResourceCache;
import su.plo.lib.mod.entity.ModPlayer;
import su.plo.lib.mod.server.ModServerLib;
import su.plo.voice.proto.data.player.MinecraftGameProfile;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static su.plo.lib.mod.server.utils.ServerPlayerKt.serverLevel;

//#if FABRIC
//#if MC>=12005
//$$ import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//$$ import su.plo.voice.codec.PacketServicePayload;
//$$ import su.plo.voice.codec.PacketTcpPayload;
//$$ import su.plo.voice.proto.packets.Packet;
//$$ import su.plo.voice.proto.packets.PacketHandler;
//$$ import su.plo.voice.proto.packets.tcp.PacketTcpCodec;
//$$ import su.plo.voice.server.ModVoiceServer;
//$$
//$$ import java.io.IOException;
//$$ import com.google.common.io.ByteStreams;
//#endif
//#endif

//#if MC>=12002
//$$ import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//$$ import net.minecraft.resources.ResourceLocation;
//#endif

public final class ModServerPlayer
        extends ModPlayer<ServerPlayer>
        implements MinecraftServerPlayerEntity {

    private final ModServerLib minecraftServer;
    private final ServerTextConverter<Component> textConverter;
    private final ResourceCache resources;
    private final Set<String> registeredChannels = Sets.newCopyOnWriteArraySet();

    @Getter
    @Setter
    private String language = "en_us";
    private MinecraftServerEntity spectatorTarget;

    public ModServerPlayer(@NotNull ModServerLib minecraftServer,
                           @NotNull ServerTextConverter<Component> textConverter,
                           @NotNull ResourceCache resources,
                           @NotNull ServerPlayer player) {
        super(player);

        this.minecraftServer = minecraftServer;
        this.textConverter = textConverter;
        this.resources = resources;
    }

    @Override
    public @NotNull ServerPos3d getServerPosition() {
        return new ServerPos3d(
                minecraftServer.getWorld(serverLevel(instance)),
                instance.position().x(),
                instance.position().y(),
                instance.position().z(),
                instance.getXRot(),
                instance.getYRot()
        );
    }

    @Override
    public @NotNull ServerPos3d getServerPosition(@NotNull ServerPos3d position) {
        position.setWorld(minecraftServer.getWorld(serverLevel(instance)));

        position.setX(instance.position().x());
        position.setY(instance.position().y());
        position.setZ(instance.position().z());

        position.setYaw(instance.getXRot());
        position.setPitch(instance.getYRot());

        return position;
    }

    @Override
    public @NotNull MinecraftServerWorld getWorld() {
        return minecraftServer.getWorld(serverLevel(instance));
    }

    @Override
    public boolean isOnline() {
        return !instance.hasDisconnected();
    }

    @Override
    public @NotNull MinecraftGameProfile getGameProfile() {
        return minecraftServer.getGameProfile(instance.getUUID())
                .orElseThrow(() -> new IllegalStateException("Game profile not found"));
    }

    @Override
    public void sendPacket(@NotNull String channel, byte[] data) {
        //#if MC>=12005
        //$$ try {
        //$$     switch (channel) {
        //$$         case ModVoiceServer.CHANNEL_STRING:
        //$$             // todo: this is actually unnecessary re-encoding, but I don't think anything can be done without rewriting good chunk of code
        //$$             //  I'll try to fix it in slib and PV 2.1.x
        //$$             Packet<PacketHandler> packet = PacketTcpCodec.decode(ByteStreams.newDataInput(data))
        //$$                     .orElseThrow(() -> new IOException("data is not Plasmo Voice packet"));
        //$$
        //$$             instance.connection.send(
        //$$                     ServerPlayNetworking.createS2CPacket(
        //$$                             new PacketTcpPayload(packet)
        //$$                     )
        //$$             );
        //$$
        //$$             break;
        //$$
        //$$         case ModVoiceServer.SERVICE_CHANNEL_STRING:
        //$$
        //$$             instance.connection.send(
        //$$                     ServerPlayNetworking.createS2CPacket(
        //$$                             new PacketServicePayload(data)
        //$$                     )
        //$$             );
        //$$
        //$$             break;
        //$$
        //$$         default:
        //$$             throw new IllegalArgumentException("This method is now not supported for custom channels due to 1.20.5 changes. It'll be fixed in PV 2.1.x");
        //$$     }
        //$$ } catch (IOException e) {
        //$$     e.printStackTrace();
        //$$     return;
        //$$ }
        //#else
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeResourceLocation(resources.getLocation(channel));
        buf.writeBytes(data);

        instance.connection.send(new ClientboundCustomPayloadPacket(buf));
        //#endif
    }

    @Override
    public void kick(@NotNull MinecraftTextComponent reason) {
        instance.connection.disconnect(textConverter.convert(this, reason));
    }

    @Override
    public void sendMessage(@NotNull MinecraftTextComponent text) {
        instance.sendSystemMessage(textConverter.convert(this, text));
    }

    @Override
    public void sendActionBar(@NotNull MinecraftTextComponent text) {
        instance.connection.send(new ClientboundSetActionBarTextPacket(
                textConverter.convert(this, text)
        ));
    }

    @Override
    public boolean canSee(@NotNull MinecraftServerPlayerEntity player) {
        ServerPlayer serverPlayer = player.getInstance();

        if (serverPlayer.isSpectator()) {
            return instance.isSpectator();
        }

        return true;
    }

    @Override
    public Collection<String> getRegisteredChannels() {
        return registeredChannels;
    }

    @Override
    public Optional<MinecraftServerEntity> getSpectatorTarget() {
        if (instance.getCamera() == instance) {
            this.spectatorTarget = null;
        } else if (spectatorTarget == null || !instance.getCamera().equals(spectatorTarget.getInstance())) {
            this.spectatorTarget = minecraftServer.getEntity(instance.getCamera());
        }

        return Optional.ofNullable(spectatorTarget);
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        if (minecraftServer.getPermissions() == null) return false;

        return minecraftServer.getPermissions().hasPermission(instance, permission);
    }

    @Override
    public @NotNull PermissionTristate getPermission(@NotNull String permission) {
        if (minecraftServer.getPermissions() == null) return PermissionTristate.UNDEFINED;

        return minecraftServer.getPermissions().getPermission(instance, permission);
    }

    public void addChannel(@NotNull String channel) {
        registeredChannels.add(channel);
    }

    public void removeChannel(@NotNull String channel) {
        registeredChannels.remove(channel);
    }
}
