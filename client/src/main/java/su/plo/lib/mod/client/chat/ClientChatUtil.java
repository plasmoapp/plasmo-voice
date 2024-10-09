package su.plo.lib.mod.client.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import su.plo.slib.api.chat.component.McTextComponent;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.minecraft.network.chat.Component;
import su.plo.lib.mod.client.render.RenderUtil;

@UtilityClass
public class ClientChatUtil {

    public static void setActionBar(@NonNull Component component) {
        Minecraft.getInstance().gui.setOverlayMessage(
                component,
                false
        );
    }

    public static void setActionBar(@NonNull McTextComponent text) {
       setActionBar(RenderUtil.getTextConverter().convert(text));
    }

    public static void sendChatMessage(@NonNull McTextComponent text) {
        sendChatMessage(RenderUtil.getTextConverter().convert(text));
    }

    public static void sendChatMessage(@NonNull Component message) {
        LocalPlayer player = Minecraft.getInstance().player;

        //#if MC>=12102
        //$$ player.displayClientMessage(message, false);
        //#elseif MC>=11900
        player.sendSystemMessage(message);
        //#elseif MC>=11602
        //$$ player.sendMessage(message, null);
        //#elseif MC>=11202
        //$$ player.sendMessage(message);
        //#else
        //$$ player.addChatMessage(message);
        //#endif
    }
}
