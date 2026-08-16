/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.utils.render;

import com.dedokok.systems.modules.Modules;
import com.dedokok.utils.Utils;
import com.dedokok.utils.render.color.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import static com.dedokok.DedTools.mc;

public class PeekScreen extends ShulkerBoxScreen {
    private final Identifier TEXTURE = Identifier.parse("textures/gui/container/shulker_box.png");
    private final ItemStack storageBlock;

    public PeekScreen(ItemStack storageBlock, ItemStack[] contents) {
        super(new ShulkerBoxMenu(0, mc.player.getInventory(), new SimpleContainer(contents)), mc.player.getInventory(), storageBlock.getHoverName());
        this.storageBlock = storageBlock;
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
        return false;
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent click) {
        return false;
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent input) {


        if (input.key() == GLFW.GLFW_KEY_ESCAPE || mc.options.keyInventory.matches(input)) {
            onClose();
            return true;
        }

        return false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        Color color = Utils.getShulkerColor(storageBlock);

        int i = (width - imageWidth) / 2;
        int j = (height - imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0f, 0f, imageWidth, imageHeight, imageWidth, imageHeight, 256, 256, ARGB.colorFromFloat(color.a / 255f, color.r / 255f, color.g / 255f, color.b / 255f));
    }
}
