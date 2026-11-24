package com.dedokok.systems.modules.Feature.MessageAnnounce;


import com.dedokok.gui.WidgetScreen;
import com.dedokok.systems.modules.Feature.AutoReveiver.AutoReceiveBlock;

import java.util.HashMap;
import java.util.Map;

public class MessageAnnouncerFactory {
    private final Map<String, Factory> factories;
    public WidgetScreen screen = null;

    public MessageAnnouncerFactory() {
        factories = new HashMap<>();
        factories.put(MessageAnnouncerBlock.type, MessageAnnouncerBlock::new);
    }

    public MessageAnnouncerBlock createInstruction(String name) {
        if (factories.containsKey(name)) return factories.get(name).create();
        return null;
    }

    private interface Factory {
        MessageAnnouncerBlock create();
    }
}
