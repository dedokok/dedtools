/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.gui.tabs.builtin;

import com.dedokok.gui.GuiTheme;
import com.dedokok.gui.GuiThemes;
import com.dedokok.gui.tabs.Tab;
import com.dedokok.gui.tabs.TabScreen;
import net.minecraft.client.gui.screens.Screen;
public class ModulesTab extends Tab {
    public ModulesTab() {
        super("Modules");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return theme.modulesScreen();
    }

    @Override
    public boolean isScreen(Screen screen) {
        return GuiThemes.get().isModulesScreen(screen);
    }
}
