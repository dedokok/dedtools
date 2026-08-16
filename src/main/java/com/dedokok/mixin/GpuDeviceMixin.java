/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.mixin;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.GpuDeviceBackend;
import com.mojang.blaze3d.systems.RenderPassBackend;
import com.dedokok.mixininterface.IGpuDevice;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GpuDevice.class)
public abstract class GpuDeviceMixin implements IGpuDevice {
    @Shadow
    @Final
    private GpuDeviceBackend backend;

    @Override
    public void meteor$pushScissor(int x, int y, int width, int height) {
        ((IGpuDevice) backend).meteor$pushScissor(x, y, width, height);
    }

    @Override
    public void meteor$popScissor() {
        ((IGpuDevice) backend).meteor$popScissor();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void meteor$onCreateRenderPass(RenderPassBackend backend) {
        ((IGpuDevice) this.backend).meteor$onCreateRenderPass(backend);
    }
}
