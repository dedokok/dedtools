/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.dedokok.DedTools;
import com.dedokok.events.game.ReceiveMessageEvent;
import com.dedokok.mixininterface.IChatHud;
import com.dedokok.mixininterface.IChatListener;
import com.dedokok.mixininterface.IGuiMessage;
import com.dedokok.mixininterface.IGuiMessageVisible;
import com.dedokok.systems.modules.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin implements IChatHud {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private List<GuiMessage.Line> trimmedMessages;
    @Shadow
    @Final
    private List<GuiMessage> allMessages;


    @Unique
    private int nextId;

    @Shadow
    public abstract void addClientSystemMessage(Component message);

    @Override
    public void meteor$add(Component message, int id) {
        nextId = id;
        addClientSystemMessage(message);
        nextId = 0;
    }

    @Inject(method = "addMessageToDisplayQueue", at = @At(value = "INVOKE", target = "Ljava/util/List;addFirst(Ljava/lang/Object;)V", shift = At.Shift.AFTER))
    private void onAddMessageAfterNewGuiMessageVisible(GuiMessage message, CallbackInfo ci) {
        ((IGuiMessage) (Object) trimmedMessages.getFirst()).meteor$setId(nextId);
    }

    @Inject(method = "addMessageToQueue", at = @At(value = "INVOKE", target = "Ljava/util/List;addFirst(Ljava/lang/Object;)V", shift = At.Shift.AFTER))
    private void onAddMessageAfterNewGuiMessage(GuiMessage message, CallbackInfo ci) {
        ((IGuiMessage) (Object) allMessages.getFirst()).meteor$setId(nextId);
    }

    @SuppressWarnings("DataFlowIssue")
    @ModifyExpressionValue(method = "addMessageToDisplayQueue", at = @At(value = "NEW", target = "(Lnet/minecraft/client/multiplayer/chat/GuiMessage;Lnet/minecraft/util/FormattedCharSequence;Z)Lnet/minecraft/client/multiplayer/chat/GuiMessage$Line;"))
    private GuiMessage.Line onAddMessage_modifyGuiMessageLine(GuiMessage.Line line, @Local(name = "i") int i) {
        IChatListener handler = (IChatListener) minecraft.gui.chatListener();

        IGuiMessageVisible meteorLine = (IGuiMessageVisible) (Object) line;

        meteorLine.meteor$setSender(handler.meteor$getSender());
        meteorLine.meteor$setStartOfEntry(i == 0);

        return line;
    }

    @ModifyExpressionValue(method = "addMessage", at = @At(value = "NEW", target = "(ILnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)Lnet/minecraft/client/multiplayer/chat/GuiMessage;"))
    private GuiMessage onAddMessage_modifyGuiMessage(GuiMessage line) {
        IChatListener handler = (IChatListener) minecraft.gui.chatListener();

        ((IGuiMessage) (Object) line).meteor$setSender(handler.meteor$getSender());
        return line;
    }

    @Inject(at = @At("HEAD"), method = "addMessage", cancellable = true)
        private void onAddMessage(Component message, MessageSignature signature, GuiMessageSource source, GuiMessageTag indicator, CallbackInfo ci, @Local(argsOnly = true, name = "contents") LocalRef<Component> contents, @Local(argsOnly = true, name = "tag") LocalRef<GuiMessageTag> tag) {
        ReceiveMessageEvent event = DedTools.EVENT_BUS.post(ReceiveMessageEvent.get(source,message, nextId));

        if (event.isCancelled()) ci.cancel();
        else {
            trimmedMessages.removeIf(msg -> ((IGuiMessage) (Object) msg).meteor$getId() == nextId && nextId != 0);

            for (int i = allMessages.size() - 1; i > -1; i--) {
                if (((IGuiMessage) (Object) allMessages.get(i)).meteor$getId() == nextId && nextId != 0) {
                    allMessages.remove(i);
                }
            }

//            if (event.isModified()) {
//                contents.set(event.getMessage());
//                tag.set(event.getIndicator());
//            }
        }
    }

    //modify max lengths for messages and visible messages


    
}
