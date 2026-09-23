/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.settings;

import com.dedokok.systems.modules.Feature.BlockBreakFinder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class VeinsListSetting extends Setting<HashSet<BlockBreakFinder.Vein>> {
    public Predicate<BlockBreakFinder.Vein> filter;

    public VeinsListSetting(String name, String description, HashSet<BlockBreakFinder.Vein> defaultValue, Consumer<HashSet<BlockBreakFinder.Vein>> onChanged, Consumer<Setting<HashSet<BlockBreakFinder.Vein>>> onModuleActivated, Predicate<BlockBreakFinder.Vein> filter, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);

        this.filter = filter;
    }

    @Override
    public void resetImpl() {
        value = new HashSet<>(defaultValue);
    }

    @Override
    protected HashSet<BlockBreakFinder.Vein> parseImpl(String str) {

        return null;
    }

//    @Override
//    protected boolean isValueValid(HashSet<BlockBreakFinder.Vein> value) {
//        return false;
//    }

    @Override
    protected boolean isValueValid(HashSet<BlockBreakFinder.Vein> value) {
        return true;
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return BuiltInRegistries.BLOCK.keySet();
    }

//    @Override
//    protected boolean isValueValid(String value) {
//        return false;
//    }


    @Override
    protected CompoundTag save(CompoundTag tag) {
//        ListTag valueTag = new ListTag();
//        for (BlockBreakFinder.Vein block : get()) {
//            valueTag.add(StringTag.valueOf(BuiltInRegistries.BLOCK.getKey(block).toString()));
//        }
//        tag.put("value", valueTag);

        return null;
    }

    @Override
    protected HashSet<BlockBreakFinder.Vein> load(CompoundTag tag) {
//        get().clear();
//
//        ListTag valueTag = tag.getListOrEmpty("value");
//        for (Tag tagI : valueTag) {
//            Block block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(tagI.asString().orElse("")));
//
//            if (filter == null || filter.test(block)) get().add(block);
//        }

        return null;
    }

    public static class Builder extends SettingBuilder<Builder, HashSet<BlockBreakFinder.Vein>, VeinsListSetting> {
        private Predicate<BlockBreakFinder.Vein> filter;

        public Builder() {
            super(new HashSet<>(0));
        }

        public Builder defaultValue(BlockBreakFinder.Vein... defaults) {
            HashSet<BlockBreakFinder.Vein> defaultSet = (defaults != null)
                    ? new HashSet<>(Arrays.asList(defaults))
                    : new HashSet<>();

            return defaultValue(defaultSet);
        }

        public Builder filter(Predicate<BlockBreakFinder.Vein> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public VeinsListSetting build() {
            return new VeinsListSetting(name, description, defaultValue, onChanged, onModuleActivated, filter, visible);
        }
    }
}
