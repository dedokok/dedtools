/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.settings;

import com.dedokok.gui.GuiTheme;
import com.dedokok.gui.renderer.GuiRenderer;
import com.dedokok.gui.utils.CharFilter;
import com.dedokok.gui.widgets.containers.WTable;
import com.dedokok.gui.widgets.input.WTextBox;
import com.dedokok.gui.widgets.pressable.WButton;
import com.dedokok.gui.widgets.pressable.WCheckbox;
import com.dedokok.gui.widgets.pressable.WMinus;
import com.dedokok.systems.modules.Feature.CirclePlayer.CirclePlayerClass;
import com.dedokok.systems.modules.Feature.CirclePlayerCommand;
import com.dedokok.systems.modules.Modules;
import net.minecraft.nbt.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class CirclePlayerSetting extends Setting<List<CirclePlayerClass>> {
    public final Class<? extends WTextBox.Renderer> renderer;
    public final CharFilter filter;
//    public boolean isTracker;

    public CirclePlayerSetting(String name, String description, List<CirclePlayerClass> defaultValue, Consumer<List<CirclePlayerClass>> onChanged, Consumer<Setting<List<CirclePlayerClass>>> onModuleActivated, IVisible visible, Class<? extends WTextBox.Renderer> renderer, CharFilter filter, boolean isTracker) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);

        this.renderer = renderer;
        this.filter = filter;
//        this.isTracker = isTracker;
    }

    @Override
    protected List<CirclePlayerClass> parseImpl(String str) {
        return null;
    }

    @Override
    protected boolean isValueValid(List<CirclePlayerClass> value) {
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag listStrings = new ListTag();
        ListTag listTypes = new ListTag();
        ListTag listEnabled = new ListTag();
        for(CirclePlayerClass sendClass : value){

            //CirclePlayerClass cc = new CirclePlayerClass()
            listStrings.add(StringTag.valueOf(sendClass.sendString));
            listTypes.add(StringTag.valueOf(sendClass.type.name()));
            listEnabled.add(ByteTag.valueOf(sendClass.enabled));
        }

        tag.put("listStrings", listStrings);
        tag.put("listTypes", listTypes);
        tag.put("listEnabled", listEnabled);

        return tag;
    }

    @Override
    public List<CirclePlayerClass> load(CompoundTag tag) {
        get().clear();

        ListTag listStringsTag = tag.getListOrEmpty("listStrings");
        ListTag listTypesTag = tag.getListOrEmpty("listTypes");
        ListTag listEnabledTag = tag.getListOrEmpty("listEnabled");

        int count = 0;
        for(int i = 0;i < listStringsTag.size();i++){
            CirclePlayerCommand.MessageTypes messageType = CirclePlayerCommand.MessageTypes.valueOf(listTypesTag.get(i).asString().orElse("Command"));
            boolean enabled = listEnabledTag.get(i).asBoolean().orElse(false);
            String sendString = listStringsTag.get(i).asString().orElse("");
            get().add(new CirclePlayerClass(sendString,enabled,messageType));
        }
//        for (Tag tagI : listTypesTag) {
//            get().add(new CirclePlayerClass(listStrings.get(count),tagI.asString().orElse(""),isFromTrackerTag.asBoolean().orElse(false)));
//            count++;
//        }

        return get();
        //return null;
    }

    @Override
    public void resetImpl() {
        value = new ArrayList<>(defaultValue);
    }

