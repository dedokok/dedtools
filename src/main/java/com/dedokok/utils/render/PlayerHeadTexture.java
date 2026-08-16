package com.dedokok.utils.render;

import com.dedokok.DedTools;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.textures.FilterMode;
import com.dedokok.renderer.Texture;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static com.dedokok.DedTools.mc;

public class PlayerHeadTexture extends Texture {
    private boolean needsRotate;

    public PlayerHeadTexture(byte[] head, boolean needsRotate) {
        super(8, 8, GpuFormat.RGBA8_UNORM, FilterMode.NEAREST, FilterMode.NEAREST);

        upload(BufferUtils.createByteBuffer(head.length).put(head));
        this.needsRotate = needsRotate;
    }

    public PlayerHeadTexture() {
        super(8, 8, GpuFormat.RGBA8_UNORM, FilterMode.NEAREST, FilterMode.NEAREST);

        try (InputStream inputStream = mc.getResourceManager().getResource(DedTools.identifier("textures/steve.png")).get().open()) {
            ByteBuffer data = TextureUtil.readResource(inputStream);
            data.rewind();

            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);
                IntBuffer comp = stack.mallocInt(1);

                ByteBuffer image = STBImage.stbi_load_from_memory(data, width, height, comp, 4);
                upload(image);
                STBImage.stbi_image_free(image);
            }
            MemoryUtil.memFree(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean needsRotate() {
        return needsRotate;
    }


}
