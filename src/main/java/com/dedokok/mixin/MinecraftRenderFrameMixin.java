/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.mixin;

import com.dedokok.renderer.MeshUniforms;
import com.dedokok.systems.modules.Modules;
//import com.dedokok.utils.render.postprocess.ChamsShader;
import com.dedokok.utils.render.postprocess.OutlineUniforms;
import com.dedokok.utils.render.postprocess.PostProcessShader;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.dedokok.DedTools.mc;

@Mixin(Minecraft.class)
public abstract class MinecraftRenderFrameMixin {
//    @Inject(method = "renderFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;endFrame()V", shift = At.Shift.AFTER))
//    private void meteor$afterRenderFrame(boolean advanceGameTime, CallbackInfo ci) {
//        MeshUniforms.flipFrame();
//        PostProcessShader.flipFrame();
//        ChamsShader.flipFrame();
//        OutlineUniforms.flipFrame();
//
//        Modules modules = Modules.get();
//        if (modules == null || mc.player == null) return;
//
//        InventoryTweaks inventoryTweaks = modules.get(InventoryTweaks.class);
//        if (inventoryTweaks != null && inventoryTweaks.frameInput()) {
//            ((MinecraftAccessor) mc).meteor$handleInputEvents();
//        }
//    }
}
