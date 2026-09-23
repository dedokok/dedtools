/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.systems.hud.elements;

import com.dedokok.settings.*;
import com.dedokok.settings.classes.PlayerNote;
import com.dedokok.systems.hud.*;
import com.dedokok.systems.modules.Feature.PlayerTracker;
import com.dedokok.systems.modules.Feature.PlayersNoteBook;
import com.dedokok.systems.modules.Module;
import com.dedokok.systems.modules.Modules;
import com.dedokok.utils.render.color.Color;
import com.dedokok.utils.render.color.SettingColor;

import java.util.List;

public class PlayerTrackerHud extends HudElement {
    public static final HudElementInfo<PlayerTrackerHud> INFO = new HudElementInfo<>(Hud.GROUP, "player-tracker", "Player Tracker HUD", PlayerTrackerHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> textShadow = sgGeneral.add(new BoolSetting.Builder()
        .name("text-shadow")
        .description("Renders shadow behind text.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> moduleColor = sgGeneral.add(new ColorSetting.Builder()
        .name("module-color")
        .description("Module color.")
        .defaultValue(new SettingColor())
        .build()
    );

    private final Setting<SettingColor> onlinePlayersColor = sgGeneral.add(new ColorSetting.Builder()
        .name("online-players-color")
        .description("Color of online players.")
        .defaultValue(new SettingColor(85, 225, 85))
        .build()
    );


    private final Setting<Alignment> alignment = sgGeneral.add(new EnumSetting.Builder<Alignment>()
        .name("alignment")
        .description("Horizontal alignment.")
        .defaultValue(Alignment.Auto)
        .build()
    );


    public PlayerTrackerHud() {
        super(INFO);
    }


    double height = 0;
    double width = 0;
    double y_temp = 0;


    @Override
    public void render(HudRenderer renderer) {
        boolean shouldStop = false;
        String text = "Трекер игроков";
        if(!Modules.get().isActive(PlayerTracker.class)) {
            shouldStop = true;
            text= text+" ВЫКЛЮЧЕНО";
        }
        y_temp = this.y;
        width = 0;
        height = 0;
       // renderer.text(text, x, y, moduleColor.get(), textShadow.get());
        updateSize(renderer,text,true, moduleColor.get());
        //setSize(renderer.textWidth(text), renderer.textHeight());








        int i = 1;

        if(shouldStop){
            setSize(width, height);
            return;}
        //boolean isFirst=true;
        for(PlayerNote note : PlayerTracker.onlinePlayers) {
            String string = note.username;
            String desc = note.description;
            if(desc!=null){
                string = string + " - " + desc;
            }
            updateSize(renderer, string,false, onlinePlayersColor.get());
        }

        setSize(width, height);
    }

    public void updateSize(HudRenderer renderer, String string, boolean isFirst, Color color){

        double moduleWidth = renderer.textWidth(string) + renderer.textWidth(" ");
        double x = this.x + alignX(moduleWidth, alignment.get());
        x = renderer.text(string, x, y_temp, color, textShadow.get());
        //renderer.text(string, x, y, moduleColor.get(), textShadow.get());
        y_temp += renderer.textHeight() + 2;
        width = Math.max(width, moduleWidth);
        height += renderer.textHeight();
        if(!isFirst)height += 2;
    }
}
