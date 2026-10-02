/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.gui.screens.settings;

import com.dedokok.gui.GuiTheme;
import com.dedokok.gui.WindowScreen;
import com.dedokok.gui.utils.Cell;
import com.dedokok.gui.widgets.WWidget;
import com.dedokok.gui.widgets.containers.WSection;
import com.dedokok.gui.widgets.containers.WTable;
import com.dedokok.gui.widgets.containers.WVerticalList;
import com.dedokok.gui.widgets.input.WTextBox;
import com.dedokok.gui.widgets.pressable.WCheckbox;
import com.dedokok.renderer.Texture;
import com.dedokok.settings.EntityTypeLimitsSetting;
import com.dedokok.settings.EntityTypeListSetting;
import com.dedokok.utils.Utils;
import com.dedokok.utils.classes.EntityTypeLimit;
import com.dedokok.utils.misc.Names;
import com.dedokok.utils.render.DisplayItemUtils;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class EntityTypeLimitsSettingScreen extends WindowScreen {
    private static Texture EMPTY_SPAWN_EGG_TEXTURE;

    private final EntityTypeLimitsSetting setting;

    private WVerticalList list;
    private final WTextBox filter;

    private String filterText = "";

    private WSection animals, waterAnimals, monsters, ambient, misc;
    private WTable animalsT, waterAnimalsT, monstersT, ambientT, miscT;
    int hasAnimal = 0, hasWaterAnimal = 0, hasMonster = 0, hasAmbient = 0, hasMisc = 0;

    public EntityTypeLimitsSettingScreen(GuiTheme theme, EntityTypeLimitsSetting setting) {
        super(theme, "Select entities");
        this.setting = setting;

        // Filter
        filter = super.add(theme.textBox("")).minWidth(400).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();

            list.clear();
            initWidgets();
        };

        list = super.add(theme.verticalList()).expandX().widget();

    }

    @Override
    public <W extends WWidget> Cell<W> add(W widget) {
        return list.add(widget);
    }

    @Override
    public void initWidgets() {
        hasAnimal = hasWaterAnimal = hasMonster = hasAmbient = hasMisc = 0;

        for (EntityTypeLimit entityType : setting.get()) {
            if(entityType.isEnabled) {
                if (setting.filter == null || setting.filter.test(entityType)) {
                    switch (entityType.type.getCategory()) {
                        case CREATURE -> hasAnimal++;
                        case WATER_AMBIENT, WATER_CREATURE, UNDERGROUND_WATER_CREATURE, AXOLOTLS -> hasWaterAnimal++;
                        case MONSTER -> hasMonster++;
                        case AMBIENT -> hasAmbient++;
                        case MISC -> hasMisc++;
                    }
                }
            }
        }

        boolean first = animals == null;

        // Animals
        List<EntityTypeLimit> animalsE = new ArrayList<>();
        WCheckbox animalsC = theme.checkbox(hasAnimal > 0);

        animals = theme.section("Animals", animals != null && animals.isExpanded(), animalsC);
        //animalsC.action = () -> tableChecked(animalsE, animalsC.checked);

        Cell<WSection> animalsCell = add(animals).expandX();
        animalsT = animals.add(theme.table()).expandX().widget();

        // Water animals
        List<EntityTypeLimit> waterAnimalsE = new ArrayList<>();
        WCheckbox waterAnimalsC = theme.checkbox(hasWaterAnimal > 0);

        waterAnimals = theme.section("Water Animals", waterAnimals != null && waterAnimals.isExpanded(), waterAnimalsC);
        waterAnimalsC.action = () -> tableChecked(waterAnimalsE, waterAnimalsC.checked);

        Cell<WSection> waterAnimalsCell = add(waterAnimals).expandX();
        waterAnimalsT = waterAnimals.add(theme.table()).expandX().widget();

        // Monsters
        List<EntityTypeLimit> monstersE = new ArrayList<>();
        WCheckbox monstersC = theme.checkbox(hasMonster > 0);

        monsters = theme.section("Monsters", monsters != null && monsters.isExpanded(), monstersC);
        monstersC.action = () -> tableChecked(monstersE, monstersC.checked);

        Cell<WSection> monstersCell = add(monsters).expandX();
        monstersT = monsters.add(theme.table()).expandX().widget();

        // Ambient
        List<EntityTypeLimit> ambientE = new ArrayList<>();
        WCheckbox ambientC = theme.checkbox(hasAmbient > 0);

        ambient = theme.section("Ambient", ambient != null && ambient.isExpanded(), ambientC);
        ambientC.action = () -> tableChecked(ambientE, ambientC.checked);

        Cell<WSection> ambientCell = add(ambient).expandX();
        ambientT = ambient.add(theme.table()).expandX().widget();

        // Misc
        List<EntityTypeLimit> miscE = new ArrayList<>();
        WCheckbox miscC = theme.checkbox(hasMisc > 0);

        misc = theme.section("Misc", misc != null && misc.isExpanded(), miscC);
        miscC.action = () -> tableChecked(miscE, miscC.checked);

        Cell<WSection> miscCell = add(misc).expandX();
        miscT = misc.add(theme.table()).expandX().widget();

        @SuppressWarnings("deprecation") // Use of Item#builtInRegistryHolder
        var spawnEggItems = BuiltInRegistries.ITEM.stream()
            .filter(item -> item.builtInRegistryHolder().areComponentsBound() && item.components().has(DataComponents.ENTITY_DATA))
            .toList();

        Consumer<EntityTypeLimit> entityTypeForEach = entityType -> {
            if (setting.filter == null || setting.filter.test(entityType)) {
                switch (entityType.type.getCategory()) {
                    case CREATURE -> {
                        animalsE.add(entityType);
                        addEntityType(animalsT, animalsC, entityType, spawnEggItems);
                    }
                    case WATER_AMBIENT, WATER_CREATURE, UNDERGROUND_WATER_CREATURE, AXOLOTLS -> {
                        waterAnimalsE.add(entityType);
                        addEntityType(waterAnimalsT, waterAnimalsC, entityType, spawnEggItems);
                    }
                    case MONSTER -> {
                        monstersE.add(entityType);
                        addEntityType(monstersT, monstersC, entityType, spawnEggItems);
                    }
                    case AMBIENT -> {
                        ambientE.add(entityType);
                        addEntityType(ambientT, ambientC, entityType, spawnEggItems);
                    }
                    case MISC -> {
                        miscE.add(entityType);
                        addEntityType(miscT, miscC, entityType, spawnEggItems);
                    }
                }
            }
        };

        // Sort all entities
        if (filterText.isEmpty()) {
            BuiltInRegistries.ENTITY_TYPE.forEach(type -> {


                EntityTypeLimit entityTypeLimit = getEntityTypeLimitByType(type);
                if(entityTypeLimit == null) {
                    entityTypeLimit=new EntityTypeLimit(type,-1);
                }
                entityTypeForEach.accept(entityTypeLimit);
            });
        } else {
            record DiffByType(EntityType<?> type, int diff) {}
            List<DiffByType> entities = new ArrayList<>();
            BuiltInRegistries.ENTITY_TYPE.forEach(entity -> {
                String text = Names.get(entity);
                int words = Utils.searchInWords(text, filterText);
                int diff = Utils.searchLevenshteinDefault(text, filterText, false);

                if (words > 0 || diff < text.length() / 2) entities.add(new DiffByType(entity, diff));
            });
            entities.sort(Comparator.comparingInt(DiffByType::diff));
            for (var pair : entities) {
                //int limit = getLimitByType(pair.type);
                EntityTypeLimit typeLimit = getEntityTypeLimitByType(pair.type);
                if(typeLimit == null) {
                    typeLimit = new EntityTypeLimit(pair.type,-1);
                }
                //EntityTypeLimit entityTypeLimit = new EntityTypeLimit(pair.type,limit);
                entityTypeForEach.accept(typeLimit);
            }
        }

        animalsC.action      = () -> tableChecked(animalsE, animalsC.checked);


        if (animalsT.cells.isEmpty()) list.cells.remove(animalsCell);
        if (waterAnimalsT.cells.isEmpty()) list.cells.remove(waterAnimalsCell);
        if (monstersT.cells.isEmpty()) list.cells.remove(monstersCell);
        if (ambientT.cells.isEmpty()) list.cells.remove(ambientCell);
        if (miscT.cells.isEmpty()) list.cells.remove(miscCell);

        if (first) {
            int totalCount = (hasWaterAnimal + waterAnimals.cells.size() + monsters.cells.size() + ambient.cells.size() + misc.cells.size()) / 2;

            if (totalCount <= 20) {
                if (!animalsT.cells.isEmpty()) animals.setExpanded(true);
                if (!waterAnimalsT.cells.isEmpty()) waterAnimals.setExpanded(true);
                if (!monstersT.cells.isEmpty()) monsters.setExpanded(true);
                if (!ambientT.cells.isEmpty()) ambient.setExpanded(true);
                if (!miscT.cells.isEmpty()) misc.setExpanded(true);
            } else {
                if (!animalsT.cells.isEmpty()) animals.setExpanded(false);
                if (!waterAnimalsT.cells.isEmpty()) waterAnimals.setExpanded(false);
                if (!monstersT.cells.isEmpty()) monsters.setExpanded(false);
                if (!ambientT.cells.isEmpty()) ambient.setExpanded(false);
                if (!miscT.cells.isEmpty()) misc.setExpanded(false);
            }
        }
    }

    public int getLimitByType(EntityType<?> type) {
        Set<EntityTypeLimit> limits = setting.get();
        int limit = -1;
        for(EntityTypeLimit entity_type : limits){
            if(entity_type.type.equals(type)){
                limit=entity_type.limit;
                break;
            }
        }
        return limit;
    }

    private void tableChecked(List<EntityTypeLimit> entityTypes, boolean checked) {
        boolean changed = false;

        for (EntityTypeLimit entityType : entityTypes) {
            entityType.isEnabled=checked;
            if (checked) {
                //setting.get().add(new EntityTypeLimit(entityType.type,entityType.limit));

                addToSetting(entityType);
                changed = true;
            } else {
                //entityType.isEnabled=false;
                //if (removeType(entityType.type)) {
                addToSetting(entityType);
                changed = true;
                //}
            }
        }

        if (changed) {
            list.clear();
            initWidgets();
            setting.onChanged();
        }
    }

    private void addEntityType(WTable table, WCheckbox tableCheckbox, EntityTypeLimit entityType, List<Item> spawnEggItems) {
        // Icon

        ItemStack stack = null;

        for (var item : spawnEggItems) {
            var component = item.components().get(DataComponents.ENTITY_DATA);

            //noinspection DataFlowIssue
            if (component.type() == entityType.type) {
                stack = DisplayItemUtils.toStack(item);
                break;
            }
        }

        if (stack != null) table.add(theme.item(stack));
        else {
            if (EMPTY_SPAWN_EGG_TEXTURE == null) {
                EMPTY_SPAWN_EGG_TEXTURE = Texture.readResource("/assets/dedtools/textures/empty_spawn_egg.png", false, FilterMode.NEAREST);
            }

            table.add(theme.texture(32, 32, 0, EMPTY_SPAWN_EGG_TEXTURE));
        }

        // Name

        table.add(theme.label(Names.get(entityType.type)));


        //textbox
        String limit_string = entityType.limit == -1 ? "" : ""+entityType.limit;
        WTextBox value = table.add(theme.textBox(limit_string)).minWidth(50).right().widget();
        value.action = () -> {
            int limit = -1;
            try{
                limit = Integer.parseInt(value.get());
            }
            catch (NumberFormatException e){
            }
            entityType.limit=limit;
            addToSetting(entityType);
        };



        // Checkbox

//        boolean isContains = false;
//        for(EntityTypeLimit type_limit : setting.get()){
//            if(type_limit.type.equals(entityType.type)){
//                isContains = true;
//                break;
//            }
//        }
        boolean isContains = entityType.isEnabled;

        WCheckbox a = table.add(theme.checkbox(isContains)).expandCellX().right().widget();
        a.action = () -> {
            if (a.checked) {
                entityType.isEnabled=true;
//                if(getEntityTypeLimitByType(entityType.type)!=null){
//                    EntityTypeLimit entityTypeLimit = getEntityTypeLimitByType(entityType.type);
//
//                }
//                setting.get().add(entityType);/////////////////////
                addToSetting(entityType);

                switch (entityType.type.getCategory()) {
                    case CREATURE -> {
                        if (hasAnimal == 0) tableCheckbox.checked = true;
                        hasAnimal++;
                    }
                    case WATER_AMBIENT, WATER_CREATURE, UNDERGROUND_WATER_CREATURE, AXOLOTLS -> {
                        if (hasWaterAnimal == 0) tableCheckbox.checked = true;
                        hasWaterAnimal++;
                    }
                    case MONSTER -> {
                        if (hasMonster == 0) tableCheckbox.checked = true;
                        hasMonster++;
                    }
                    case AMBIENT -> {
                        if (hasAmbient == 0) tableCheckbox.checked = true;
                        hasAmbient++;
                    }
                    case MISC -> {
                        if (hasMisc == 0) tableCheckbox.checked = true;
                        hasMisc++;
                    }
                }
            } else {
                //if (removeType(entityType.type)) {
                entityType.isEnabled=false;
                addToSetting(entityType);
                    switch (entityType.type.getCategory()) {
                        case CREATURE -> {
                            hasAnimal--;
                            if (hasAnimal == 0) tableCheckbox.checked = false;
                        }
                        case WATER_AMBIENT, WATER_CREATURE, UNDERGROUND_WATER_CREATURE, AXOLOTLS -> {
                            hasWaterAnimal--;
                            if (hasWaterAnimal == 0) tableCheckbox.checked = false;
                        }
                        case MONSTER -> {
                            hasMonster--;
                            if (hasMonster == 0) tableCheckbox.checked = false;
                        }
                        case AMBIENT -> {
                            hasAmbient--;
                            if (hasAmbient == 0) tableCheckbox.checked = false;
                        }
                        case MISC -> {
                            hasMisc--;
                            if (hasMisc == 0) tableCheckbox.checked = false;
                        }
                    }
                //}
            }

            setting.onChanged();
        };

        table.row();
    }

    public boolean addToSetting(EntityTypeLimit entityTypeLimit) {
        EntityTypeLimit oldLimit = null;
        for(EntityTypeLimit limit  : setting.get()){
            if(limit.type.equals(entityTypeLimit.type)){
                oldLimit = limit;
                break;
            }
        }
        if(oldLimit != null){
            setting.get().remove(oldLimit);
        }
        setting.get().add(entityTypeLimit);
        return true;
    }

    public boolean removeType(EntityType<?>type){
        EntityTypeLimit toRemove =  null;
        for(EntityTypeLimit type_limit : setting.get()){
            if(type_limit.type.equals(type)){
                toRemove = type_limit;
                break;
            }
        }
        if(toRemove!=null){
            return setting.get().remove(toRemove);
        }
        return false;
    }
    public EntityTypeLimit getEntityTypeLimitByType(EntityType<?> type){
        for(EntityTypeLimit type_limit : setting.get()){
            if(type_limit.type.equals(type)){
                return type_limit;
            }
        }
        return null;
    }
}
