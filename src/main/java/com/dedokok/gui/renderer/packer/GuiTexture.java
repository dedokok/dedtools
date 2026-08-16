package com.dedokok.gui.renderer.packer;/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */


import java.util.ArrayList;
import java.util.List;

public class GuiTexture {
    private final List<com.dedokok.gui.renderer.packer.TextureRegion> regions = new ArrayList<>(2);

    void add(com.dedokok.gui.renderer.packer.TextureRegion region) {
        regions.add(region);
    }

    public com.dedokok.gui.renderer.packer.TextureRegion get(double width, double height) {
        double targetDiagonal = Math.sqrt(width * width + height * height);

        double closestDifference = Double.MAX_VALUE;
        com.dedokok.gui.renderer.packer.TextureRegion closestRegion = null;

        for (com.dedokok.gui.renderer.packer.TextureRegion region : regions) {
            double difference = Math.abs(targetDiagonal - region.diagonal);

            if (difference < closestDifference) {
                closestDifference = difference;
                closestRegion = region;
            }
        }

        return closestRegion;
    }
}
