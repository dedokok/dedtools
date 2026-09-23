/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.settings;

import com.dedokok.gui.GuiTheme;
import com.dedokok.gui.WidgetScreen;
import com.dedokok.utils.misc.ICopyable;
import com.dedokok.utils.misc.ISerializable;

public interface IGeneric<T extends IGeneric<T>> extends ICopyable<T>, ISerializable<T> {
    WidgetScreen createScreen(GuiTheme theme, Setting<T> setting);
}
