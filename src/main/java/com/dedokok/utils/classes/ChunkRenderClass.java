package com.dedokok.utils.classes;

import com.dedokok.utils.render.color.Color;

public class ChunkRenderClass {
    public Color color;
    public EntityChunk chunk;
    public ChunkRenderClass(Color color, EntityChunk chunk){
        this.color = color;
        this.chunk = chunk;
    }
    public ChunkRenderClass(){}
}
