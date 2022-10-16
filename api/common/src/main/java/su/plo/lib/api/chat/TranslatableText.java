package su.plo.lib.api.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class TranslatableText extends TextComponent {

    @Getter
    private final String key;
    @Getter
    private final Object[] args;

    @Override
    public String toString() {
        return key;
    }
}
