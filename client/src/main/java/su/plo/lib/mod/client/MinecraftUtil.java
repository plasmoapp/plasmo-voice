package su.plo.lib.mod.client;

import lombok.experimental.UtilityClass;
import net.minecraft.SharedConstants;

@UtilityClass
public class MinecraftUtil {

    public static String getVersion() {
        return SharedConstants.getCurrentVersion().getName();
    }
}
