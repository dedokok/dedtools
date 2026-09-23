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
import com.dedokok.gui.widgets.pressable.WMinus;
import com.dedokok.settings.classes.PlayerNote;
import com.dedokok.systems.modules.Feature.PlayerTracker;
import com.dedokok.systems.modules.Feature.PlayersNoteBook;
import com.dedokok.systems.modules.Modules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class PlayerNotesListSetting extends Setting<List<PlayerNote>> {
    public final Class<? extends WTextBox.Renderer> renderer;
    public final CharFilter filter;
    public boolean isTracker;

    public PlayerNotesListSetting(String name, String description, List<PlayerNote> defaultValue, Consumer<List<PlayerNote>> onChanged, Consumer<Setting<List<PlayerNote>>> onModuleActivated, IVisible visible, Class<? extends WTextBox.Renderer> renderer, CharFilter filter, boolean isTracker) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);

        this.renderer = renderer;
        this.filter = filter;
        this.isTracker = isTracker;
    }

    @Override
    protected List<PlayerNote> parseImpl(String str) {
        return null;
    }

    @Override
    protected boolean isValueValid(List<PlayerNote> value) {
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag listUsernames = new ListTag();
        for (int i = 0; i < this.value.size(); i++) {
            listUsernames.add(StringTag.valueOf(get().get(i).username));
        }
        tag.put("username", listUsernames);

        ListTag listDescriptions = new ListTag();
        for (int i = 0; i < this.value.size(); i++) {
            listDescriptions.add(StringTag.valueOf(get().get(i).description));
        }
        tag.put("description", listDescriptions);

        return tag;
    }

    @Override
    public List<PlayerNote> load(CompoundTag tag) {
        get().clear();

        ListTag usernameTag = tag.getListOrEmpty("username");
        ArrayList<String>usernames = new ArrayList<>();
        for (Tag tagI : usernameTag) {
            usernames.add(tagI.asString().orElse(""));
        }

        ListTag descTag = tag.getListOrEmpty("description");

        ListTag isFromTrackerTag = tag.getListOrEmpty("fromtracker");
        //ArrayList<String>descriptions = new ArrayList<>();
        int count = 0;
        for (Tag tagI : descTag) {
            get().add(new PlayerNote(usernames.get(count),tagI.asString().orElse(""),isFromTrackerTag.asBoolean().orElse(false)));
            count++;
        }

        return get();
        //return null;
    }

    @Override
    public void resetImpl() {
        value = new ArrayList<>(defaultValue);
    }

    public static void fillTable(GuiTheme theme, WTable table, PlayerNotesListSetting setting) {
        table.clear();

        ArrayList<PlayerNote> strings = new ArrayList<>(setting.get());
        CharFilter filter = setting.filter == null ? (_, _) -> true : setting.filter;

        for (int i = 0; i < setting.get().size(); i++) {
            int msgI = i;
            PlayerNote note = setting.get().get(i);
            String username = note.getUsername();
            String description = note.getDescription();


            WTextBox textBoxUsername = table.add(theme.textBox(username, filter, setting.renderer)).minWidth(PlayersNoteBook.usernameWidth).expandX().widget();
            WTextBox textBoxNote = table.add(theme.textBox(description, filter, setting.renderer)).minWidth(PlayersNoteBook.descriptionWidth).expandX().widget();


            textBoxUsername.action = () -> strings.set(msgI, new PlayerNote(textBoxUsername.get(),textBoxNote.get(),note.fromTracker));
            textBoxUsername.actionOnUnfocused = () -> setting.set(strings);
           // textBoxUsername.width= PlayersNoteBook.usernameWidth;

            textBoxNote.action = () -> strings.set(msgI, new PlayerNote(textBoxUsername.get(),textBoxNote.get(),note.fromTracker));
            textBoxNote.actionOnUnfocused = () -> setting.set(strings);
            //textBoxNote.width= PlayersNoteBook.descriptionWidth;




            WMinus delete = table.add(theme.minus()).widget();
            delete.action = () -> {
                strings.remove(msgI);
//                setting.set(strings);

                int id = 0;
                boolean isFind = false;
                List<PlayerNote> setting_1 = Modules.get().get(PlayersNoteBook.class).playerNotesSetting.get();
                for(PlayerNote playerNote : setting_1){
                    if(playerNote.username.equals(username)){
                        isFind=true;
                        break;
                    }
                    id++;
                }
                if(isFind){
                    setting_1.remove(id);
                    Modules.get().get(PlayersNoteBook.class).playerNotesSetting.set(setting_1);
                    isFind=false;
                }
                id=0;


                List<PlayerNote> setting_2 = Modules.get().get(PlayersNoteBook.class).playerNotesSetting.get();

                for(PlayerNote playerNote : setting_2){
                    if(playerNote.username.equals(username)){
                        isFind=true;
                        break;
                    }
                    id++;
                }
                if(isFind){
                    setting_2.remove(id);
                    Modules.get().get(PlayerTracker.class).playerListSetting.set(setting_2);
                }


                fillTable(theme, table, setting);
            };

            table.row();
        }

        if (!setting.get().isEmpty()) {
            table.add(theme.horizontalSeparator()).expandX();
            table.row();
        }

        WButton add = table.add(theme.button("Add")).expandX().widget();
        add.action = () -> {
            strings.add(new PlayerNote("","",false));
            setting.set(strings);



            fillTable(theme, table, setting);
        };

        WButton reset = table.add(theme.button(GuiRenderer.RESET)).widget();
        reset.action = () -> {
            setting.reset();

            fillTable(theme, table, setting);
        };
        reset.tooltip = "Reset";
    }

    public static class Builder extends SettingBuilder<Builder, List<PlayerNote>, PlayerNotesListSetting> {
        private Class<? extends WTextBox.Renderer> renderer;
        private CharFilter filter;
        private boolean isTracker;

        public Builder() {
            super(new ArrayList<>(0));
        }

        public Builder defaultValue(PlayerNote... defaults) {
            return defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList<>());
        }

        public Builder renderer(Class<? extends WTextBox.Renderer> renderer) {
            this.renderer = renderer;
            return this;
        }

        public Builder filter(CharFilter filter) {
            this.filter = filter;
            return this;
        }

        public Builder isTracker(boolean isTracker) {
            this.isTracker = isTracker;
            return this;
        }

        @Override
        public PlayerNotesListSetting build() {
            return new PlayerNotesListSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, renderer, filter,isTracker);
        }
    }

}
