package com.dedokok.settings;

import com.dedokok.gui.GuiTheme;
import com.dedokok.gui.WidgetScreen;
import com.dedokok.gui.widgets.pressable.WButton;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ButtonSetting extends Setting {
    public final int width,height;
    public ButtonSetting(String name, String description, WidgetScreen defaultValue, Consumer<WidgetScreen> clickAction, Consumer<Setting<WidgetScreen>> onModuleActivated, IVisible visible, int width, int height) {
        super(name, description, defaultValue, clickAction, onModuleActivated, visible);

        this.width = width;
        this.height = height;
    }


    @Override
    protected Object parseImpl(String str) {
        return null;
    }

    @Override
    protected boolean isValueValid(Object value) {
        return false;
    }

    @Override
    public WidgetScreen getDefaultValue() {
        return (WidgetScreen) defaultValue;
    }



    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return BuiltInRegistries.ITEM.keySet();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        //tag.putString("value", BuiltInRegistries.ITEM.getKey(get()).toString());

        return tag;
    }

    @Override
    protected Object load(CompoundTag tag) {
        return null;
    }

//    @Override
//    public T load(CompoundTag tag) {
////        value = BuiltInRegistries.ITEM.getValue(Identifier.parse(tag.getStringOr("value", "")));
////
////        if (filter != null && !filter.test(value)) {
////            for (Item item : BuiltInRegistries.ITEM) {
////                if (filter.test(item)) {
////                    value = item;
////                    break;
////                }
////            }
////        }
//
//        return get();
//    }
//    public WidgetScreen createScreen(GuiTheme theme) {
//        return this.get().createScreen(theme, this);
//    }


    public static class Builder extends SettingBuilder<Builder, WidgetScreen, ButtonSetting> {

        private int width = 50;
        private int height = 20;

        public Builder() {
            super(null);
        }

        public Builder width(int width) {
            this.width=width;
            return this;
        }
        public Builder height(int height) {
            this.height=height;
            return this;
        }


        @Override
        public ButtonSetting build() {
            return new ButtonSetting(name, description, defaultValue, onChanged, onModuleActivated, visible,width,height);
        }
    }
}
