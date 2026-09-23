package com.dedokok.events.meteor;

import com.dedokok.events.Cancellable;
import com.dedokok.events.world.TickEvent;
import com.dedokok.gui.widgets.WWidget;
import net.minecraft.client.gui.screens.Screen;

public class InitWidgetEvent extends Cancellable {
    private static final InitWidgetEvent INSTANCE = new InitWidgetEvent();

    public WWidget widget;

    public static InitWidgetEvent get(WWidget widget) {
        INSTANCE.setCancelled(false);
        INSTANCE.widget = widget;
        return INSTANCE;
    }
}
