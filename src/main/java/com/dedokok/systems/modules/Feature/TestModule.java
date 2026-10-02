package com.dedokok.systems.modules.Feature;

import com.dedokok.events.CoreProtect.CoreProtectEvent;
import com.dedokok.events.world.TickEvent;
import com.dedokok.settings.*;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Module;
import com.dedokok.utils.classes.CoreProtect.MessagesTypes;
import com.dedokok.utils.misc.text.MessageToSend;
import com.mojang.authlib.GameProfile;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestModule extends Module {

    public TestModule() {
        super(Categories.Feature, "TestModule", "Module for tests",null);
        runInMainMenu=true;
    }

    @EventHandler
    public void onRowMessage(CoreProtectEvent.Row event){
        if(event.type == MessagesTypes.rowBlockBreak) {
            System.out.println("CP: "+event.username+" сломал блок " +event.block +" в "+event.time);
        }
    }

//    @EventHandler
//    public void onSystemMessage(CoreProtectEvent.System event){
//        System.out.println("CP: "+event.type.name());
//    }


}
