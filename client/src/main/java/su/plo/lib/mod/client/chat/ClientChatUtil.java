package su.plo.lib.mod.client.chat;

import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.voice.universal.UMinecraft;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.minecraft.network.chat.Component;
import su.plo.lib.mod.client.render.RenderUtil;

@UtilityClass
public class ClientChatUtil {

    public static void sendChatMessage(@NonNull McTextComponent text) {
        sendChatMessage(RenderUtil.getTextConverter().convert(text));
    }

    public static void sendChatMessage(@NonNull Component message) {
        //#if MC>=11900
        UMinecraft.getPlayer().sendSystemMessage(message);
        //#elseif MC>=11602
        //$$ UMinecraft.getPlayer().sendMessage(message, null);
        //#elseif MC>=11202
        //$$ UMinecraft.getPlayer().sendMessage(message);
        //#else
        //$$ UMinecraft.getPlayer().addChatMessage(message);
        //#endif
    }
}
