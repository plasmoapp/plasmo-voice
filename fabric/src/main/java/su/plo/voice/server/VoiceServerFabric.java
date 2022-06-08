package su.plo.voice.server;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import su.plo.voice.server.commands.CommandManager;
import su.plo.voice.server.config.ServerConfigFabric;
import su.plo.voice.server.network.ServerNetworkHandlerFabric;

public class VoiceServerFabric extends VoiceServer implements ModInitializer {
    static {
        network = new ServerNetworkHandlerFabric();
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            setServer(server);
            this.start();

            if (server.isDedicatedServer()) {
                this.setupMetrics("Fabric");
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> this.close());

        S2CPlayChannelEvents.REGISTER.register((handler, sender, server, channels) ->
                network.handleRegisterChannels(channels, handler.player)
        );
        ServerPlayNetworking.registerGlobalReceiver(VoiceServer.PLASMO_VOICE, ((ServerNetworkHandlerFabric) network)::handle);

        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) ->
                network.handleJoin(handler.player))
        );

        ServerPlayConnectionEvents.DISCONNECT.register(((handler, server) ->
                network.handleQuit(handler.player))
        );

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, selection) ->
                CommandManager.register(dispatcher)
        );
    }

    @Override
    protected void start() {
        network.start();
        super.start();
    }

    @Override
    protected void close() {
        network.close();
        super.close();
    }

    @Override
    public void updateConfig() {
        super.updateConfig();

        int clientModCheckTimeout = config.getInt("client_mod_check_timeout");
        if (clientModCheckTimeout < 20) {
            LOGGER.warn("Client mod check timeout cannot be < 20 ticks");
            return;
        }

        VoiceServer.setServerConfig(new ServerConfigFabric(getServerConfig(), config.getBoolean("client_mod_required"), clientModCheckTimeout));
    }
}
