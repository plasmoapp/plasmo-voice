package su.plo.lib.api.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class MinecraftTranslatableText extends MinecraftTextComponent {

    @Getter
    private final String key;
    @Getter
    private final Object[] args;

    @Override
    public String toString() {
        return key;
    }
}
