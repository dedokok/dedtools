package com.dedokok.systems.modules.Feature.AutoReveiver;


import com.dedokok.gui.WidgetScreen;

import java.util.HashMap;
import java.util.Map;

public class InstructionFactory {
    private final Map<String, Factory> factories;
    public WidgetScreen screen = null;

    public InstructionFactory() {
        factories = new HashMap<>();
        factories.put(AutoReceiveBlock.type, AutoReceiveBlock::new);
    }

    public AutoReceiveBlock createInstruction(String name) {
        if (factories.containsKey(name)) return factories.get(name).create();
        return null;
    }

    private interface Factory {
        AutoReceiveBlock create();
    }
}
