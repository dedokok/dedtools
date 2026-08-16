package com.dedokok.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class DebugStopUsingItemMixin {
    @Inject(method = "stopUsingItem", at = @At("HEAD"))
    private void debugStopUsingItem(CallbackInfo ci) {
        System.out.println("[DEBUG] stopUsingItem() called!");
        Thread.dumpStack(); // покажет, откуда именно вызван метод
    }
}
