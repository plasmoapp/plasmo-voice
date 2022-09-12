package su.plo.voice.client;

import su.plo.voice.chat.TextConverter;
import su.plo.voice.client.gui.ScreenContainer;
import su.plo.voice.client.player.ClientPlayer;

import java.util.Optional;

public interface MinecraftClientLib {

    TextConverter<?> getTextConverter();

    Optional<ClientPlayer> getClientPlayer();

    Optional<ScreenContainer> getScreen();
}
