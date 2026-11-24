package com.dedokok.systems.modules.Feature;

import com.dedokok.events.packets.PacketEvent;
import com.dedokok.events.world.TickEvent;
import com.dedokok.gui.GuiTheme;
import com.dedokok.gui.widgets.WWidget;
import com.dedokok.gui.widgets.containers.WContainer;
import com.dedokok.gui.widgets.containers.WTable;
import com.dedokok.gui.widgets.containers.WVerticalList;
import com.dedokok.gui.widgets.pressable.WButton;
import com.dedokok.gui.widgets.pressable.WMinus;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Feature.AutoReveiver.AutoReceiveBlock;
import com.dedokok.systems.modules.Feature.AutoReveiver.InstructionFactory;
import com.dedokok.systems.modules.Module;
import com.dedokok.utils.classes.SendMessagesTypes;
import com.dedokok.utils.misc.text.MessageToSend;


import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;

import java.util.ArrayList;
import java.util.List;

public class AutoReceiver extends Module {

    public AutoReceiver() {
        super(Categories.Feature, "AutoReceiver", "AutoReceiver for messages",null);
        runInMainMenu=true;
    }

    public static ArrayList<MessageToSend>messagesToSend = new ArrayList<>();
    public static List<AutoReceiveBlock> messages = new ArrayList<>();
    private final InstructionFactory factory = new InstructionFactory();





    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if(event.packet instanceof ClientboundSetTitleTextPacket title){
            mc.execute(() -> {
                for(AutoReceiveBlock instruction : messages){
                    if(instruction.getTypeSetting.get() == AutoReceiveBlock.GetTypes.Title) {
                        instruction.doMessage(title.text());
                    }
                }
            });
            return;
        }
        if(event.packet instanceof ClientboundSetSubtitleTextPacket title){
            mc.execute(() -> {
                for(AutoReceiveBlock instruction : messages){
                    if(instruction.getTypeSetting.get() == AutoReceiveBlock.GetTypes.SubTitle) {
                        instruction.doMessage(title.text());
                    }
                }
            });
            return;
        }
    }


    public void onGameMessage(Component message, boolean overlay){
        for(AutoReceiveBlock instruction : messages){
            if(instruction.getTypeSetting.get() == AutoReceiveBlock.GetTypes.Chat) {
                instruction.doMessage(message);
            }
        }
    }

    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();

        ListTag list = new ListTag();
        for (AutoReceiveBlock instruction : messages) {
            CompoundTag mTag = new CompoundTag();
            mTag.putString("type", instruction.getTypeName());
            mTag.put("instruction", instruction.toTag());


            list.add(mTag);
        }
        tag.put("instructions", list);
        return tag;
    }

    public static List<AutoReceiveBlock> readInstructionsFromTag(CompoundTag tag, InstructionFactory factory) {
        List<AutoReceiveBlock> instructions = new ArrayList<>();
        ListTag list = tag.getListOrEmpty("instructions");

        for (Tag tagII : list) {
            CompoundTag tagI = (CompoundTag) tagII;
            String type = tagI.getStringOr("type", "");
            AutoReceiveBlock instruction = factory.createInstruction(type);

            if (instruction != null) {
                CompoundTag instructionTag = (CompoundTag) tagI.get("instruction");
                if (instructionTag != null) instruction.fromTag(instructionTag);
                instructions.add(instruction);
            }
        }

        return instructions;
    }
    public Module fromTag(CompoundTag tag) {
        super.fromTag(tag);

        messages.clear();
        messages = readInstructionsFromTag(tag, factory);

        return this;
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList list = theme.verticalList();
        fillWidget(theme, list);
        return list;
    }

    private void fillWidget(GuiTheme theme, WVerticalList list) {
        list.clear();

        for (AutoReceiveBlock message : messages) {
            message.screen = screen;

            list.add(theme.settings(message.settings)).expandX();

            WContainer container = list.add(theme.horizontalList()).expandX().widget();


            WButton copy = container.add(theme.button("copy")).right().widget();
            copy.tooltip = "Duplicate message.";
            copy.action = () -> {
                messages.add(messages.indexOf(message), message.copy());
                fillWidget(theme, list);
            };

            WMinus remove = container.add(theme.minus()).right().widget();
            remove.tooltip = "Remove message.";
            remove.action = () -> {
                messages.remove(message);
                fillWidget(theme, list);
            };
        }

        if (!messages.isEmpty()) list.add(theme.horizontalSeparator()).expandX();

        WTable controls = list.add(theme.table()).expandX().widget();

        WButton createCommand = controls.add(theme.button("New autoreceive")).expandX().widget();
        createCommand.action = () -> {
            AutoReceiveBlock instruction = new AutoReceiveBlock();
            messages.add(instruction);
            fillWidget(theme, list);
        };

        WButton removeAll = controls.add(theme.button("Remove all messages")).expandX().widget();
        removeAll.action = () -> {
            messages.clear();
            fillWidget(theme, list);
        };

        //MeteoristConfigManager.configManager(theme, list, this);
    }




    @EventHandler
    private void onTick(TickEvent.Post event) {

        if(messagesToSend.isEmpty())return;

        java.util.Iterator<MessageToSend> iterator = messagesToSend.iterator();

        while (iterator.hasNext()) {
            MessageToSend messageToSend = iterator.next();

            if (System.currentTimeMillis() - messageToSend.getTimeGet() > messageToSend.getTimeToWait()) {
                sendMessage(messageToSend.getText(),messageToSend.getType());
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

    private void sendMessage(String message, SendMessagesTypes type){
        if(mc==null || mc.player==null)return;
        //{
        switch(type) {
            case Command: {
                if (mc.getConnection() == null) return;
                mc.getConnection().sendCommand(message);
                break;
            }
            case Chat: {
                if (mc.getConnection() == null) return;
                mc.getConnection().sendChat(message);
                break;
            }
            case System:{
                mc.player.sendSystemMessage(Component.literal(message));
                break;
            }
        }
        //}
        mc.gui.hud.setTitle(Component.literal("Sent message").withStyle(ChatFormatting.GREEN));
        mc.gui.hud.setTimes(10, 70, 20);
        //mc.player.connection.sendChat(message);
    }


}
