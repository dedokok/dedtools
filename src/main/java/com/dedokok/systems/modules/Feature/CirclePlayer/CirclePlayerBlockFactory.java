package com.dedokok.systems.modules.Feature.CirclePlayer;


import com.dedokok.gui.WidgetScreen;

import java.util.HashMap;
import java.util.Map;

public class CirclePlayerBlockFactory {
    private final Map<String, Factory> factories;
    public WidgetScreen screen = null;

    public CirclePlayerBlockFactory() {
        factories = new HashMap<>();
        factories.put(CirclePlayerBlock.type, CirclePlayerBlock::new);
    }

    public CirclePlayerBlock createInstruction(String name) {
        if (factories.containsKey(name)) return factories.get(name).create();
        return null;
    }

    private interface Factory {
        CirclePlayerBlock create();
    }
}
