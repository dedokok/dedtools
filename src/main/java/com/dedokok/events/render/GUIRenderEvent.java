package com.dedokok.events.render;/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */


import com.dedokok.gui.renderer.GuiRenderer;
import com.dedokok.utils.Utils;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class GUIRenderEvent {
    private static final GUIRenderEvent INSTANCE = new GUIRenderEvent();

    public GuiRenderer guiRenderer;
    public double mouseX, mouseY;
    public double frameTime;
    public float tickDelta;

    public static GUIRenderEvent get(GuiRenderer guiRenderer, double mouseX, double mouseY, float tickDelta) {
        INSTANCE.guiRenderer = guiRenderer;
        INSTANCE.mouseX=mouseX;
        INSTANCE.mouseY=mouseY;
        INSTANCE.tickDelta=tickDelta;
        return INSTANCE;
    }
}
