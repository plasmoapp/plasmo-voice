package su.plo.voice.server.connection;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.server.channel.McServerChannelHandler;
import su.plo.slib.api.server.entity.player.McServerPlayer;
import su.plo.voice.BaseVoice;
import su.plo.voice.proto.packets.PacketUtil;
import su.plo.voice.server.BaseVoiceServer;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@RequiredArgsConstructor
public final class ServerServiceChannelHandler implements McServerChannelHandler {

    private final BaseVoiceServer voiceServer;

    @Override
    public void receive(@NotNull McServerPlayer serverPlayer, @NotNull byte[] bytes) {
        if (voiceServer.getConfig().host().forwardingSecret() == null) return;

        ByteArrayDataInput input = ByteStreams.newDataInput(bytes);

        try {
            byte[] signature = PacketUtil.readBytes(input, 32);
            byte[] aesEncryptionKey = PacketUtil.readBytes(input, 64);

            SecretKey key = new SecretKeySpec(
                    PacketUtil.getUUIDBytes(voiceServer.getConfig().host().forwardingSecret()),
                    "HmacSHA256"
            );
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            mac.update(aesEncryptionKey, 0, aesEncryptionKey.length);
            if (!MessageDigest.isEqual(signature, mac.doFinal())) {
                BaseVoice.LOGGER.warn("Received invalid AES key signature from player {}", serverPlayer.getUuid());
                return;
            }

            voiceServer.updateAesEncryptionKey(aesEncryptionKey);

            ByteArrayDataOutput output = ByteStreams.newDataOutput();
            PacketUtil.writeBytes(output, signature);

            serverPlayer.sendPacket(BaseVoiceServer.SERVICE_CHANNEL_STRING, output.toByteArray());

            BaseVoice.DEBUG_LOGGER.log("Received AES key from proxy server");
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }
}
