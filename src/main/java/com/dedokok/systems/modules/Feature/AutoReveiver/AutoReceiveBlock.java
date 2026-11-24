package com.dedokok.systems.modules.Feature.AutoReveiver;

import com.dedokok.gui.WidgetScreen;
import com.dedokok.settings.*;
import com.dedokok.utils.classes.SendMessagesTypes;
import com.dedokok.utils.misc.ISerializable;
import com.dedokok.utils.misc.text.MessageToSend;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dedokok.DedTools.mc;
import static com.dedokok.systems.modules.Feature.AutoReceiver.messagesToSend;

public class AutoReceiveBlock implements ISerializable<AutoReceiveBlock> {
    public final Settings settings = new Settings();
    public static final String type = "AutoReceive";
    public WidgetScreen screen = null;

    public AutoReceiveBlock() {}


    protected final SettingGroup sgGeneral = settings.createGroup("Messages");


    public final Setting<GetTypes> getTypeSetting = sgGeneral.add(new EnumSetting.Builder<GetTypes>()
            .name("get-type")
            .description("What type of get messages")
            .defaultValue(GetTypes.Chat)
            .build()
    );

    public enum GetTypes{
        Chat,
        Title,
        SubTitle
    }

    public final Setting<String> messageTextSetting = sgGeneral.add(new StringSetting.Builder()
                    .name("message-get")
                    .description("On what text should receive (regex)")
                    .defaultValue("(.+): Hello server!")
                    .build()
    );

    public final Setting<Boolean> isRandomReceiveTimeSetting = sgGeneral.add(new BoolSetting.Builder()
            .name("is-random-receive-time")
            .description("Receive on message after random time between after and before")
            .defaultValue(true)
                    .onChanged(value ->{
                        if(value) {
                            checkRules(2);
                        }
                        reload();
                    })
            .build()
    );

    public final Setting<Integer> afterTimeSetting = sgGeneral.add(new IntSetting.Builder()
            .name("after-time")
            .description("After what time should receive. In seconds")
            .defaultValue(0)
            .min(0)
            .max(1000)
                    .onChanged(_ -> {
                        if(isRandomReceiveTimeSetting.get()) {
                            checkRules(1);
                        }
                    })
            .sliderMax(100)
            .build()
    );

    public final Setting<Integer> beforeTimeSetting = sgGeneral.add(new IntSetting.Builder()
            .name("before-time")
            .description("Before what time should receive. In seconds")
            .defaultValue(1)
            .min(1)
            .max(1000)
            .onChanged(_->{
                if(isRandomReceiveTimeSetting.get()) {
                    checkRules(2);
                }
            })
            .sliderMax(100)
            .visible(isRandomReceiveTimeSetting::get)
            .build()
    );

    public final Setting<SendMessagesTypes> sendTypeSetting = sgGeneral.add(new EnumSetting.Builder<SendMessagesTypes>()
            .name("send-type")
            .description("What type of string need to send")
            .defaultValue(SendMessagesTypes.Command)
            .build()
    );
    private final Setting<Boolean> showMessageSendSettign = sgGeneral.add(new BoolSetting.Builder()
            .name("show")
            .description("Hide or show message to send")
            .onChanged(_->{
                if(screen==null)return;
                screen.reload();
            })
            .defaultValue(false)
            .build()
    );

    public final Setting<String> messageSendSetting = sgGeneral.add(new StringSetting.Builder()
            .name("message-send")
            .description("What should send in chat. {int from 0} to select regex group")
            .defaultValue("Hello, {0}")
                    .visible(showMessageSendSettign::get)
            .build()
    );



    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.put("settings", settings.toTag());

        return tag;
    }

    public void reload(){
        if(screen==null)return;
        screen.reload();
    }

    public void checkRules(int changed){
        if(afterTimeSetting==null || beforeTimeSetting==null)return;
        if(afterTimeSetting.get()>=beforeTimeSetting.get()){
            switch(changed) {
                case 1: {
                    sendMessage(Component.literal("After time cannot be more or equal than before time").withStyle(ChatFormatting.RED));
                    afterTimeSetting.set(beforeTimeSetting.get() - 1);
                    break;
                }
                case 2: {
                    sendMessage(Component.literal("Before time cannot be lower or equal than after time").withStyle(ChatFormatting.RED));
                    beforeTimeSetting.set(afterTimeSetting.get() + 1);
                    break;
                }
            }


            return;
        }

    }
    public void sendMessage(Component message){
        if(mc==null ||mc.player==null){
            return;
        }
        mc.player.sendSystemMessage(message);
    }

    @Override
    public AutoReceiveBlock fromTag(CompoundTag tag) {
        CompoundTag settingsTag = (CompoundTag) tag.get("settings");
        if (settingsTag != null) settings.fromTag(settingsTag);

        return this;
    }

    public String getTypeName() {
        return type;
    }

    public AutoReceiveBlock copy() {
        return new AutoReceiveBlock().fromTag(toTag());
    }



    public void doMessage(Component message){
        String pattern_string = messageTextSetting.get();

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
            formattedString = formattedString.replaceAll("(\\{" + i + "})", list.get(i));
        }

        //if(isRandomReceiveTimeSetting.get()){
//        if(afterTimeSetting.get()>=beforeTimeSetting.get()){
//            mc.player.sendSystemMessage(Component.literal("After time cannot be more than before time").withStyle(ChatFormatting.RED));
//            return;
//        }
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


        MessageToSend newMessage = new MessageToSend(timeGet,formattedString,timeWait,sendTypeSetting.get());
        messagesToSend.add(newMessage);
        //}
//        else {
//            MessageToSend newMessage = new MessageToSend(timeGet,formattedString,timeWait);
//            messagesToSend.add(newMessage);
//            sendMessage(formattedString);
//        }
    }


}
