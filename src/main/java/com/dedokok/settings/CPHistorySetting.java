//package com.dedokok.settings;
//
//import com.dedokok.gui.WidgetScreen;
//import com.dedokok.utils.classes.CoreProtect.HistoryRecordClass;
//import com.dedokok.utils.classes.EntityTypeLimit;
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.nbt.ListTag;
//import net.minecraft.nbt.StringTag;
//import net.minecraft.resources.Identifier;
//
//import java.util.ArrayList;
//import java.util.function.Consumer;
//
//public class CPHistorySetting extends Setting {
//    public final int width,height;
//    public CPHistorySetting(String name, String description, ArrayList<HistoryRecordClass> defaultValue, Consumer<ArrayList<HistoryRecordClass>> clickAction, Consumer<Setting<ArrayList<HistoryRecordClass>>> onModuleActivated, IVisible visible, int width, int height) {
//        super(name, description, defaultValue, clickAction, onModuleActivated, visible);
//        this.width = width;
//        this.height = height;
//    }
//
//
//    @Override
//    protected Object parseImpl(String str) {
//        return null;
//    }
//
//    @Override
//    protected boolean isValueValid(Object value) {
//        return false;
//    }
//
//    @Override
//    public WidgetScreen getDefaultValue() {
//        return (WidgetScreen) defaultValue;
//    }
//
//
//
//    @Override
//    public Iterable<Identifier> getIdentifierSuggestions() {
//        return BuiltInRegistries.ITEM.keySet();
//    }
//
//    @Override
//    public CompoundTag sav0e(CompoundTag tag) {
//        //tag.putString("value", BuiltInRegistries.ITEM.getKey(get()).toString());
////        ListTag valueType = new ListTag();
////        ListTag limitTag = new ListTag();
////        ListTag isEnabledTag = new ListTag();
////        for (EntityTypeLimit entityType : get()) {
////            valueType.add(StringTag.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(entityType.type).toString()));
////            limitTag.add(StringTag.valueOf(String.valueOf(entityType.limit)));
////            isEnabledTag.add(StringTag.valueOf(String.valueOf(entityType.isEnabled)));
////        }
////        tag.put("type", valueType);
////
////
////        tag.put("limit", limitTag);
////        tag.put("enabled",isEnabledTag);
//        return tag;
//    }
//
//    @Override
//    protected Object load(CompoundTag tag) {
//        return null;
//    }
//
////    @Override
////    public T load(CompoundTag tag) {
//////        value = BuiltInRegistries.ITEM.getValue(Identifier.parse(tag.getStringOr("value", "")));
//////
//////        if (filter != null && !filter.test(value)) {
//////            for (Item item : BuiltInRegistries.ITEM) {
//////                if (filter.test(item)) {
//////                    value = item;
//////                    break;
//////                }
//////            }
//////        }
////
////        return get();
////    }
////    public WidgetScreen createScreen(GuiTheme theme) {
////        return this.get().createScreen(theme, this);
////    }
//
//
//    public static class Builder extends SettingBuilder<Builder, ArrayList<HistoryRecordClass>, CPHistorySetting> {
//
//        private int width = 50;
//        private int height = 20;
//
//        public Builder() {
//            super(null);
//        }
//
//        public Builder width(int width) {
//            this.width=width;
//            return this;
//        }
//        public Builder height(int height) {
//            this.height=height;
//            return this;
//        }
//
//
//        @Override
//        public CPHistorySetting build() {
//            return new CPHistorySetting(name, description, defaultValue, onChanged, onModuleActivated, visible,width,height);
//        }
//    }
//}
