package com.dedokok.events.meteor;

import com.dedokok.events.Cancellable;
import com.dedokok.gui.WidgetScreen;
import net.minecraft.client.gui.screens.Screen;

public class ChangeScreenEvent extends Cancellable {
    private static final ChangeScreenEvent INSTANCE = new ChangeScreenEvent();

    public Screen screen;

    public static ChangeScreenEvent get(Screen screen) {
        INSTANCE.setCancelled(false);
        INSTANCE.screen = screen;
        return INSTANCE;
    }
}
