package com.dedokok.utils.classes;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;

public class EntityChunk {
    public int x;
    public int z;
    public int id;
    public ArrayList<EntitiesInChunkClass> entities = new ArrayList<>();
    public EntityChunk(int x, int z, int id,ArrayList<EntitiesInChunkClass> entities) {
        this.x = x;
        this.z = z;
        this.id = id;
        this.entities = entities;
    }
    public EntityChunk(ChunkPos chunkPos, int id) {
        this.x= chunkPos.x();
        this.z= chunkPos.z();
        this.id = id;
    }
    public EntityChunk(){}

    public void increaseEntity(EntityType<?> entity_type){
        boolean isFind = false;
        for(EntitiesInChunkClass entity : entities){
            if(entity.entity_type.equals(entity_type)){
                entity.count++;
                isFind=true;
                break;
            }
        }
        if(!isFind){
            entities.add(new EntitiesInChunkClass(entity_type,1));
        }
    }

    public int getCount(){
        int count = 0;
        for(EntitiesInChunkClass entity : entities){
            count += entity.count;
        }
        return count;
    }
}
