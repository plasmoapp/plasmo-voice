package su.plo.voice.client.gui.settings.widget;


import su.plo.lib.api.client.gui.widget.GuiWidget;
import su.plo.lib.api.client.gui.widget.GuiWidgetListener;

public interface UpdatableWidget extends GuiWidget, GuiWidgetListener {

    void updateValue();
}
