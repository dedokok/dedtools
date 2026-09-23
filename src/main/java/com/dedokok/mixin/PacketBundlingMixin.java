/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.dedokok.systems.modules.Modules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 */
@Mixin(targets = "net/minecraft/network/protocol/BundlerInfo$1$1")
public abstract class PacketBundlingMixin {
    @ModifyExpressionValue(method = "addPacket", at = @At(value = "CONSTANT", args = "intValue=4096"))
    private int add(int value) {
        return value;
    }
}
