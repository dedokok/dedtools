/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.gui.widgets;

import com.dedokok.gui.renderer.GuiRenderer;
import com.dedokok.gui.screens.settings.VeinsListSettingScreen;
import com.dedokok.gui.widgets.containers.WView;
import com.dedokok.systems.modules.Feature.BlockBreakFinder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.item.ItemStack;

import static com.dedokok.DedTools.mc;

public class WBBFItem extends WItem {
    protected ItemStack itemStack;

    public int block_x;
    public int block_y;
    public int block_z;
    public String username;
    public String world;
    public String date;
    public int id;
    public VeinsListSettingScreen screen;

    public WBBFItem(ItemStack itemStack) {
        this.itemStack = itemStack;
        super(itemStack);
    }

    public WBBFItem(VeinsListSettingScreen screen, int id, WItem wItem, int x, int y, int z, String username, String date, String world) {
        super(wItem.itemStack);
        this.screen = screen;
        this.block_x = x;
        this.block_y = y;
        this.block_z = z;
        this.username = username;
        this.date = date;
        this.world = world;
        this.itemStack=wItem.itemStack;
        this.id = id;
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
            renderer.post(() -> {
                double s = theme.scale(2);
            //renderer.graphics.fill((int) x, (int) y, (int) (x + 16), (int) (y + 16), 0xFFFF0000);
                renderer.item(itemStack, (int) x, (int) y, (float) s, true);
            });
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

    @Override
    public boolean onMouseClicked(MouseButtonEvent event, boolean doubled){
        double x = event.x();
        double y = event.y();
        if(isOver(x, y)) {
            if (event.button() == 0) {
                BlockBreakFinder.teleported_vein = BlockBreakFinder.veinsArrayList.get(id);
                BlockBreakFinder.veinNow = id;
                mc.player.connection.sendCommand("co teleport " + world + " " + block_x + " " + block_y + " " + block_z);
            }
            if (event.button() == 1) {
                screen.openWidget(id);
            }
        }
        return doubled;
    }
}
