package su.plo.voice.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class TranslatableText extends Text {

    @Getter
    private final String key;
    @Getter
    private final Object[] args;

    @Override
    public String toString() {
        return key;
    }
}
