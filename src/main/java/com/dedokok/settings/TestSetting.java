/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.settings;

import com.dedokok.utils.entity.EntityUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TestSetting extends Setting<Set<EntityType<?>>> {
    public final Predicate<EntityType<?>> filter;

    public TestSetting(String name, String description, Set<EntityType<?>> defaultValue, Consumer<Set<EntityType<?>>> onChanged, Consumer<Setting<Set<EntityType<?>>>> onModuleActivated, IVisible visible, Predicate<EntityType<?>> filter) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);

        this.filter = filter;
    }

    @Override
    public void resetImpl() {
        value = new ObjectOpenHashSet<>(defaultValue);
    }

    @Override
    protected Set<EntityType<?>> parseImpl(String str) {
        return null;
    }

    @Override
    protected boolean isValueValid(Set<EntityType<?>> value) {
        return true;
    }

    @Override
    public Iterable<String> getSuggestions() {
        return null;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        return null;
    }

    @Override
    public Set<EntityType<?>> load(CompoundTag tag) {
        return null;
    }

    public static class Builder extends SettingBuilder<Builder, Set<EntityType<?>>, TestSetting> {
        private Predicate<EntityType<?>> filter;

        public Builder() {
            super(new ObjectOpenHashSet<>(0));
        }

        public Builder defaultValue(EntityType<?>... defaults) {
            return defaultValue(defaults != null ? new ObjectOpenHashSet<>(defaults) : new ObjectOpenHashSet<>(0));
        }

        public Builder onlyAttackable() {
            filter = EntityUtils::isAttackable;
            return this;
        }

        public Builder filter(Predicate<EntityType<?>> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public TestSetting build() {
            return new TestSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, filter);
        }
    }
}
