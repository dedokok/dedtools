package com.dedokok.systems.modules.Feature;

import com.dedokok.gui.GuiTheme;
import com.dedokok.gui.widgets.WWidget;
import com.dedokok.gui.widgets.containers.WContainer;
import com.dedokok.gui.widgets.containers.WTable;
import com.dedokok.gui.widgets.containers.WVerticalList;
import com.dedokok.gui.widgets.pressable.WButton;
import com.dedokok.gui.widgets.pressable.WMinus;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Feature.MessageAnnounce.MessageAnnouncerBlock;
import com.dedokok.systems.modules.Feature.MessageAnnounce.MessageAnnouncerFactory;
import com.dedokok.systems.modules.Module;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class MessageAnnouncement extends Module {

    public MessageAnnouncement() {
        super(Categories.Feature, "MessageAnnouncer", "Sound and title announcements for messages",null);
        runInMainMenu=true;
    }
    public static List<MessageAnnouncerBlock> messages = new ArrayList<>();
    private final MessageAnnouncerFactory factory = new MessageAnnouncerFactory();

    







    //@EventHandler
    public void onGameMessage(Component message, boolean overlay){
        //if(sender !=null && mc!=null && mc.player !=null && sender.name().equals(mc.player.getName().getString()))return;
        //doMessage(message);
        //mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.SILVERFISH_DEATH,1.0F));
        //mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.,1.0F));
        if(!isActive())return;
        String message_string = message.getString();
        for(MessageAnnouncerBlock instruction : messages){
            instruction.doMessage(message_string);
        }
        

    }


    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();

        ListTag list = new ListTag();
        for (MessageAnnouncerBlock announceMessage : messages) {
            CompoundTag mTag = new CompoundTag();
            mTag.putString("type", announceMessage.getTypeName());
            mTag.put("announceMessage", announceMessage.toTag());


            list.add(mTag);
        }
        tag.put("announceMessages", list);
        return tag;
    }
    public static List<MessageAnnouncerBlock> readannounceMessagesFromTag(CompoundTag tag, MessageAnnouncerFactory factory) {
        List<MessageAnnouncerBlock> announceMessages = new ArrayList<>();
        ListTag list = tag.getListOrEmpty("announceMessages");

        for (Tag tagII : list) {
            CompoundTag tagI = (CompoundTag) tagII;
            String type = tagI.getStringOr("type", "");
            MessageAnnouncerBlock announceMessage = factory.createInstruction(type);

            if (announceMessage != null) {
                CompoundTag announceMessageTag = (CompoundTag) tagI.get("announceMessage");
                if (announceMessageTag != null) announceMessage.fromTag(announceMessageTag);
                announceMessages.add(announceMessage);
            }
        }

        return announceMessages;
    }
    public Module fromTag(CompoundTag tag) {
        super.fromTag(tag);

        messages.clear();
        messages = readannounceMessagesFromTag(tag, factory);

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

        for (MessageAnnouncerBlock message : messages) {
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

        WButton createCommand = controls.add(theme.button("New message")).expandX().widget();
        createCommand.action = () -> {
            MessageAnnouncerBlock announceMessage = new MessageAnnouncerBlock();
            messages.add(announceMessage);
            fillWidget(theme, list);
        };

        WButton removeAll = controls.add(theme.button("Remove all messages")).expandX().widget();
        removeAll.action = () -> {
            messages.clear();
            fillWidget(theme, list);
        };

        //MeteoristConfigManager.configManager(theme, list, this);
    }
    



}
