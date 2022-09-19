package su.plo.voice.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
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
