/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.systems.modules;

import com.dedokok.utils.render.DisplayItemUtils;
import net.minecraft.world.item.Items;

public class Categories {
    public static final Category Feature = new Category("Feature", () -> DisplayItemUtils.toStack(Items.GOLDEN_SWORD));

    public static boolean REGISTERING;

    public static void init() {
        REGISTERING = true;
        Modules.registerCategory(Feature);
        REGISTERING = false;
    }
}
