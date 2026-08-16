/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.gui;

import com.dedokok.gui.utils.Cell;
import com.dedokok.gui.widgets.WWidget;
import com.dedokok.gui.widgets.containers.WWindow;

public abstract class WindowScreen extends com.dedokok.gui.WidgetScreen {
    protected final WWindow window;

    public WindowScreen(com.dedokok.gui.GuiTheme theme, WWidget icon, String title) {
        super(theme, title);

        window = super.add(theme.window(icon, title)).center().widget();
        window.view.scrollOnlyWhenMouseOver = false;
    }

    public WindowScreen(com.dedokok.gui.GuiTheme theme, String title) {
        this(theme, null, title);
    }

    @Override
    public <W extends WWidget> Cell<W> add(W widget) {
        return window.add(widget);
    }

    @Override
    public void clear() {
        window.clear();
    }
}
