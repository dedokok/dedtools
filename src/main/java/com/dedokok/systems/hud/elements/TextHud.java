/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.systems.hud.elements;

import com.dedokok.DedTools;
import com.dedokok.settings.*;
import com.dedokok.systems.hud.Hud;
import com.dedokok.systems.hud.HudElement;
import com.dedokok.systems.hud.HudElementInfo;
import com.dedokok.systems.hud.HudRenderer;
import com.dedokok.utils.render.color.Color;
import com.dedokok.utils.render.color.SettingColor;

import java.util.List;

public class TextHud extends HudElement {
    private static final Color WHITE = new Color();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgShown = settings.createGroup("Shown");
    private final SettingGroup sgScale = settings.createGroup("Scale");
    private final SettingGroup sgBackground = settings.createGroup("Background");

    private double originalWidth, originalHeight;
    private boolean needsCompile, recalculateSize;

    private int timer;

    // General

    public final Setting<String> text = sgGeneral.add(new StringSetting.Builder()
        .name("text")
        .description("Text to display with Starscript.")
        .defaultValue(DedTools.NAME)
        .onChanged(_ -> recompile())
        .wide()
        .build()
    );

    public final Setting<Integer> updateDelay = sgGeneral.add(new IntSetting.Builder()
        .name("update-delay")
        .description("Update delay in ticks")
        .defaultValue(4)
        .onChanged(integer -> {
            if (timer > integer) timer = integer;
        })
        .min(0)
        .build()
    );

    public final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Renders shadow behind text.")
        .defaultValue(true)
        .onChanged(_ -> recalculateSize = true)
        .build()
    );

    public final Setting<Integer> border = sgGeneral.add(new IntSetting.Builder()
        .name("border")
        .description("How much space to add around the text.")
        .defaultValue(0)
        .onChanged(integer -> super.setSize(originalWidth + integer * 2, originalHeight + integer * 2))
        .build()
    );

    // Shown

    public final Setting<Shown> shown = sgShown.add(new EnumSetting.Builder<Shown>()
        .name("shown")
        .description("When this text element is shown.")
        .defaultValue(Shown.Always)
        .onChanged(_ -> recompile())
        .build()
    );

    public final Setting<String> condition = sgShown.add(new StringSetting.Builder()
        .name("condition")
        .description("Condition to check when shown is not Always.")
        .visible(() -> shown.get() != Shown.Always)
        .onChanged(_ -> recompile())
        .build()
    );

    // Scale

    public final Setting<Boolean> customScale = sgScale.add(new BoolSetting.Builder()
        .name("custom-scale")
        .description("Applies a custom scale to this hud element.")
        .defaultValue(false)
        .onChanged(_ -> recalculateSize = true)
        .build()
    );

    public final Setting<Double> scale = sgScale.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Custom scale.")
        .visible(customScale::get)
        .defaultValue(1)
        .onChanged(_ -> recalculateSize = true)
        .min(0.5)
        .sliderRange(0.5, 3)
        .build()
    );

    // Background

    public final Setting<Boolean> background = sgBackground.add(new BoolSetting.Builder()
        .name("background")
        .description("Displays background.")
        .defaultValue(false)
        .build()
    );

    public final Setting<SettingColor> backgroundColor = sgBackground.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Color used for the background.")
        .visible(background::get)
        .defaultValue(new SettingColor(25, 25, 25, 50))
        .build()
    );



    private boolean firstTick = true;
    private boolean empty = false;
    private boolean visible;

    public TextHud(HudElementInfo<TextHud> info) {
        super(info);

        needsCompile = true;
    }

    private void recompile() {
        firstTick = true;
        needsCompile = true;
    }

    @Override
    public void setSize(double width, double height) {
        this.originalWidth = width;
        this.originalHeight = height;
        super.setSize(width + border.get() * 2, height + border.get() * 2);
    }

    private void calculateSize(HudRenderer renderer) {
        double width = 0;



        if (width != 0) {
            setSize(width, renderer.textHeight(shadow.get(), getScale()));
            empty = false;
        } else {
            setSize(100, renderer.textHeight(shadow.get(), getScale()));
            empty = true;
        }
    }






    @Override
    public void onFontChanged() {
        recalculateSize = true;
    }

    private double getScale() {
        return customScale.get() ? scale.get() : Hud.get().getTextScale();
    }

    public static Color getSectionColor(int i) {
        List<SettingColor> colors = Hud.get().textColors.get();
        return (i >= 0 && i < colors.size()) ? colors.get(i) : WHITE;
    }

    public enum Shown {
        Always,
        WhenTrue,
        WhenFalse;

        @Override
        public String toString() {
            return switch (this) {
                case Always -> "Always";
                case WhenTrue -> "When True";
                case WhenFalse -> "When False";
            };
        }
    }
}
