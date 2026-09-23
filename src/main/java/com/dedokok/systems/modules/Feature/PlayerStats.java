package com.dedokok.systems.modules.Feature;
import com.dedokok.DedTools;
import com.dedokok.events.packets.PacketEvent;
import com.dedokok.events.world.TickEvent;
import com.dedokok.gui.utils.StarscriptTextBoxRenderer;
import com.dedokok.mixin.ServerboundTeleportToEntityPacketAccessor;
import com.dedokok.settings.*;
import com.dedokok.systems.hud.elements.PlayerStatsHud;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Module;
import com.dedokok.utils.other.TelegramNotifier;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerStats extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public static UUID targetUUID = null;


    private final Setting<List<String>> tpCommandsSetting = sgGeneral.add(new StringListSetting.Builder()
            .name("tp-commands")
            .description("Teleport to player commands")
            .renderer(StarscriptTextBoxRenderer.class)
                    .defaultValue(List.of("^tp (\\b[a-zA-Z0-9_]+\\b)$"))
            .build()
    );

    private final Setting<Integer> timeBeforeRemovePlayerStatsAterUnloadSetting = sgGeneral.add(new IntSetting.Builder()
            .name("time-before-remove-player-stats")
            .description("How many time in seconds wait before remove player stats after unload it")
            .defaultValue(60)
            .min(0)
            .sliderMax(600)
            .build()
    );



    public PlayerStats() {
        super(Categories.Feature, "PlayerStats", "Stats of player you teleported",null);
        runInMainMenu = true;
    }




    int ticks = 0;

    @EventHandler
    public void onTick(TickEvent.Post event){
        if(mc==null || mc.level==null)return;
        PlayerStatsHud.targetUUID = targetUUID;

        if(mc.level.getEntity(targetUUID)!=null){
            ticks=0;
        }
        else{
            ticks++;
        }

        if(ticks>0 && ticks>(timeBeforeRemovePlayerStatsAterUnloadSetting.get()*20)){
            ticks=0;
            targetUUID = null;
        }

    }

    @EventHandler
    public void onPacket(PacketEvent.Send event){
        //event.packet.type().toString();
        if(event.packet instanceof ServerboundTeleportToEntityPacket packet){
             //System.out.println("пакет телепорта");
             UUID uuid = ((ServerboundTeleportToEntityPacketAccessor) packet).getTargetUuid();
             targetUUID = uuid;
             //String username = getNickByUUID(uuid);
             //sendResult(username,uuid);
        }
        else if(event.packet instanceof ServerboundChatCommandPacket packet){
            //System.out.println("пакет команды");
            findCommand(packet.command());
        }
//        else{
//            System.out.println("другой пакет: "+event.packet.getClass().getName());
//        }

    }


    public boolean findCommand(String command){
        String pattern_username = "(\\b[a-zA-Z0-9_]+\\b)";
        for(String pattern_string : tpCommandsSetting.get()) {

            pattern_string = pattern_string.replaceAll("%player%",pattern_username);
            Pattern pattern = Pattern.compile(pattern_string);
            Matcher matcher = pattern.matcher(command);

            if (matcher.find()) {
                String username = matcher.group(1);
                UUID uuid = getUUIDByNick(username);
                if(uuid!=null){
                    //sendResult(username,uuid);
                    targetUUID = uuid;
                    return true;
                }
            }
        }
        return false;
    }

    public String getNickByUUID(UUID uuid){
        for(PlayerInfo player : mc.player.connection.getOnlinePlayers()){
            if(player.getProfile().id().equals(uuid)){
                return player.getProfile().name();
            }
        }
        return null;
    }

    public UUID getUUIDByNick(String username){
        for(PlayerInfo player : mc.player.connection.getOnlinePlayers()){
            if(player.getProfile().name().equals(username)){
                return player.getProfile().id();
            }
        }
        return null;
    }

    public void sendResult(String username, UUID uuid){
        mc.player.sendSystemMessage(Component.literal("Телепорт к "+username));
    }
    //@EventHandler
//    private void onReceivePacket(PacketEvent.Receive event) {
//        if (event.packet instanceof ClientboundSetTimePacket) {
//            System.out.println("принялпакет 1");
//        }
//        else{
//            System.out.println("принялпакет 2");
//        }
//    }




}
