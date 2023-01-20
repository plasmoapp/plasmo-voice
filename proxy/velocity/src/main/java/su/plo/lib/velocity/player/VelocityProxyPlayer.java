package su.plo.lib.velocity.player;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.util.GameProfile;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.proxy.MinecraftProxyLib;
import su.plo.lib.api.proxy.connection.MinecraftProxyServerConnection;
import su.plo.lib.api.proxy.player.MinecraftProxyPlayer;
import su.plo.lib.api.server.permission.PermissionTristate;
import su.plo.lib.velocity.chat.ComponentTextConverter;
import su.plo.lib.velocity.connection.VelocityProxyServerConnection;
import su.plo.voice.proto.data.player.MinecraftGameProfile;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public final class VelocityProxyPlayer implements MinecraftProxyPlayer {

    private final MinecraftProxyLib minecraftProxy;
    private final ComponentTextConverter textConverter;
    @Getter
    private final Player instance;
    @Getter
    private final VelocityTabList tabList;

    private VelocityProxyServerConnection server;

    public VelocityProxyPlayer(@NotNull MinecraftProxyLib minecraftProxy,
                               @NotNull ComponentTextConverter textConverter,
                               @NotNull Player instance) {
        this.minecraftProxy = minecraftProxy;
        this.textConverter = textConverter;
        this.instance = instance;
        this.tabList = new VelocityTabList(instance);
    }

    @Override
    public @NotNull MinecraftGameProfile getGameProfile() {
        GameProfile gameProfile = instance.getGameProfile();

        return new MinecraftGameProfile(
                gameProfile.getId(),
                gameProfile.getName(),
                gameProfile.getProperties().stream()
                        .map((property) -> new MinecraftGameProfile.Property(
                                property.getName(),
                                property.getValue(),
                                property.getSignature()
                        ))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Optional<MinecraftProxyServerConnection> getServer() {
        if (!instance.getCurrentServer().isPresent()) {
            this.server = null;
            return Optional.empty();
        }

        ServerConnection connection = instance.getCurrentServer().get();
        if (server != null && server.getInstance().equals(connection)) {
            return Optional.of(server);
        }

        this.server = new VelocityProxyServerConnection(minecraftProxy, connection);
        return Optional.of(server);
    }

    @Override
    public void sendPacket(@NotNull String channel, byte[] data) {
        instance.sendPluginMessage(MinecraftChannelIdentifier.from(channel), data);
    }

    @Override
    public void kick(@NotNull MinecraftTextComponent reason) {
        instance.disconnect(textConverter.convert(reason));
    }

    @Override
    public @NotNull UUID getUUID() {
        return instance.getUniqueId();
    }

    @Override
    public @NotNull String getName() {
        return instance.getUsername();
    }

    @Override
    public void sendMessage(@NotNull MinecraftTextComponent text) {
        instance.sendMessage(textConverter.convert(text));
    }

    @Override
    public void sendMessage(@NotNull String text) {
        instance.sendMessage(Component.text(text));
    }

    @Override
    public void sendActionBar(@NotNull String text) {
        instance.sendActionBar(Component.text(text));
    }

    @Override
    public void sendActionBar(@NotNull MinecraftTextComponent text) {
        instance.sendActionBar(textConverter.convert(text));
    }

    @Override
    public @NotNull String getLanguage() {
        return instance.getPlayerSettings().getLocale().toString();
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return instance.hasPermission(permission);
    }

    @Override
    public @NotNull PermissionTristate getPermission(@NotNull String permission) {
        switch (instance.getPermissionValue(permission)) {
            case TRUE:
                return PermissionTristate.TRUE;
            case FALSE:
                return PermissionTristate.FALSE;
            default:
                return PermissionTristate.UNDEFINED;
        }
    }
}
