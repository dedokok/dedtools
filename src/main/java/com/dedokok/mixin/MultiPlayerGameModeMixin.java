/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.dedokok.DedTools;
import com.dedokok.events.entity.DropItemsEvent;
import com.dedokok.events.entity.player.*;
import com.dedokok.mixininterface.IMultiPlayerGameMode;
import com.dedokok.systems.modules.Modules;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.dedokok.DedTools.mc;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin implements IMultiPlayerGameMode {
    @Shadow
    private int destroyDelay;

    @Shadow
    protected abstract void ensureHasSentCarriedItem();

    @Shadow
    public abstract boolean destroyBlock(BlockPos pos);

    @Shadow
    public abstract void startPrediction(ClientLevel level, PredictiveAction predictiveAction);

    @Inject(method = "handleContainerInput", at = @At("HEAD"), cancellable = true)
    private void onHandleInventoryMouseClick(int containerId, int slotNum, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci) {
        if (containerInput == ContainerInput.THROW && slotNum >= 0 && slotNum < player.containerMenu.slots.size()) {
            if (DedTools.EVENT_BUS.post(DropItemsEvent.get(player.containerMenu.slots.get(slotNum).getItem())).isCancelled())
                ci.cancel();
        } else if (slotNum == -999) {
            // Clicking outside of inventory
            if (DedTools.EVENT_BUS.post(DropItemsEvent.get(player.containerMenu.getCarried())).isCancelled())
                ci.cancel();
        }
    }



    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    public void useItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult blockHit, CallbackInfoReturnable<InteractionResult> cir) {
        if (DedTools.EVENT_BUS.post(InteractBlockEvent.get(player.getMainHandItem().isEmpty() ? InteractionHand.OFF_HAND : hand, blockHit)).isCancelled())
            cir.setReturnValue(InteractionResult.FAIL);
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttack(Player player, Entity entity, CallbackInfo ci) {
        if (DedTools.EVENT_BUS.post(AttackEntityEvent.get(entity)).isCancelled()) ci.cancel();
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onInteract(Player player, Entity entity, EntityHitResult hitResult, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (DedTools.EVENT_BUS.post(InteractEntityEvent.get(entity, hand)).isCancelled())
            cir.setReturnValue(InteractionResult.FAIL);
    }

    @Inject(method = "handleCreativeModeItemDrop", at = @At("HEAD"), cancellable = true)
    private void onHandleCreativeModeItemDrop(ItemStack clicked, CallbackInfo ci) {
        if (DedTools.EVENT_BUS.post(DropItemsEvent.get(clicked)).isCancelled()) ci.cancel();
    }





    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void onUseItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        InteractItemEvent event = DedTools.EVENT_BUS.post(InteractItemEvent.get(hand));
        if (event.toReturn != null) cir.setReturnValue(event.toReturn);
    }


    @Override
    public void meteor$syncSelected() {
        ensureHasSentCarriedItem();
    }
}
