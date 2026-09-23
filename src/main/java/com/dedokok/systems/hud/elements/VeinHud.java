/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.systems.hud.elements;

import com.dedokok.settings.*;
import com.dedokok.systems.hud.*;
import com.dedokok.systems.modules.Feature.BlockBreakFinder;
import com.dedokok.systems.modules.Feature.PlayerStats;
import com.dedokok.systems.modules.Feature.PlayerTracker;
import com.dedokok.systems.modules.Modules;
import com.dedokok.utils.render.color.SettingColor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.UUID;

import static com.dedokok.DedTools.mc;
import static com.dedokok.systems.modules.Feature.BlockBreakFinder.unpack;

public class VeinHud extends HudElement {
    public static final HudElementInfo<VeinHud> INFO = new HudElementInfo<>(Hud.GROUP, "vein-stats", "Stats of vein you teleported", VeinHud::new);

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


    private final Setting<Alignment> alignment = sgGeneral.add(new EnumSetting.Builder<Alignment>()
        .name("alignment")
        .description("Horizontal alignment.")
        .defaultValue(Alignment.Auto)
        .build()
    );

    public VeinHud() {
        super(INFO);
    }

    double height = 0;
    double width = 0;
    double y_temp = 0;
    UUID uuid = null;

    Player player = null;

    @Override
    public void render(HudRenderer renderer) {
        if(!Modules.get().get(BlockBreakFinder.class).veinStatsHudSetting.get()){
            return;
        }
        boolean shouldStop = false;


        String text = "Параметры жилы: ";
        if(BlockBreakFinder.teleported_vein==null){
            shouldStop = true;
        }

//        else{
//            shouldStop=true;
//        }


        y_temp = this.y;

        width = 0;
        height = 0;

        int i = 1;



        if(!shouldStop) {
            updateSize(renderer, text,true);
            //if(uuid!=null && PlayerStats.targetUUID!=null&&!uuid.equals(PlayerStats.targetUUID)){
                //uuid=PlayerStats.targetUUID;
            //}

            BlockBreakFinder.Vein vein = BlockBreakFinder.teleported_vein;

            if(mc.player == null)return;

            String username_string = "Игрок: " + vein.rows.getFirst().getUser();
            updateSize(renderer, username_string,false);
            String health_string = "Тип блока: " + vein.rows.getFirst().getBlock();
            updateSize(renderer, health_string,false);

            int[] coords = unpack(vein.rows.getFirst().getCoords());
            int x = coords[0], y = coords[1], z = coords[2];
            String coords_string = "Координаты: " + x+" "+y+" "+z;
            updateSize(renderer, coords_string,false);

            String world_string = "Мир: "+vein.rows.getFirst().getWrld();
            updateSize(renderer, world_string,false);


            SimpleDateFormat sdfEST = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            sdfEST.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
            String date = sdfEST.format(vein.rows.getFirst().getTimestamp()*1000);
            String date_string = "Дата: "+date;
            updateSize(renderer, date_string,false);

            String size_string = "Количество блоков: " + vein.rows.size();
            updateSize(renderer, size_string,false);

            String id_string = "Id: " + vein.id+"/"+BlockBreakFinder.veinsArrayList.size();
            updateSize(renderer, id_string,false);
        }

        setSize(width, height);
    }

    public void updateSize(HudRenderer renderer, String string, boolean isFirst){
        double moduleWidth = renderer.textWidth(string) + renderer.textWidth(" ");
        double x = this.x + alignX(moduleWidth, alignment.get());
        x = renderer.text(string, x, y_temp, moduleColor.get(), textShadow.get());
        //renderer.text(string, x, y, moduleColor.get(), textShadow.get());
        y_temp += renderer.textHeight() + 2;
        width = Math.max(width, moduleWidth);
        height += renderer.textHeight();
        if(!isFirst)height += 2;
    }
    public Entity getPlayerByUUID(UUID uuid){
        if(mc==null || mc.level==null)return null;
        return mc.level.getEntity(uuid);
    }
}
