package su.plo.voice.proxy.connection

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteStreams
import su.plo.slib.api.proxy.channel.McProxyChannelHandler
import su.plo.slib.api.proxy.connection.McProxyConnection
import su.plo.slib.api.proxy.connection.McProxyServerConnection
import su.plo.voice.BaseVoice
import su.plo.voice.proto.packets.PacketUtil
import su.plo.voice.proxy.BaseVoiceProxy
import su.plo.voice.proxy.server.VoiceRemoteServer
import java.io.IOException
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class ProxyServiceChannelHandler(
    private val voiceProxy: BaseVoiceProxy
) : McProxyChannelHandler {
    override fun receive(source: McProxyConnection, destination: McProxyConnection, data: ByteArray): Boolean {
        if (source !is McProxyServerConnection) return false

        try {
            val input: ByteArrayDataInput = ByteStreams.newDataInput(data)
            val signature = PacketUtil.readBytes(input, 32)
            val aesEncryptionKey: ByteArray = voiceProxy.config!!.aesEncryptionKey()

            val key: SecretKey = SecretKeySpec(
                PacketUtil.getUUIDBytes(voiceProxy.config!!.forwardingSecret()),
                "HmacSHA256"
            )
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(key)
            mac.update(aesEncryptionKey, 0, aesEncryptionKey.size)

            if (!MessageDigest.isEqual(signature, mac.doFinal())) {
                BaseVoice.LOGGER.warn("Received invalid AES key signature from {}", source.serverInfo)
                return true
            }

            voiceProxy.remoteServerManager.getServer(source.serverInfo.name)
                .ifPresent {
                    (it as VoiceRemoteServer).isAesEncryptionKeySet = true
                    it.getAddress(true)
                }
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        }

        return true
    }
}
