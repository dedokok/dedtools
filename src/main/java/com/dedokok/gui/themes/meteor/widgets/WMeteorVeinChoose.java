/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.gui.themes.meteor.widgets;

import com.dedokok.gui.GuiHitTest;
import com.dedokok.gui.WindowScreen;
import com.dedokok.gui.renderer.GuiRenderer;
import com.dedokok.gui.screens.settings.VeinsListSettingScreen;
import com.dedokok.gui.themes.meteor.MeteorWidget;
import com.dedokok.gui.widgets.WTooltip;
import com.dedokok.gui.widgets.WVeinChoose;
import com.dedokok.systems.modules.Feature.BlockBreakFinder;
import com.dedokok.utils.classes.Vein;
import com.dedokok.utils.render.color.Color;
import net.minecraft.client.input.MouseButtonEvent;

public class WMeteorVeinChoose extends WVeinChoose implements MeteorWidget {
    public WMeteorVeinChoose(VeinsListSettingScreen screen, Vein vein) {
        super(screen,vein);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        Color bgColor =new Color(theme().backgroundColor.get());
        bgColor.r+=20;
        bgColor.g+=20;
        bgColor.b+=20;

        double t = theme.scale(1);

        renderer.quad(this, bgColor);
        renderer.quad(x,y,width,t,Color.RED);
        renderer.quad(x, y + height - t, width, t, Color.RED);       // низ
        renderer.quad(x, y + t, t, height - 2 * t, Color.RED);       // лево
        renderer.quad(x + width - t, y + t, t, height - 2 * t, Color.RED);

    }



}
