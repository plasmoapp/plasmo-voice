package su.plo.lib.mod.client;

import lombok.experimental.UtilityClass;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class MinecraftUtil {

    public static void openUri(@NotNull String url) {
        Util.getPlatform().openUri(url);
    }

    public static String getVersion() {
        return SharedConstants.getCurrentVersion().getName();
    }
}
