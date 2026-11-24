package com.dedokok.utils.classes;

import net.minecraft.world.entity.EntityType;

public class EntitiesInChunkClass {
    public EntityType<?> entity_type;
    public int count;
    public EntitiesInChunkClass(EntityType<?> entity_type, int count){
        this.entity_type = entity_type;
        this.count = count;
    }
    public EntitiesInChunkClass(){}
}