//    public static void fillTable(GuiTheme theme, WTable table, CirclePlayerSetting setting) {
//        table.clear();
//
//        ArrayList<CirclePlayerClass> strings = new ArrayList<>(setting.get());
//        CharFilter filter = setting.filter == null ? (_, _) -> true : setting.filter;
//
//        for (int i = 0; i < setting.get().size(); i++) {
//            int msgI = i;
//            CirclePlayerClass send = setting.get().get(i);
//            //String sendString = send.sendString;
//            // sendType = send.type;
//
//
//
//            WTextBox sendStringTextBox = table.add(theme.textBox(send.sendString, filter, setting.renderer)).expandX().widget();
//            WCheckbox enabledCheckBox = table.add(theme.checkbox(send.enabled)).expandX().widget();
//            WEn
//
//
//            sendStringTextBox.action = () -> strings.set(msgI, new CirclePlayerClass(sendStringTextBox.get(),enabledCheckBox.checked,note.fromTracker));
//            textBoxUsername.actionOnUnfocused = () -> setting.set(strings);
//           // textBoxUsername.width= PlayersNoteBook.usernameWidth;
//
//            textBoxNote.action = () -> strings.set(msgI, new PlayerNote(textBoxUsername.get(),textBoxNote.get(),note.fromTracker));
//            textBoxNote.actionOnUnfocused = () -> setting.set(strings);
//            //textBoxNote.width= PlayersNoteBook.descriptionWidth;
//
//
//
//
//            WMinus delete = table.add(theme.minus()).widget();
//            delete.action = () -> {
//                strings.remove(msgI);
////                setting.set(strings);
//
//                int id = 0;
//                boolean isFind = false;
//                List<CirclePlayerClass> setting_1 = Modules.get().get(PlayersNoteBook.class).playerNotesSetting.get();
//                for(CirclePlayerClass playerNote : setting_1){
//                    if(playerNote.username.equals(username)){
//                        isFind=true;
//                        break;
//                    }
//                    id++;
//                }
//                if(isFind){
//                    setting_1.remove(id);
//                    Modules.get().get(PlayersNoteBook.class).playerNotesSetting.set(setting_1);
//                    isFind=false;
//                }
//                id=0;
//
//
//                List<CirclePlayerClass> setting_2 = Modules.get().get(PlayersNoteBook.class).playerNotesSetting.get();
//
//                for(CirclePlayerClass playerNote : setting_2){
//                    if(playerNote.username.equals(username)){
//                        isFind=true;
//                        break;
//                    }
//                    id++;
//                }
//                if(isFind){
//                    setting_2.remove(id);
//                    Modules.get().get(PlayerTracker.class).playerListSetting.set(setting_2);
//                }
//
//
//                fillTable(theme, table, setting);
//            };
//
//            table.row();
//        }
//
//        if (!setting.get().isEmpty()) {
//            table.add(theme.horizontalSeparator()).expandX();
//            table.row();
//        }
//
//        WButton add = table.add(theme.button("Add")).expandX().widget();
//        add.action = () -> {
//            strings.add(new PlayerNote("","",false));
//            setting.set(strings);
//
//
//
//            fillTable(theme, table, setting);
//        };
//
//        WButton reset = table.add(theme.button(GuiRenderer.RESET)).widget();
//        reset.action = () -> {
//            setting.reset();
//
//            fillTable(theme, table, setting);
//        };
//        reset.tooltip = "Reset";
//    }
//
//    public static class Builder extends SettingBuilder<Builder, List<PlayerNote>, CirclePlayerSetting> {
//        private Class<? extends WTextBox.Renderer> renderer;
//        private CharFilter filter;
//        private boolean isTracker;
//
//        public Builder() {
//            super(new ArrayList<>(0));
//        }
//
//        public Builder defaultValue(PlayerNote... defaults) {
//            return defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList<>());
//        }
//
//        public Builder renderer(Class<? extends WTextBox.Renderer> renderer) {
//            this.renderer = renderer;
//            return this;
//        }
//
//        public Builder filter(CharFilter filter) {
//            this.filter = filter;
//            return this;
//        }
//
//        public Builder isTracker(boolean isTracker) {
//            this.isTracker = isTracker;
//            return this;
//        }
//
//        @Override
//        public CirclePlayerSetting build() {
//            return new CirclePlayerSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, renderer, filter,isTracker);
//        }
//    }

}
