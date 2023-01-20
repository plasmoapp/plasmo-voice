package su.plo.voice.api.server.config;

import lombok.NoArgsConstructor;

import java.util.function.Function;

@NoArgsConstructor
public class ServerLanguageProcessor implements Function<Object, Object> {

    @Override
    public Object apply(Object object) {
        if (object instanceof String) {
            String string = (String) object;
            return string.replaceAll("&", "§");
        }

        return object;
    }
}
