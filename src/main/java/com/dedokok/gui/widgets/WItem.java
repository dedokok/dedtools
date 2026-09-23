/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.gui.widgets;

import com.dedokok.events.meteor.MouseClickEvent;
import com.dedokok.gui.renderer.GuiRenderer;
import com.dedokok.gui.widgets.containers.WView;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.item.ItemStack;

import static com.dedokok.DedTools.mc;

public class WItem extends WWidget {
    protected ItemStack itemStack;

    public WItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    protected void onCalculateSize() {
        double s = theme.scale(32);

        width = s;
        height = s;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!itemStack.isEmpty()) {
           // renderer.post(() -> {
                double s = theme.scale(2);
            //renderer.graphics.fill((int) x, (int) y, (int) (x + 16), (int) (y + 16), 0xFFFF0000);
                renderer.item(itemStack, (int) x, (int) y, (float) s, true);
            //});
        }
    }

    @Override
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!visible) return true;

        onRender(renderer, mouseX, mouseY, delta);
        if (isOver(mouseX, mouseY)) {
            mouseOverTimer += delta;
            if (mouseOverTimer > 0 && tooltip != null) {
                WView view = getView();
                if (view == null || view.mouseOver){
                    renderer.absolutePost(() -> {
                        renderer.tooltip(tooltip);
                    });
                }
            }
        } else {
            mouseOverTimer = 0;
        }

        return false;
    }

    public void set(ItemStack itemStack) {
        this.itemStack = itemStack;
    }


//    @Override
//    public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
//       // if(!isFocused())return;
//        //if (itemStack.getItemName().getString() != null) {
//        if(itemStack.getCount() > 0 && mouseOver) {
//            System.out.println(itemStack.toString());
//            System.out.println("навёлся на " + itemStack.getDisplayName().getString());
//        }
//
//        //}
//
//    }

//    @Override
//    public boolean onMouseClicked(MouseButtonEvent event, boolean doubled){
//        double x = event.x();
//        double y = event.y();
//        if(isOver(x, y)){
//            mc.player.connection.sendCommand();
//        }
//        return doubled;
//    }
}
