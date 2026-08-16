package com.dedokok.mixininterface;

import com.mojang.blaze3d.pipeline.RenderPipeline;

public interface IRenderPipelineBuilder {
    void meteor$setLineSmooth(boolean lineSmooth);

    default RenderPipeline.Builder withLineSmooth() {
        meteor$setLineSmooth(true);
        return (RenderPipeline.Builder) this;
    }
}