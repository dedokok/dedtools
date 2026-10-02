/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.systems.modules.Feature.blockesp;

import com.dedokok.events.render.Render3DEvent;
import com.dedokok.renderer.ShapeMode;
import com.dedokok.systems.modules.Modules;
import com.dedokok.utils.render.color.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static com.dedokok.DedTools.mc;

public class ESPBlock {
    private static final BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();



    public final int x, y, z;
    private BlockState state;
    public int neighbours;


    public ESPBlock(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }


    public void update() {
        if(mc==null || mc.level==null)return;
        state = mc.level.getBlockState(blockPos.set(x, y, z));
        neighbours = 0;
    }



    public void render(Render3DEvent event, Color color) {
        if(mc==null || mc.level==null || state==null)return;
        double x1 = x;
        double y1 = y;
        double z1 = z;
        double x2 = x + 1;
        double y2 = y + 1;
        double z2 = z + 1;

        VoxelShape shape = state.getShape(mc.level, blockPos);

        if (!shape.isEmpty()) {
            x1 = x + shape.min(Direction.Axis.X);
            y1 = y + shape.min(Direction.Axis.Y);
            z1 = z + shape.min(Direction.Axis.Z);
            x2 = x + shape.max(Direction.Axis.X);
            y2 = y + shape.max(Direction.Axis.Y);
            z2 = z + shape.max(Direction.Axis.Z);
        }


        ShapeMode shapeMode = ShapeMode.Lines;
        neighbours=0;
        event.renderer.box(x1, y1, z1, x2, y2, z2, color, color, shapeMode, 0);

    }


    public static long getKey(int x, int y, int z) {
        return ((long) y << 16) | ((long) (z & 15) << 8) | ((long) (x & 15));
    }

    public static long getKey(BlockPos blockPos) {
        return getKey(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }
}
