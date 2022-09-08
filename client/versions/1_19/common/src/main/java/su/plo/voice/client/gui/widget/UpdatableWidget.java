package su.plo.voice.client.gui.widget;

import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;

public interface UpdatableWidget extends Widget, GuiEventListener {

    void updateValue();
}
