package su.plo.voice.server.connection;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoiceServerPlayer;
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
public abstract class BaseServerServiceChannelHandler {

    protected final BaseVoiceServer voiceServer;

    protected void handlePacket(@NotNull VoiceServerPlayer player, byte[] data) throws IOException {
        if (voiceServer.getConfig().host().forwardingSecret() == null) return;

        ByteArrayDataInput input = ByteStreams.newDataInput(data);

        byte[] signature = PacketUtil.readBytes(input, 32);
        try {
            byte[] aesEncryptionKey = PacketUtil.readBytes(input, 64);

            SecretKey key = new SecretKeySpec(
                    PacketUtil.getUUIDBytes(voiceServer.getConfig().host().forwardingSecret()),
                    "HmacSHA256"
            );
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            mac.update(aesEncryptionKey, 0, aesEncryptionKey.length);
            if (!MessageDigest.isEqual(signature, mac.doFinal())) {
                voiceServer.getLogger().warn("Received invalid AES key signature from player " + player.getInstance().getUUID());
                return;
            }

            voiceServer.getConfig().voice().aesEncryptionKey(aesEncryptionKey);

            ByteArrayDataOutput output = ByteStreams.newDataOutput();
            PacketUtil.writeBytes(output, signature);

            player.getInstance().sendPacket(
                    BaseVoiceServer.SERVICE_CHANNEL_STRING,
                    output.toByteArray()
            );

            voiceServer.getDebugLogger().log("Received AES key from proxy server");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }
}
