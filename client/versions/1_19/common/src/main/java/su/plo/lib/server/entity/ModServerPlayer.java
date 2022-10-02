package su.plo.lib.server.entity;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.chat.TextComponent;
import su.plo.lib.chat.TextConverter;
import su.plo.lib.client.texture.ResourceCache;
import su.plo.lib.entity.ModPlayer;
import su.plo.lib.server.MinecraftServerLib;
import su.plo.lib.server.world.MinecraftServerWorld;
import su.plo.lib.server.world.ServerPos3d;
import su.plo.voice.server.player.PermissionSupplier;

public final class ModServerPlayer extends ModPlayer<ServerPlayer> implements MinecraftServerPlayer {

    private final MinecraftServerLib minecraftServer;
    private final TextConverter<Component> textConverter;
    private final PermissionSupplier permissions;
    private final ResourceCache resources;

    public ModServerPlayer(@NotNull MinecraftServerLib minecraftServer,
                           @NotNull TextConverter<Component> textConverter,
                           @NotNull PermissionSupplier permissions,
                           @NotNull ResourceCache resources,
                           @NotNull ServerPlayer player) {
        super(player);

        this.minecraftServer = minecraftServer;
        this.textConverter = textConverter;
        this.permissions = permissions;
        this.resources = resources;
    }

    @Override
    public @NotNull ServerPos3d getServerPosition() {
        return new ServerPos3d(
                minecraftServer.getWorld(instance.getLevel()),
                instance.position().x(),
                instance.position().y(),
                instance.position().z(),
                instance.getXRot(),
                instance.getYRot()
        );
    }

    @Override
    public @NotNull ServerPos3d getServerPosition(@NotNull ServerPos3d position) {
        position.setWorld(minecraftServer.getWorld(instance.getLevel()));

        position.setX(instance.position().x());
        position.setY(instance.position().y());
        position.setZ(instance.position().z());

        position.setYaw(instance.getXRot());
        position.setPitch(instance.getYRot());

        return position;
    }

    @Override
    public @NotNull MinecraftServerWorld getWorld() {
        return minecraftServer.getWorld(instance.getLevel());
    }

    @Override
    public void sendPacket(@NotNull String channel, byte[] data) {
        instance.connection.send(new ClientboundCustomPayloadPacket(
                resources.getLocation(channel),
                new FriendlyByteBuf(Unpooled.wrappedBuffer(data))
        ));
    }

    @Override
    public void sendMessage(@NotNull TextComponent text) {
        instance.sendSystemMessage(textConverter.convert(text));
    }

    @Override
    public boolean canSee(@NotNull MinecraftServerPlayer player) {
        return !player.isInvisibleTo(player);
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return permissions.hasPermission(instance, permission);
    }
}
