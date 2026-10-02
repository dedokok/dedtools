/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.settings;

import com.dedokok.utils.classes.EntityTypeLimit;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EntityTypeLimitsSetting extends Setting<Set<EntityTypeLimit>> {
    public final Predicate<EntityTypeLimit> filter;
    private List<String> suggestions;
    private final static List<String> groups = List.of("animal", "wateranimal", "monster", "ambient", "misc");

    public EntityTypeLimitsSetting(String name, String description, Set<EntityTypeLimit> defaultValue, Consumer<Set<EntityTypeLimit>> onChanged, Consumer<Setting<Set<EntityTypeLimit>>> onModuleActivated, IVisible visible, Predicate<EntityTypeLimit> filter) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);

        this.filter = filter;
    }

    @Override
    public void resetImpl() {
        value = new ObjectOpenHashSet<>(defaultValue);
    }

    @Override
    protected Set<EntityTypeLimit> parseImpl(String str) {
        String[] values = str.split(",");
        Set<EntityTypeLimit> entities = new ObjectOpenHashSet<>(values.length);

//        try {
//            for (String value : values) {
//                EntityType<?> entity = parseId(BuiltInRegistries.ENTITY_TYPE, value);
//                if (entity != null) entities.add(entity);
//                else {
//                    String lowerValue = value.trim().toLowerCase();
//                    if (!groups.contains(lowerValue)) continue;
//
//                    for (EntityTypeLimit entityType : BuiltInRegistries.ENTITY_TYPE) {
//                        if (filter != null && !filter.test(entityType)) continue;
//
//                        switch (lowerValue) {
//                            case "animal" -> {
//                                if (entityType.getCategory() == MobCategory.CREATURE) entities.add(entityType);
//                            }
//                            case "wateranimal" -> {
//                                if (entityType.getCategory() == MobCategory.WATER_AMBIENT
//                                    || entityType.getCategory() == MobCategory.WATER_CREATURE
//                                    || entityType.getCategory() == MobCategory.UNDERGROUND_WATER_CREATURE
//                                    || entityType.getCategory() == MobCategory.AXOLOTLS) entities.add(entityType);
//                            }
//                            case "monster" -> {
//                                if (entityType.getCategory() == MobCategory.MONSTER) entities.add(entityType);
//                            }
//                            case "ambient" -> {
//                                if (entityType.getCategory() == MobCategory.AMBIENT) entities.add(entityType);
//                            }
//                            case "misc" -> {
//                                if (entityType.getCategory() == MobCategory.MISC) entities.add(entityType);
//                            }
//                        }
//                    }
//                }
            //}
//        } catch (Exception _) {
//        }

        return entities;
        //return null;
    }

    @Override
    protected boolean isValueValid(Set<EntityTypeLimit> value) {
        return true;
    }

    @Override
    public Iterable<String> getSuggestions() {
        if (suggestions == null) {
            suggestions = new ArrayList<>(groups);
//            ArrayList<EntityType<?>> list = new ArrayList<>();
//            for(EntityTypeLimit typeLimit : get()) {
//                list.add(typeLimit.type);
//            }
            for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
                EntityTypeLimit typeLimit = isContainsType(entityType);
                if(typeLimit == null) {
                    typeLimit = new EntityTypeLimit(entityType,-1);
                }
                if (filter == null || filter.test(typeLimit)) {
                    suggestions.add(BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
                }
            }
        }

        return suggestions;
    }

    public EntityTypeLimit isContainsType(EntityType<?>type){
        for(EntityTypeLimit limit : get()){
            if(limit.type.equals(type)){
                return limit;
            }
        }
        return null;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag valueType = new ListTag();
        ListTag limitTag = new ListTag();
        ListTag isEnabledTag = new ListTag();
        for (EntityTypeLimit entityType : get()) {
            valueType.add(StringTag.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(entityType.type).toString()));
            limitTag.add(StringTag.valueOf(String.valueOf(entityType.limit)));
            isEnabledTag.add(StringTag.valueOf(String.valueOf(entityType.isEnabled)));
        }
        tag.put("type", valueType);


        tag.put("limit", limitTag);
        tag.put("enabled",isEnabledTag);

        return tag;
    }

    @Override
    public Set<EntityTypeLimit> load(CompoundTag tag) {
        get().clear();

        //ArrayList<EntityTypeLimit>entity_type_limits = new ArrayList<>();



        ListTag typeTag = tag.getListOrEmpty("type");
        ListTag limitTag = tag.getListOrEmpty("limit");
        ListTag enabledTag = tag.getListOrEmpty("enabled");

        for (int i = 0; (i<typeTag.size())&&(i<limitTag.size()) && (i<enabledTag.size()); i++) {
            Tag tagType = typeTag.get(i);
            Tag tagLimit = limitTag.get(i);
            Tag tagEnabled = enabledTag.get(i);

            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(tagType.asString().orElse("")));
            int limit = -1;
            try{
                limit = Integer.parseInt(tagLimit.asString().orElse("-1"));
            } catch (NumberFormatException _) {

            }

            boolean isEnabled = Boolean.parseBoolean(tagEnabled.asString().orElse("false"));

            EntityTypeLimit typeLimit = new EntityTypeLimit(type,limit);
            typeLimit.isEnabled=isEnabled;
            get().add(typeLimit);
        }
        //return get();
        //Set<EntityTypeLimit>entities = new HashSet<>(entity_type_limits);
        return get();
    }

    public static class Builder extends SettingBuilder<Builder, Set<EntityTypeLimit>, EntityTypeLimitsSetting> {
        private Predicate<EntityTypeLimit> filter;

        public Builder() {
            super(new ObjectOpenHashSet<>(0));
        }

        public Builder defaultValue(EntityTypeLimit... defaults) {
            return defaultValue(defaults != null ? new ObjectOpenHashSet<>(defaults) : new ObjectOpenHashSet<>(0));
        }

        public Builder onlyAttackable() {
            //filter = EntityUtils::isAttackable;
            return this;
        }

        public Builder filter(Predicate<EntityTypeLimit> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public EntityTypeLimitsSetting build() {
            return new EntityTypeLimitsSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, filter);
        }
    }
}
