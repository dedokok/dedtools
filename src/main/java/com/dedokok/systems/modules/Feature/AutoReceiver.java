package com.dedokok.systems.modules.Feature;

import com.dedokok.DedTools;
import com.dedokok.events.game.ChatMessageEvent;
import com.dedokok.events.game.ReceiveMessageEvent;
import com.dedokok.events.meteor.CharTypedEvent;
import com.dedokok.events.world.TickEvent;
import com.dedokok.gui.WidgetScreen;
import com.dedokok.settings.*;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Category;
import com.dedokok.systems.modules.Module;
import com.dedokok.utils.Utils;
import com.dedokok.utils.misc.MeteorStarscript;
import com.dedokok.utils.misc.text.MessageToSend;
import com.dedokok.utils.player.ChatUtils;
import com.mojang.authlib.GameProfile;



import meteordevelopment.orbit.EventHandler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.*;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.realms.RealmsScreen;
import org.w3c.dom.Text;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoReceiver extends Module {

    public AutoReceiver() {
        super(Categories.Feature, "AutoReceiver", "AutoReceiver for messages",null);
        runInMainMenu=true;
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private static ArrayList<MessageToSend>messagesToSend = new ArrayList<>();

    private final Setting<String> messageTextSetting = sgGeneral.add(new StringSetting.Builder()
            .name("message-get")
            .description("On what text should receive")
            .defaultValue("^\uF859 игрок (\\b[a-zA-Z0-9_]+\\b) впервые присоединился!$")
//            .onChanged(newValue -> {
//                DedTools.LOG.info("[DEBUG] messageTextSetting changed to: {}", newValue);
//                if(mc!=null && mc.player!=null) {
//                    mc.player.sendSystemMessage(Component.literal("Изменил паттерн на " + newValue));
//                }w
//                })
            .build()
    );

    private final Setting<Boolean> isRandomReceiveTimeSetting = sgGeneral.add(new BoolSetting.Builder()
            .name("is-random-receive-time")
            .description("Receive on message after random time between after and before")
            .defaultValue(true)
            .build()
    );





    private final Setting<Integer> afterTimeSetting = sgGeneral.add(new IntSetting.Builder()
            .name("after-time")
            .description("After what time should receive. In seconds")
            .defaultValue(0)
            .min(0)
            .max(1000)
            .sliderMax(100)
            .build()
    );

    private final Setting<Integer> beforeTimeSetting = sgGeneral.add(new IntSetting.Builder()
            .name("before-time")
            .description("Before what time should receive. In seconds")
            .defaultValue(1)
            .min(1)
            .max(1000)
            .sliderMax(100)
            .visible(isRandomReceiveTimeSetting::get)
            .build()
    );

    private final Setting<String> messageSendSetting = sgGeneral.add(new StringSetting.Builder()
            .name("message-send")
            .description("What should send in chat. {int from 0} to select regex group")
            .defaultValue("Привет, {0}")
            .build()
    );



    public void onChatMessage(Component message, PlayerChatMessage playerChatMessage, GameProfile sender, ChatType.Bound boundChatType, Instant timeStamp){
        doMessage(message);
    }

    //@EventHandler
    public void onGameMessage(Component message, boolean overlay){
        //if(sender !=null && mc!=null && mc.player !=null && sender.name().equals(mc.player.getName().getString()))return;
        doMessage(message);
    }

    private void doMessage(Component message){
        String pattern_string = messageTextSetting.get();
        String pattern_username = "^\\| . (\\b[a-zA-Z0-9_]+\\b):";
        Pattern pattern_1 = Pattern.compile(pattern_username);
        Matcher matcher_1 = pattern_1.matcher(message.getString());

        if(matcher_1.find()){
            String username = matcher_1.group(1);
            if(mc!=null && mc.player!=null && username.equals(mc.player.getName().getString()))return;
        }


        Pattern pattern_2 = Pattern.compile(pattern_string);
        Matcher matcher_2 = pattern_2.matcher(message.getString());
        if(!matcher_2.find()) {
            return;
        }
        List<String> list = new ArrayList<>();
        for(int i = 1; i<matcher_2.groupCount()+1;i++){
            list.add(matcher_2.group(i));
        }
        String formattedString = messageSendSetting.get();
        for (int i = 0; i < list.size(); i++) {
            formattedString = formattedString.replace("{" + i + "}", list.get(i));
        }

        //if(isRandomReceiveTimeSetting.get()){
            if(afterTimeSetting.get()>=beforeTimeSetting.get()){
                mc.player.sendSystemMessage(Component.literal("Время after не может быть больше или равно времени before, отправка сообщения отменена").withStyle(ChatFormatting.RED));
                return;
            }
            if(beforeTimeSetting.get()<=afterTimeSetting.get()){
                mc.player.sendSystemMessage(Component.literal("Время before не может быть меньше или равно времени after, отправка сообщения отменена").withStyle(ChatFormatting.RED));
                return;
            }
            Long timeGet = System.currentTimeMillis();
            int timeWait = 0;
            if(isRandomReceiveTimeSetting.get()){
                int timeAfter = afterTimeSetting.get()*1000;
                int timeBefore = (beforeTimeSetting.get()+1)*1000;
                timeWait = new java.util.Random().nextInt(timeAfter, timeBefore);
            }
            else{
                timeWait = afterTimeSetting.get()*1000;
            }


            MessageToSend newMessage = new MessageToSend(timeGet,formattedString,timeWait);
            messagesToSend.add(newMessage);
        //}
//        else {
//            MessageToSend newMessage = new MessageToSend(timeGet,formattedString,timeWait);
//            messagesToSend.add(newMessage);
//            sendMessage(formattedString);
//        }
    }


    @EventHandler
    private void onTick(TickEvent.Post event) {

        if(messagesToSend.isEmpty())return;

        java.util.Iterator<MessageToSend> iterator = messagesToSend.iterator();

        while (iterator.hasNext()) {
            MessageToSend messageToSend = iterator.next();

            if (System.currentTimeMillis() - messageToSend.getTimeGet() > messageToSend.getTimeToWait()) {
                sendMessage(messageToSend.getText());
                iterator.remove();
            }
        }
    }


    @Override
    public void onActivate() {
        messagesToSend.clear();
    }

    @Override
    public void onDeactivate() {
        messagesToSend.clear();
    }

    private void sendMessage(String message){
        mc.gui.hud.setTitle(Component.literal("Отправил сообщение").withStyle(ChatFormatting.GREEN));
        mc.gui.hud.setTimes(10, 70, 20);
        mc.player.connection.sendChat(message);
    }


}
