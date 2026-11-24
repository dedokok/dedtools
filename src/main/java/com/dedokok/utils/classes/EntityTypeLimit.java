package com.dedokok.utils.classes;

import net.minecraft.world.entity.EntityType;

public class EntityTypeLimit {
    public EntityType<?> type;
    public int limit;
    public boolean isEnabled = false;
    public EntityTypeLimit(EntityType<?> type, int limit) {
        this.type = type;
        this.limit = limit;
    }



}
