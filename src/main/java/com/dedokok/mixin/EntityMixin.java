/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.dedokok.DedTools;
import com.dedokok.events.entity.EntityMoveEvent;

import com.dedokok.mixininterface.ICamera;
import com.dedokok.systems.modules.Modules;


import com.dedokok.utils.Utils;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static com.dedokok.DedTools.mc;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = {"isInWater", "isInLava"}, at = @At("HEAD"), cancellable = true)
    private void onIsInFluid(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this != mc.player) return;

    }

    @Inject(method = {"onAboveBubbleColumn", "onInsideBubbleColumn"}, at = @At("HEAD"))
    private void onBubbleColumn(CallbackInfo ci) {
        if ((Object) this != mc.player) return;

    }

    @ModifyExpressionValue(method = "updateSwimming", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isUnderWater()Z"))
    private boolean isSubmergedInWater(boolean submerged) {
        if ((Object) this != mc.player) return submerged;


        return submerged;
    }

    @ModifyArgs(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;push(DDD)V"))
    private void onPushAwayFrom(Args args, Entity entity) {
        // Velocity

    }



    @Inject(method = "move", at = @At("HEAD"))
    private void onMove(MoverType moverType, Vec3 delta, CallbackInfo ci) {

    }







    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    private void onIsInvisibleTo(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player == null) cir.setReturnValue(false);
    }



    @ModifyReturnValue(method = "getPose", at = @At("RETURN"))
    private Pose modifyGetPose(Pose original) {
        if ((Object) this != mc.player) return original;

        if (original == Pose.CROUCHING && !mc.player.isShiftKeyDown() && ((PlayerAccessor) mc.player).meteor$canChangeIntoPose(Pose.STANDING))
            return Pose.STANDING;
        return original;
    }




}
