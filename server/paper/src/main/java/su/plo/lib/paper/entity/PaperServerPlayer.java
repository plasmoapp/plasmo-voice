package su.plo.lib.paper.entity;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.lib.api.server.entity.MinecraftServerEntity;
import su.plo.lib.api.server.entity.MinecraftServerPlayer;
import su.plo.lib.api.server.permission.PermissionTristate;
import su.plo.lib.paper.chat.BaseComponentTextConverter;
import su.plo.voice.server.player.PermissionSupplier;

import java.util.Collection;
import java.util.Optional;

public final class PaperServerPlayer extends PaperServerEntity<Player> implements MinecraftServerPlayer {

    private final JavaPlugin loader;
    private final BaseComponentTextConverter textConverter;
    private final PermissionSupplier permissions;

    private MinecraftServerEntity spectatorTarget;

    public PaperServerPlayer(@NotNull JavaPlugin loader,
                             @NotNull MinecraftServerLib minecraftServer,
                             @NotNull BaseComponentTextConverter textConverter,
                             @NotNull PermissionSupplier permissions,
                             @NotNull Player player) {
        super(minecraftServer, player);

        this.loader = loader;
        this.textConverter = textConverter;
        this.permissions = permissions;
    }

    @Override
    public @NotNull String getName() {
        return instance.getName();
    }

    @Override
    public boolean isSpectator() {
        return instance.getGameMode() == GameMode.SPECTATOR;
    }

    @Override
    public boolean isSneaking() {
        return instance.isSneaking();
    }

    @Override
    public boolean hasLabelScoreboard() {
        return instance.getScoreboard().getObjective(DisplaySlot.BELOW_NAME) != null;
    }

    @Override
    public void sendMessage(@NotNull MinecraftTextComponent text) {
        instance.sendMessage(textConverter.convert(text));
    }

    @Override
    public void sendMessage(@NotNull String text) {
        instance.sendMessage(text);
    }

    @Override
    public @NotNull String getLanguage() {
        return instance.getLocale();
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return permissions.hasPermission(instance, permission);
    }

    @Override
    public @NotNull PermissionTristate getPermission(@NotNull String permission) {
        return permissions.getPermission(instance, permission);
    }

    @Override
    public void sendPacket(@NotNull String channel, byte[] data) {
        instance.sendPluginMessage(loader, channel, data);
    }

    @Override
    public void kick(@NotNull MinecraftTextComponent reason) {
        instance.kickPlayer(reason.toString()); // todo: use BaseComponent?
    }

    @Override
    public boolean canSee(@NotNull MinecraftServerPlayer player) {
        return instance.canSee(((PaperServerPlayer) player).instance);
    }

    @Override
    public Collection<String> getRegisteredChannels() {
        return instance.getListeningPluginChannels();
    }

    @Override
    public Optional<MinecraftServerEntity> getSpectatorTarget() {
        if (instance.getSpectatorTarget() == null) {
            this.spectatorTarget = null;
        } else if (!instance.getSpectatorTarget().equals(spectatorTarget.getInstance())) {
            this.spectatorTarget = minecraftServer.getEntity(instance.getSpectatorTarget());
        }

        return Optional.ofNullable(spectatorTarget);
    }
}
