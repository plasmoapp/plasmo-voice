package su.plo.lib.mod.client.chat;

import java.util.Map;
import java.util.Optional;

@FunctionalInterface
public interface ClientLanguageSupplier {

    Optional<Map<String, String>> get();
}
