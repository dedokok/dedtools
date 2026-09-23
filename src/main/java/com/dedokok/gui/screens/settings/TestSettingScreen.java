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
//import com.dedokok.settings.EntityTypeListSetting;
import com.dedokok.settings.TestSetting;
import com.dedokok.utils.Utils;
import com.dedokok.utils.misc.Names;
import com.dedokok.utils.render.DisplayItemUtils;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class TestSettingScreen extends WindowScreen {
    private static Texture EMPTY_SPAWN_EGG_TEXTURE;

    private final TestSetting setting;

    private WVerticalList list;
    private final WTextBox filter;

    private String filterText = "";
    private WSection animals;
    private WTable animalsT;

    int hasAnimal = 0, hasWaterAnimal = 0, hasMonster = 0, hasAmbient = 0, hasMisc = 0;

    public TestSettingScreen(GuiTheme theme, TestSetting setting) {
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

        for (EntityType<?> entityType : setting.get()) {
            if (setting.filter == null || setting.filter.test(entityType)) {
                switch (entityType.getCategory()) {
                    case CREATURE -> hasAnimal++;
                    case WATER_AMBIENT, WATER_CREATURE, UNDERGROUND_WATER_CREATURE, AXOLOTLS -> hasWaterAnimal++;
                    case MONSTER -> hasMonster++;
                    case AMBIENT -> hasAmbient++;
                    case MISC -> hasMisc++;
                }
            }
        }

        boolean first = animals == null;

        // Animals
        List<EntityType<?>> animalsE = new ArrayList<>();
        WCheckbox animalsC = theme.checkbox(hasAnimal > 0);

        animals = theme.section("Animals", animals != null && animals.isExpanded(), animalsC);
        animalsC.action = () -> tableChecked(animalsE, animalsC.checked);

        Cell<WSection> animalsCell = add(animals).expandX();
        animalsT = animals.add(theme.table()).expandX().widget();



        @SuppressWarnings("deprecation") // Use of Item#builtInRegistryHolder
        var spawnEggItems = BuiltInRegistries.ITEM.stream()
            .filter(item -> item.builtInRegistryHolder().areComponentsBound() && item.components().has(DataComponents.ENTITY_DATA))
            .toList();

        Consumer<EntityType<?>> entityTypeForEach = entityType -> {
            if (setting.filter == null || setting.filter.test(entityType)) {
                switch (entityType.getCategory()) {
                    case CREATURE -> {
                        animalsE.add(entityType);
                        addEntityType(animalsT, animalsC, entityType, spawnEggItems);
                    }
                }
            }
        };

        // Sort all entities
//        if (filterText.isEmpty()) {
//            BuiltInRegistries.ENTITY_TYPE.forEach(entityTypeForEach);
//        } else {
//            //record DiffByType(EntityType<?> type, int diff) {}
//            //List<DiffByType> entities = new ArrayList<>();
////            BuiltInRegistries.ENTITY_TYPE.forEach(entity -> {
////                String text = Names.get(entity);
////                int words = Utils.searchInWords(text, filterText);
////                int diff = Utils.searchLevenshteinDefault(text, filterText, false);
////
////                if (words > 0 || diff < text.length() / 2) entities.add(new DiffByType(entity, diff));
////            });
//            //entities.sort(Comparator.comparingInt(DiffByType::diff));
//            //for (var pair : entities) entityTypeForEach.accept(pair.type);
//        }

        //if (animalsT.cells.isEmpty()) list.cells.remove(animalsCell);

//        if (first) {
//            int totalCount = (hasWaterAnimal) / 2;
//
//            if (totalCount <= 20) {
//                if (!animalsT.cells.isEmpty()) animals.setExpanded(true);
//            } else {
//                if (!animalsT.cells.isEmpty()) animals.setExpanded(false);
//            }
//        }
    }

    private void tableChecked(List<EntityType<?>> entityTypes, boolean checked) {
        boolean changed = false;

        for (EntityType<?> entityType : entityTypes) {
            if (checked) {
                setting.get().add(entityType);
                changed = true;
            } else {
                if (setting.get().remove(entityType)) {
                    changed = true;
                }
            }
        }

        if (changed) {
            list.clear();
            initWidgets();
            //setting.onChanged();
        }
    }

    private void addEntityType(WTable table, WCheckbox tableCheckbox, EntityType<?> entityType, List<Item> spawnEggItems) {
        // Icon

        ItemStack stack = null;

        for (var item : spawnEggItems) {
            var component = item.components().get(DataComponents.ENTITY_DATA);

            //noinspection DataFlowIssue
            if (component.type() == entityType) {
                stack = DisplayItemUtils.toStack(item);
                break;
            }
        }

        if (stack != null) table.add(theme.item(stack));
        else {
            if (EMPTY_SPAWN_EGG_TEXTURE == null) {
                EMPTY_SPAWN_EGG_TEXTURE = Texture.readResource("/assets/meteor-client/textures/empty_spawn_egg.png", false, FilterMode.NEAREST);
            }

            table.add(theme.texture(32, 32, 0, EMPTY_SPAWN_EGG_TEXTURE));
        }

        // Name

        table.add(theme.label(Names.get(entityType)));

        // Checkbox

        WCheckbox a = table.add(theme.checkbox(setting.get().contains(entityType))).expandCellX().right().widget();
        a.action = () -> {
            if (a.checked) {
                setting.get().add(entityType);
                switch (entityType.getCategory()) {
                    case CREATURE -> {
                        if (hasAnimal == 0) tableCheckbox.checked = true;
                        hasAnimal++;
                    }
//                    case WATER_AMBIENT, WATER_CREATURE, UNDERGROUND_WATER_CREATURE, AXOLOTLS -> {
//                        if (hasWaterAnimal == 0) tableCheckbox.checked = true;
//                        hasWaterAnimal++;
//                    }
//                    case MONSTER -> {
//                        if (hasMonster == 0) tableCheckbox.checked = true;
//                        hasMonster++;
//                    }
//                    case AMBIENT -> {
//                        if (hasAmbient == 0) tableCheckbox.checked = true;
//                        hasAmbient++;
//                    }
//                    case MISC -> {
//                        if (hasMisc == 0) tableCheckbox.checked = true;
//                        hasMisc++;
//                    }
                }
            } else {
                if (setting.get().remove(entityType)) {
                    switch (entityType.getCategory()) {
                        case CREATURE -> {
                            hasAnimal--;
                            if (hasAnimal == 0) tableCheckbox.checked = false;
                        }
//                        case WATER_AMBIENT, WATER_CREATURE, UNDERGROUND_WATER_CREATURE, AXOLOTLS -> {
//                            hasWaterAnimal--;
//                            if (hasWaterAnimal == 0) tableCheckbox.checked = false;
//                        }
//                        case MONSTER -> {
//                            hasMonster--;
//                            if (hasMonster == 0) tableCheckbox.checked = false;
//                        }
//                        case AMBIENT -> {
//                            hasAmbient--;
//                            if (hasAmbient == 0) tableCheckbox.checked = false;
//                        }
//                        case MISC -> {
//                            hasMisc--;
//                            if (hasMisc == 0) tableCheckbox.checked = false;
//                        }
                    }
                }
            }

            setting.onChanged();
        };

        table.row();
    }
}
