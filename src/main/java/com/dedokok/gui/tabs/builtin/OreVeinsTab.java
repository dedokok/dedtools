package com.dedokok.gui.tabs.builtin;

import com.dedokok.gui.GuiTheme;
import com.dedokok.gui.GuiThemes;
import com.dedokok.gui.tabs.Tab;
import com.dedokok.gui.tabs.TabScreen;
import net.minecraft.client.gui.screens.Screen;

public class OreVeinsTab extends Tab {
    public OreVeinsTab() {
        super("Veins");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return theme.oreVeinsScreen();
    }

    @Override
    public boolean isScreen(Screen screen) {
        return GuiThemes.get().isOreVeinsScreen(screen);
    }
}
