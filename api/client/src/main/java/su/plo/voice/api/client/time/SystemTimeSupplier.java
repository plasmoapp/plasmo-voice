package su.plo.voice.api.client.time;

public class SystemTimeSupplier implements TimeSupplier {

    @Override
    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }
}
