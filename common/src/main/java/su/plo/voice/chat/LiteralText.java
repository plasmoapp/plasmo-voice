package su.plo.voice.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class LiteralText extends Text {

    @Getter
    private final String text;

    @Override
    public String toString() {
        return text;
    }
}
