/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.systems.hud.elements;

import com.dedokok.settings.*;
import com.dedokok.systems.hud.*;
import com.dedokok.systems.modules.Feature.PlayersNoteBook;
import com.dedokok.systems.modules.Modules;
import com.dedokok.utils.render.color.Color;
import com.dedokok.utils.render.color.SettingColor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.UUID;

import static com.dedokok.DedTools.mc;

public class PlayerStatsHud extends HudElement {
    public static final HudElementInfo<PlayerStatsHud> INFO = new HudElementInfo<>(Hud.GROUP, "player-stats", "Stats of player you teleported", PlayerStatsHud::new);

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

    public PlayerStatsHud() {
        super(INFO);
    }

    double height = 0;
    double width = 0;
    double y_temp = 0;
    //UUID uuid = null;
    public static UUID targetUUID = null;
    public static int idNow = -10;
    public static int idMax = -10;


    Player player = null;

    @Override
    public void render(HudRenderer renderer) {
        boolean shouldStop = false;

        String text = "Статистика игрока: ";
        if(targetUUID==null){
            player=null;
            idNow=-10;
            idMax=-10;
            shouldStop = true;
            text= text+" нет игрока";
        }
//        else{
//            shouldStop=true;
//        }

        y_temp = this.y;

        width = 0;
        height = 0;

        int i = 1;

        updateSize(renderer, text,true);

        if(!shouldStop) {

            //if(uuid!=null && PlayerStats.targetUUID!=null&&!uuid.equals(PlayerStats.targetUUID)){
            //uuid=targetUUID;
            //}

            if (player == null || !player.getUUID().equals(targetUUID)) {
                player = (Player) getPlayerByUUID(targetUUID);
                if (mc != null && mc.getConnection() != null && mc.player != null) {
                    int count = 0;
                    Collection<PlayerInfo> players = mc.getConnection().getOnlinePlayers();
                    for (PlayerInfo player : players) {
                        if (player.getProfile().id().equals(targetUUID)) {
                            idNow = count;
                            break;
                        }
                        count++;
                    }
                    idMax = players.size();
                }

            }

            //uuid = PlayerStats.targetUUID;
            //moduleWidth = renderer.textWidth(name) + renderer.textWidth(" ");
            //if(uuid!=null && player==null)player = (Player) getPlayerByUUID(uuid);
            //player = (Player) getPlayerByUUID(uuid);
            if (player == null) {
                updateSize(renderer, "игрок null", false);
                setSize(width, height);
                return;
            }
            if (mc.player == null) return;

            String username_string = "Никнейм: " + player.getName().getString();
            updateSize(renderer, username_string, false);
            String health_string = "Здоровье: " + player.getHealth();
            updateSize(renderer, health_string, false);


            PlayerInfo info = mc.player.connection.getPlayerInfo(targetUUID);
            if (info == null) {
                return;
            }

            String ping_string = "Пинг: " + info.getLatency();
            updateSize(renderer, ping_string, false);

            if (idNow != -10 && idMax != -10) {
                String id_string = "ID: " + idNow + "/" + idMax;
                updateSize(renderer, id_string, false);
            }


            String player_note = Modules.get().get(PlayersNoteBook.class).getPlayerDesc(player.getName().getString());
            if (player_note != null) {
                setNote(player_note, renderer);
            }


        }

        setSize(width, height);
    }

    private void setNote(String player_note,HudRenderer renderer){
        String note = "Note: " + player_note;
        updateSize(renderer, note,false);
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
