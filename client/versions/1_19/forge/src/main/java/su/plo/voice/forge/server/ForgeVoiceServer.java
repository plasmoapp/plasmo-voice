package su.plo.voice.forge.server;

import io.netty.util.AsciiString;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.event.EventNetworkChannel;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.permission.PermissionDefault;
import su.plo.lib.api.server.permission.PermissionTristate;
import su.plo.voice.forge.server.connection.ForgeServerChannelHandler;
import su.plo.voice.mod.server.ModVoiceServer;
import su.plo.voice.server.event.player.PlayerJoinEvent;
import su.plo.voice.server.event.player.PlayerQuitEvent;
import su.plo.voice.server.player.PermissionSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ForgeVoiceServer extends ModVoiceServer<ForgeServerChannelHandler> {

    private EventNetworkChannel channel;

    public void onInitialize(EventNetworkChannel channel) {
        this.channel = channel;
    }

    @Override
    public @NotNull String getVersion() {
        return ModList.get().getModFileById("plasmovoice").versionString();
    }

    @Override
    protected PermissionSupplier createPermissionSupplier() {
        return new PermissionSupplier() {
            @Override
            public boolean hasPermission(@NotNull Object player, @NotNull String permission) {
                if (!(player instanceof ServerPlayer serverPlayer))
                    throw new IllegalArgumentException("player is not " + ServerPlayer.class);

                PermissionDefault permissionDefault = minecraftServerLib.getPermissionsManager().getPermissionDefault(permission);
                boolean isOp = server.getPlayerList().isOp(serverPlayer.getGameProfile());

                return getPermission(serverPlayer, permission).booleanValue(permissionDefault.getValue(isOp));
            }

            @Override
            public @NotNull PermissionTristate getPermission(@NotNull Object player, @NotNull String permission) {
                if (!(player instanceof ServerPlayer serverPlayer))
                    throw new IllegalArgumentException("player is not " + ServerPlayer.class);

                Optional<PermissionNode<?>> permissionNode = PermissionAPI.getRegisteredNodes().stream()
                        .filter((node) -> node.getNodeName().equals(permission))
                        .findAny();

                if (permissionNode.isEmpty()) return PermissionTristate.UNDEFINED;

                Boolean value = (Boolean) permissionNode.get().getDefaultResolver().resolve(serverPlayer, serverPlayer.getUUID());
                if (value == null) return PermissionTristate.UNDEFINED;

                return value ? PermissionTristate.TRUE : PermissionTristate.FALSE;
            }
        };
    }



    @Override
    protected ForgeServerChannelHandler createChannelHandler() {
        ForgeServerChannelHandler handler = new ForgeServerChannelHandler(this, channel);
        MinecraftForge.EVENT_BUS.register(handler);
        return handler;
    }

    @SubscribeEvent
    public void onChannelRegister(@NotNull NetworkEvent.ChannelRegistrationChangeEvent event) {
        System.out.println(event.getRegistrationChangeType());
        if (event.getRegistrationChangeType() != NetworkEvent.RegistrationChangeType.REGISTER) return;

        FriendlyByteBuf buf = event.getPayload();

        List<ResourceLocation> ids = new ArrayList<>();
        StringBuilder active = new StringBuilder();

        while (buf.isReadable()) {
            byte b = buf.readByte();

            if (b != 0) {
                active.append(AsciiString.b2c(b));
            } else {
                try {
                    ids.add(new ResourceLocation(active.toString()));
                } catch (ResourceLocationException ex) {
                    continue;
                }
                active = new StringBuilder();
            }
        }

        System.out.println(ids);
    }

    @SubscribeEvent
    public void onServerStart(ServerStartedEvent event) {
        onInitialize(event.getServer());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        onShutdown(event.getServer());
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            eventBus.call(new PlayerJoinEvent(player, player.getUUID()));
        }
    }

    @SubscribeEvent
    public void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            eventBus.call(new PlayerQuitEvent(player, player.getUUID()));
        }
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        onCommandRegister(event.getDispatcher());
    }
}
