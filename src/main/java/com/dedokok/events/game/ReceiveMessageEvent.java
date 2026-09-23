/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.events.game;

import com.dedokok.events.Cancellable;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.network.chat.Component;

public class ReceiveMessageEvent extends Cancellable {
    private static final ReceiveMessageEvent INSTANCE = new ReceiveMessageEvent();

    private Component message;
    private GuiMessageSource sender;
    private int id;

    public static ReceiveMessageEvent get(GuiMessageSource sender, Component message, int id) {
        INSTANCE.setCancelled(false);
        INSTANCE.message = message;
        INSTANCE.sender = sender;
        INSTANCE.id = id;
        return INSTANCE;
    }

    public Component getMessage() {
        return message;
    }


    public void setMessage(Component message) {
        this.message = message;
    }

    public  GuiMessageSource getSender() {
        return sender;
    }

}
