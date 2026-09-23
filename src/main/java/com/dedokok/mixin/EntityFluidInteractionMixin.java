/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.dedokok.systems.modules.Modules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static com.dedokok.DedTools.mc;

@Mixin(EntityFluidInteraction.class)
public abstract class EntityFluidInteractionMixin {


}
