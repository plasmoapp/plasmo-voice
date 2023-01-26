package su.plo.voice.client.chat;

import java.util.Map;
import java.util.Optional;

@FunctionalInterface
public interface ClientLanguageSupplier {

    Optional<Map<String, String>> get();
}
