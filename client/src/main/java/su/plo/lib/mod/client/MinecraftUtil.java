package su.plo.lib.mod.client;

import lombok.experimental.UtilityClass;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class MinecraftUtil {

    public static void openUri(@NotNull String url) {
        //#if MC>=26.3
        //$$ com.mojang.blaze3d.Blaze3D.openUri(java.net.URI.create(url));
        //#else
        Util.getPlatform().openUri(url);
        //#endif
    }

    public static String getVersion() {
        return SharedConstants.getCurrentVersion().getName();
    }
}
