package com.dedokok.systems.modules.Feature;

import com.dedokok.settings.*;
import com.dedokok.systems.hud.elements.PlayerStatsHud;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Module;
import com.dedokok.systems.modules.Modules;
import com.dedokok.utils.misc.Keybind;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;

import java.util.*;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;

public class Patrol extends Module {
    public Patrol() {
        super(Categories.Feature, "Patrol", "Teleport with keys to players in spectator",null);
        runInMainMenu=true;
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();


    public static int playerIdNow = 0;


    public static UUID target_uuid = null;



//    private final Setting<Integer> maxRowsAmountSetting = sgGeneral.add(new IntSetting.Builder()
//            .name("max-rows-amount")
//            .description("Amount of CoreProtect rows you'll get at one page")
//            .defaultValue(100)
//            .min(1)
//            .build()
//    );


//    private final ButtonSetting startGettingRowsSetting = sgGeneral.add(new ButtonSetting.Builder()
//            .defaultValue(screen)
//            .width(50)
//            .onChanged(_ -> {
//                this.enable();
//                rows.clear();
//                newRow=new Row();
//                startTicks=0;
//                updateOres();
//                isStarted = true;
//            })
//            .name("start-getting-rows")
//            .build()
//    );

    private final Setting<Keybind>keyBindNextSetting = sgGeneral.add(new KeybindSetting.Builder()
            .name("keybind-next-vein")
            .description("Keybind to teleport to the next player")
            .defaultValue(Keybind.fromKey(GLFW_KEY_RIGHT))
                    .action(()->{
                        if(isActive() && mc!=null && mc.gui.screen()==null) {
                            nextPlayer();
                        }
                    })
            .build()
    );
    private final Setting<Keybind>keyBindPrevSetting = sgGeneral.add(new KeybindSetting.Builder()
            .name("keybind-prev-vein")
            .description("Keybind to teleport to the previous player")
            .defaultValue(Keybind.fromKey(GLFW_KEY_LEFT))
            .action(()->{
                if(isActive() && mc!=null && mc.gui.screen()==null) {
                    prevPlayer();
                }
            })
            .build()
    );


    public void nextPlayer(){
        if(mc==null || mc.getConnection()==null || mc.player == null)return;
        playerIdNow++;
        if(playerIdNow>=mc.getConnection().getOnlinePlayers().size()){
            mc.player.sendSystemMessage(Component.literal("Дошёл до конца списка игроков, начал сначала"));
            playerIdNow=0;
        }

        Collection<PlayerInfo> players = mc.getConnection().getOnlinePlayers();
        int count = 0;
        UUID uuid = null;
        for(PlayerInfo player : players){
            if(count==playerIdNow){
                uuid=player.getProfile().id();
                target_uuid=uuid;
            }
            count++;
        }

        mc.getConnection().send(new ServerboundTeleportToEntityPacket(uuid));
        //PlayerStatsHud.idNow =  playerIdNow;
        //PlayerStatsHud.idMax = mc.getConnection().getOnlinePlayers().size();
        return;
    }
    public void prevPlayer(){
        if(mc==null || mc.getConnection()==null || mc.player == null)return;

        playerIdNow--;
        if(playerIdNow<0){
            mc.player.sendSystemMessage(Component.literal("Дошёл до начала списка игроков, начал с конца"));
            playerIdNow=mc.getConnection().getOnlinePlayers().size()-1;
        }

        Collection<PlayerInfo> players = mc.getConnection().getOnlinePlayers();
        int count = 0;
        UUID uuid = null;
        for(PlayerInfo player : players){
            if(count==playerIdNow){
                uuid=player.getProfile().id();
                target_uuid=uuid;

            }
            count++;
        }

        mc.getConnection().send(new ServerboundTeleportToEntityPacket(uuid));
        //PlayerStatsHud.idNow =  playerIdNow;
        //PlayerStatsHud.idMax = mc.getConnection().getOnlinePlayers().size();
        return;
    }



    @Override
    public void onDeactivate(){
        target_uuid=null;
        //sendCoreProtectLookupCommand();
    }




    @Override
    public void onActivate(){
        if(mc==null || mc.player == null || !mc.player.isSpectator())disable();
        playerIdNow=-1;
        if(Modules.get().isActive(BlockBreakFinder.class)){
            Modules.get().get(BlockBreakFinder.class).disable();
        }
        //sendCoreProtectLookupCommand();
    }
}
