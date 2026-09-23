package com.dedokok.systems.modules.Feature;

import com.dedokok.settings.*;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Module;
import com.dedokok.utils.render.color.Color;
import com.dedokok.utils.render.color.SettingColor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ColourChatMessages extends Module {
    private static boolean isActive = false;
    public ColourChatMessages() {
        super(Categories.Feature, "ColourMessages", "Coloured messages",null);
        runInMainMenu=true;
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> isGlobalChat= sgGeneral.add(new BoolSetting.Builder()
            .name("colour-global-chat")
            .description("Make global chat colour")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> isLocalChat = sgGeneral.add(new BoolSetting.Builder()
            .name("colour-local-chat")
            .description("Make local chat colour")
            .defaultValue(false)
            .build()
    );


    public final Setting<SettingColor> localColorSetting = sgGeneral.add(new ColorSetting.Builder()
            .name("local-chat-color")
            .description("Color of local chat")
            .defaultValue(new SettingColor(170, 170, 170))
                    .visible(isLocalChat::get)
            .build()
    );

    public final Setting<SettingColor> globalColorSetting = sgGeneral.add(new ColorSetting.Builder()
            .name("global-chat-color")
            .description("Color of global chat")
            .defaultValue(new SettingColor(85, 85, 85))
            .visible(isGlobalChat::get)
            .build()
    );



    public void parseSiblings(Component sibling, String level){
        List<Component>siblings = sibling.getSiblings();
        int count = 1;
        for(Component component : siblings){
            //System.out.println(level+"."+count+" "+component.getString());
            if(!component.getSiblings().isEmpty()){
                parseSiblings(component,level+"."+count);
            }
            count++;
        }
    }

    public Component onGameMessage(Component message, boolean overlay){
        if(!isActive)return message;
        if(!message.getSiblings().isEmpty()){
            //parseSiblings(message,"1");
//            System.out.println("text last = " +message.getSiblings().getLast().getString());
//            System.out.println("style color = "+message.getSiblings().getFirst().getStyle().getColor());
//            System.out.println("text = "+message.getSiblings().getFirst().getString());
            if(message.getSiblings().getFirst().getString().contains("栗")){
                return makeChat(message, globalColorSetting.get().toTextColor());
                    //ettingColor color = localColorSetting.get();
            }
            else if(message.getSiblings().getFirst().getString().contains("隆")) {
                return makeChat(message, localColorSetting.get().toTextColor());
            }

            }
        return message;
    }


    public Component makeChat(Component message, TextColor color){
        MutableComponent result = Component.empty().withStyle(message.getStyle());
        for(int i = 0; i<message.getSiblings().size(); i++){
            if(i!=message.getSiblings().size()-1){
                result.append(message.getSiblings().get(i));
            }
            else{
                MutableComponent modified = Component.literal(message.getSiblings().get(i).getString()).withColor(color);
                result.append(modified);
            }
        }

        return result;
    }


    @Override
    public void onActivate() {
        isActive = true;
    }

    @Override
    public void onDeactivate(){
        isActive = false;
    }

}
