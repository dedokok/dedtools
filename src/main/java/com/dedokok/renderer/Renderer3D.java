/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.dedokok.utils.render.color.Color;
import com.dedokok.utils.world.Dir;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Renderer3D {
    public final MeshBuilder lines;
    public final MeshBuilder triangles;
    private final RenderPipeline linesPipeline;
    private final RenderPipeline trianglesPipeline;

    public Renderer3D(RenderPipeline lines, RenderPipeline triangles) {
        this.lines = new MeshBuilder(lines);
        this.triangles = new MeshBuilder(triangles);
        this.linesPipeline = lines;
        this.trianglesPipeline = triangles;
    }

    public void begin() {
        lines.begin();
        triangles.begin();
    }

    public void render(PoseStack matrices) {
        MeshRenderer.begin()
            .attachments(Minecraft.getInstance().gameRenderer.mainRenderTarget())
            .pipeline(linesPipeline)
            .mesh(lines, matrices)
            .end();

        MeshRenderer.begin()
            .attachments(Minecraft.getInstance().gameRenderer.mainRenderTarget())
            .pipeline(trianglesPipeline)
            .mesh(triangles, matrices)
            .end();
    }

    // Lines

    public void line(double x1, double y1, double z1, double x2, double y2, double z2, Color color1, Color color2) {
        lines.ensureLineCapacity();

        lines.line(
            lines.vec3(x1, y1, z1).color(color1).next(),
            lines.vec3(x2, y2, z2).color(color2).next()
        );
    }

    public void line(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
        line(x1, y1, z1, x2, y2, z2, color, color);
    }

    @SuppressWarnings("Duplicates")
    public void boxLines(double x1, double y1, double z1, double x2, double y2, double z2, Color color, int excludeDir) {
        lines.ensureCapacity(8, 24);

        int blb = lines.vec3(x1, y1, z1).color(color).next();
        int blf = lines.vec3(x1, y1, z2).color(color).next();
        int brb = lines.vec3(x2, y1, z1).color(color).next();
        int brf = lines.vec3(x2, y1, z2).color(color).next();
        int tlb = lines.vec3(x1, y2, z1).color(color).next();
        int tlf = lines.vec3(x1, y2, z2).color(color).next();
        int trb = lines.vec3(x2, y2, z1).color(color).next();
        int trf = lines.vec3(x2, y2, z2).color(color).next();

        if (excludeDir == 0) {
            // Bottom to top
            lines.line(blb, tlb);
            lines.line(blf, tlf);
            lines.line(brb, trb);
            lines.line(brf, trf);

            // Bottom loop
            lines.line(blb, blf);
            lines.line(brb, brf);
            lines.line(blb, brb);
            lines.line(blf, brf);

            // Top loop
            lines.line(tlb, tlf);
            lines.line(trb, trf);
            lines.line(tlb, trb);
            lines.line(tlf, trf);
        } else {
            // Bottom to top
            if (Dir.isNot(excludeDir, Dir.WEST) && Dir.isNot(excludeDir, Dir.NORTH)) lines.line(blb, tlb);
            if (Dir.isNot(excludeDir, Dir.WEST) && Dir.isNot(excludeDir, Dir.SOUTH)) lines.line(blf, tlf);
            if (Dir.isNot(excludeDir, Dir.EAST) && Dir.isNot(excludeDir, Dir.NORTH)) lines.line(brb, trb);
            if (Dir.isNot(excludeDir, Dir.EAST) && Dir.isNot(excludeDir, Dir.SOUTH)) lines.line(brf, trf);

            // Bottom loop
            if (Dir.isNot(excludeDir, Dir.WEST) && Dir.isNot(excludeDir, Dir.DOWN)) lines.line(blb, blf);
            if (Dir.isNot(excludeDir, Dir.EAST) && Dir.isNot(excludeDir, Dir.DOWN)) lines.line(brb, brf);
            if (Dir.isNot(excludeDir, Dir.NORTH) && Dir.isNot(excludeDir, Dir.DOWN)) lines.line(blb, brb);
            if (Dir.isNot(excludeDir, Dir.SOUTH) && Dir.isNot(excludeDir, Dir.DOWN)) lines.line(blf, brf);

            // Top loop
            if (Dir.isNot(excludeDir, Dir.WEST) && Dir.isNot(excludeDir, Dir.UP)) lines.line(tlb, tlf);
            if (Dir.isNot(excludeDir, Dir.EAST) && Dir.isNot(excludeDir, Dir.UP)) lines.line(trb, trf);
            if (Dir.isNot(excludeDir, Dir.NORTH) && Dir.isNot(excludeDir, Dir.UP)) lines.line(tlb, trb);
            if (Dir.isNot(excludeDir, Dir.SOUTH) && Dir.isNot(excludeDir, Dir.UP)) lines.line(tlf, trf);
        }
    }

    public void blockLines(int x, int y, int z, Color color, int excludeDir) {
        boxLines(x, y, z, x + 1, y + 1, z + 1, color, excludeDir);
    }

    // Quads

    public void quad(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, Color topLeft, Color topRight, Color bottomRight, Color bottomLeft) {
        triangles.ensureQuadCapacity();

        triangles.quad(
            triangles.vec3(x1, y1, z1).color(bottomLeft).next(),
            triangles.vec3(x2, y2, z2).color(topLeft).next(),
            triangles.vec3(x3, y3, z3).color(topRight).next(),
            triangles.vec3(x4, y4, z4).color(bottomRight).next()
        );
    }

    public void quad(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, Color color) {
        quad(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, color, color, color, color);
    }

    public void quadVertical(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
        quad(x1, y1, z1, x1, y2, z1, x2, y2, z2, x2, y1, z2, color);
    }

    public void quadHorizontal(double x1, double y, double z1, double x2, double z2, Color color) {
        quad(x1, y, z1, x1, y, z2, x2, y, z2, x2, y, z1, color);
    }

    public void gradientQuadVertical(double x1, double y1, double z1, double x2, double y2, double z2, Color topColor, Color bottomColor) {
        quad(x1, y1, z1, x1, y2, z1, x2, y2, z2, x2, y1, z2, topColor, topColor, bottomColor, bottomColor);
    }

    // Sides

    @SuppressWarnings("Duplicates")
    public void side(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, Color sideColor, Color lineColor, ShapeMode mode) {
        if (mode.lines()) {
            lines.ensureCapacity(4, 8);

            int i1 = lines.vec3(x1, y1, z1).color(lineColor).next();
            int i2 = lines.vec3(x2, y2, z2).color(lineColor).next();
            int i3 = lines.vec3(x3, y3, z3).color(lineColor).next();
            int i4 = lines.vec3(x4, y4, z4).color(lineColor).next();

            lines.line(i1, i2);
            lines.line(i2, i3);
            lines.line(i3, i4);
            lines.line(i4, i1);
        }

        if (mode.sides()) {
            quad(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, sideColor);
        }
    }

    public void sideVertical(double x1, double y1, double z1, double x2, double y2, double z2, Color sideColor, Color lineColor, ShapeMode mode) {
        side(x1, y1, z1, x1, y2, z1, x2, y2, z2, x2, y1, z2, sideColor, lineColor, mode);
    }

    public void sideHorizontal(double x1, double y, double z1, double x2, double z2, Color sideColor, Color lineColor, ShapeMode mode) {
        side(x1, y, z1, x1, y, z2, x2, y, z2, x2, y, z1, sideColor, lineColor, mode);
    }

    // Boxes

    @SuppressWarnings("Duplicates")
    public void boxSides(double x1, double y1, double z1, double x2, double y2, double z2, Color color, int excludeDir) {
        triangles.ensureCapacity(8, 36);

        int blb = triangles.vec3(x1, y1, z1).color(color).next();
        int blf = triangles.vec3(x1, y1, z2).color(color).next();
        int brb = triangles.vec3(x2, y1, z1).color(color).next();
        int brf = triangles.vec3(x2, y1, z2).color(color).next();
        int tlb = triangles.vec3(x1, y2, z1).color(color).next();
        int tlf = triangles.vec3(x1, y2, z2).color(color).next();
        int trb = triangles.vec3(x2, y2, z1).color(color).next();
        int trf = triangles.vec3(x2, y2, z2).color(color).next();

        if (excludeDir == 0) {
            // Bottom to top
            triangles.quad(blb, blf, tlf, tlb);
            triangles.quad(brb, trb, trf, brf);
            triangles.quad(blb, tlb, trb, brb);
            triangles.quad(blf, brf, trf, tlf);

            // Bottom
            triangles.quad(blb, brb, brf, blf);

            // Top
            triangles.quad(tlb, tlf, trf, trb);
        } else {
            // Bottom to top
            if (Dir.isNot(excludeDir, Dir.WEST)) triangles.quad(blb, blf, tlf, tlb);
            if (Dir.isNot(excludeDir, Dir.EAST)) triangles.quad(brb, trb, trf, brf);
            if (Dir.isNot(excludeDir, Dir.NORTH)) triangles.quad(blb, tlb, trb, brb);
            if (Dir.isNot(excludeDir, Dir.SOUTH)) triangles.quad(blf, brf, trf, tlf);

            // Bottom
            if (Dir.isNot(excludeDir, Dir.DOWN)) triangles.quad(blb, brb, brf, blf);

            // Top
            if (Dir.isNot(excludeDir, Dir.UP)) triangles.quad(tlb, tlf, trf, trb);
        }
    }

    public void blockSides(int x, int y, int z, Color color, int excludeDir) {
        boxSides(x, y, z, x + 1, y + 1, z + 1, color, excludeDir);
    }

    public void box(double x1, double y1, double z1, double x2, double y2, double z2, Color sideColor, Color lineColor, ShapeMode mode, int excludeDir) {
        if (mode.lines()) boxLines(x1, y1, z1, x2, y2, z2, lineColor, excludeDir);
        if (mode.sides()) boxSides(x1, y1, z1, x2, y2, z2, sideColor, excludeDir);
    }

    public void box(BlockPos pos, Color sideColor, Color lineColor, ShapeMode mode, int excludeDir) {
        if (mode.lines())
            boxLines(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, lineColor, excludeDir);
        if (mode.sides())
            boxSides(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, sideColor, excludeDir);
    }

    public void box(AABB box, Color sideColor, Color lineColor, ShapeMode mode, int excludeDir) {
        if (mode.lines()) boxLines(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, lineColor, excludeDir);
        if (mode.sides()) boxSides(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, sideColor, excludeDir);
    }


//    //circles
//    public void circleLines(double cx, double cy, double cz, double radius, int segments, Color color) {
//        lines.ensureCapacity(segments, segments * 2);
//
//        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
//        Vec3 right = camera.getLeftVector().mul(-1, -1, -1); // right = -left
//        Vec3 up = camera.getUpVector();
//
//        int firstIndex = -1;
//        int prevIndex = -1;
//
//        for (int i = 0; i < segments; i++) {
//            double angle = 2 * Math.PI * i / segments;
//            double cos = Math.cos(angle) * radius;
//            double sin = Math.sin(angle) * radius;
//
//            double x = cx + right.x * cos + up.x * sin;
//            double y = cy + right.y * cos + up.y * sin;
//            double z = cz + right.z * cos + up.z * sin;
//
//            int index = lines.vec3(x, y, z).color(color).next();
//
//            if (i == 0) firstIndex = index;
//            else lines.line(prevIndex, index);
//
//            prevIndex = index;
//        }
//
//        lines.line(prevIndex, firstIndex);
//    }
//
//    public void ring(double cx, double cy, double cz, double innerRadius, double outerRadius, int segments, Color color) {
//        triangles.ensureCapacity(segments * 2, segments * 6);
//
//        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
//        Vec3 right = camera.getLeftVector().mul(-1, -1, -1);
//        Vec3 up = camera.getUpVector();
//
//        for (int i = 0; i < segments; i++) {
//            double a1 = 2 * Math.PI * i / segments;
//            double a2 = 2 * Math.PI * (i + 1) / segments;
//
//            double cos1 = Math.cos(a1), sin1 = Math.sin(a1);
//            double cos2 = Math.cos(a2), sin2 = Math.sin(a2);
//
//            int i1 = triangles.vec3(
//                    cx + right.x * cos1 * outerRadius + up.x * sin1 * outerRadius,
//                    cy + right.y * cos1 * outerRadius + up.y * sin1 * outerRadius,
//                    cz + right.z * cos1 * outerRadius + up.z * sin1 * outerRadius
//            ).color(color).next();
//
//            int i2 = triangles.vec3(
//                    cx + right.x * cos1 * innerRadius + up.x * sin1 * innerRadius,
//                    cy + right.y * cos1 * innerRadius + up.y * sin1 * innerRadius,
//                    cz + right.z * cos1 * innerRadius + up.z * sin1 * innerRadius
//            ).color(color).next();
//
//            int i3 = triangles.vec3(
//                    cx + right.x * cos2 * innerRadius + up.x * sin2 * innerRadius,
//                    cy + right.y * cos2 * innerRadius + up.y * sin2 * innerRadius,
//                    cz + right.z * cos2 * innerRadius + up.z * sin2 * innerRadius
//            ).color(color).next();
//
//            int i4 = triangles.vec3(
//                    cx + right.x * cos2 * outerRadius + up.x * sin2 * outerRadius,
//                    cy + right.y * cos2 * outerRadius + up.y * sin2 * outerRadius,
//                    cz + right.z * cos2 * outerRadius + up.z * sin2 * outerRadius
//            ).color(color).next();
//
//            triangles.quad(i1, i2, i3, i4);
//        }
//    }
}
