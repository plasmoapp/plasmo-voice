package su.plo.voice.proxy.util;

import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;

public class AddressUtil {

    public static InetSocketAddress parseAddress(@NotNull String ip) {
        Preconditions.checkNotNull(ip, "ip");
        URI uri = URI.create("udp://" + ip);
        if (uri.getHost() == null) {
            throw new IllegalStateException("Invalid hostname/IP " + ip);
        }

        int port = uri.getPort() == -1 ? 60606 : uri.getPort();
        try {
            InetAddress ia = InetAddresses.forUriString(uri.getHost());
            return new InetSocketAddress(ia, port);
        } catch (IllegalArgumentException e) {
            return InetSocketAddress.createUnresolved(uri.getHost(), port);
        }
    }

    private AddressUtil() {
    }
}
