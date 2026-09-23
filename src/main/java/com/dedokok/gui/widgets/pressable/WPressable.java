/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.gui.widgets.pressable;

import com.dedokok.gui.widgets.WWidget;
import net.minecraft.client.input.MouseButtonEvent;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public abstract class WPressable extends WWidget {
    public Runnable action;

    public boolean pressed;

    @Override
    public boolean onMouseClicked(MouseButtonEvent click, boolean doubled) {
        if(layer==2){
            int a = 0;
            a++;
        }
        if (mouseOver) {
            if ((click.button() == GLFW_MOUSE_BUTTON_LEFT || click.button() == GLFW_MOUSE_BUTTON_RIGHT))
                pressed = true;
        }
        return pressed;
    }

    @Override
    public boolean onMouseReleased(MouseButtonEvent click) {
        if (pressed) {
            onPressed(click.button());
            if (action != null) action.run();

            pressed = false;
        }

        return false;
    }

    protected void onPressed(int button) {
    }
}
