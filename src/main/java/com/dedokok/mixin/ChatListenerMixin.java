/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.mixin;

import com.dedokok.DedTools;
import com.dedokok.events.game.ReceiveMessageEvent;
import com.mojang.authlib.GameProfile;
import com.dedokok.mixininterface.IChatListener;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;

@Mixin(ChatListener.class)
public abstract class ChatListenerMixin implements IChatListener {
    @Unique
    private GameProfile sender;
    @Inject(method = "showMessageToPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addPlayerMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V", shift = At.Shift.BEFORE))
    private void onShowMessageToPlayer_beforeAddMessage(ChatType.Bound boundChatType, PlayerChatMessage message, Component decoratedMessage, GameProfile sender, boolean onlyShowSecure, Instant received, CallbackInfoReturnable<Boolean> cir) {
        this.sender = sender;
    }

//    @Inject(method = "handleSystemMessage", at = @At("HEAD"))
//    private void onHandleSystemMessage(Component message, boolean remote, CallbackInfo ci) {
//        //DedTools.EVENT_BUS.post(ReceiveMessageEvent.get(null, message));
//        System.out.println("получил сообщение: "+message.getString());
//    }

    @Inject(method = "handlePlayerChatMessage", at = @At("HEAD"))
    private void onHandleSystemMessage(PlayerChatMessage message, GameProfile sender, ChatType.Bound boundChatType, CallbackInfo ci) {
        System.out.println("!!! HANDLE SYSTEM MESSAGE !!!");
        System.out.println("message = " + message.decoratedContent().getString());
    }

    @Override
    public GameProfile meteor$getSender() {
        return sender;
    }
}
