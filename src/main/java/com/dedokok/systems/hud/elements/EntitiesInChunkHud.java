/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.systems.hud.elements;

import com.dedokok.events.world.TickEvent;
import com.dedokok.settings.*;
import com.dedokok.systems.hud.*;
import com.dedokok.systems.modules.Feature.EntitiesInChunk;
import com.dedokok.systems.modules.Modules;
import com.dedokok.utils.classes.EntitiesInChunkClass;
import com.dedokok.utils.classes.EntityChunk;
import com.dedokok.utils.classes.EntityTypeLimit;
import com.dedokok.utils.classes.RenderStringClass;
import com.dedokok.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import java.awt.*;
import java.util.*;

import static com.dedokok.DedTools.mc;

public class EntitiesInChunkHud extends HudElement {
    public static final HudElementInfo<EntitiesInChunkHud> INFO = new HudElementInfo<>(Hud.GROUP, "entities-in-chunk", "Amount of entities in chunks", EntitiesInChunkHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> textShadow = sgGeneral.add(new BoolSetting.Builder()
        .name("text-shadow")
        .description("Renders shadow behind text.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> defaultTextColor = sgGeneral.add(new ColorSetting.Builder()
        .name("default-text-color")
        .description("Text color.")
        .defaultValue(new SettingColor())
        .build()
    );

    private final Setting<SettingColor> chunkYouInTextColor = sgGeneral.add(new ColorSetting.Builder()
            .name("your-chunk-text-color")
            .description("Color of chunk row you in")
            .defaultValue(new SettingColor(Color.YELLOW))
            .build()
    );

    private final Setting<SettingColor> overThanMaxColor = sgGeneral.add(new ColorSetting.Builder()
            .name("over-limit-color")
            .description("Color of chunk and entities that over than limit")
            .defaultValue(new SettingColor(Color.RED))
            .build()
    );



    private final Setting<Alignment> alignment = sgGeneral.add(new EnumSetting.Builder<Alignment>()
        .name("alignment")
        .description("Horizontal alignment.")
        .defaultValue(Alignment.Auto)
        .build()
    );

    public EntitiesInChunkHud() {
        super(INFO);
    }

    double height = 0;
    double width = 0;
    double y_temp = 0;
    //UUID uuid = null;
    public static UUID targetUUID = null;
    public static int idNow = -10;
    public static int idMax = -10;


    Player player = null;



    ArrayList<RenderStringClass> strings = new ArrayList<>();
    ArrayList<EntityChunk>last_chunks = new ArrayList<>();
    int lastPlayerX = 0;
    int lastPlayerZ = 0;


    @Override
    public void render(HudRenderer renderer) {
        if(Modules.get().get(EntitiesInChunk.class) != null && !Modules.get().get(EntitiesInChunk.class).isActive())return;

        y_temp = this.y;

        width = 0;
        height = 0;

        for (RenderStringClass string : strings) {
            updateSize(renderer, string.string, true, string.color);
        }
        setSize(width, height);
    }

    @Override
    public void tick(HudRenderer renderer){
        if(Modules.get().get(EntitiesInChunk.class) != null && !Modules.get().get(EntitiesInChunk.class).isActive())return;
        boolean shouldSplit = Modules.get().get(EntitiesInChunk.class) != null && Modules.get().get(EntitiesInChunk.class).splitEntitiesTypesSetting.get();
        boolean renderMoreThan = Modules.get().get(EntitiesInChunk.class) != null && Modules.get().get(EntitiesInChunk.class).renderTypeSetting.get() == EntitiesInChunk.RenderType.MoreThan;

        int playerXNow = 0;
        int playerZNow = 0;
        if (mc != null && mc.player != null) {
            playerXNow = mc.player.chunkPosition().x();
            playerZNow = mc.player.chunkPosition().z();
        }

        ArrayList<EntityChunk> chunks = new ArrayList<>(EntitiesInChunk.chunks.values());
        chunks.sort(Comparator.comparing((EntityChunk chunk) -> chunk.id));

        if (chunks.equals(last_chunks) && lastPlayerX == playerXNow && lastPlayerZ == playerZNow) {
            return;
        }

        Set<EntityTypeLimit>typeLimits = Modules.get().get(EntitiesInChunk.class).entitiesTypeLimits.get();


        strings.clear();
        last_chunks = chunks;

        int minEntities = 0;


        if (renderMoreThan && Modules.get().get(EntitiesInChunk.class) != null) {
            minEntities = Modules.get().get(EntitiesInChunk.class).moreThanIntSetting.get();

        }

        for(EntityChunk chunk : chunks) {
            if (renderMoreThan && minEntities > chunk.getCount()) {
                continue;
            }
            SettingColor color = defaultTextColor.get();
            boolean isPlayerChunk = false;


            if (mc != null && mc.player != null) {
                ChunkPos pos_e = new ChunkPos(chunk.x, chunk.z);
                if (pos_e.equals(mc.player.chunkPosition())) {
                    //color = chunkYouInTextColor.get();
                    isPlayerChunk = true;
                }


            }

            if (isPlayerChunk) {
                color = chunkYouInTextColor.get();
            }



            //проверяю, есть ли в чанке энтити, который больше лимита
            for(EntitiesInChunkClass entity : chunk.entities) {
                if(isOverThanLimit(entity,typeLimits)){
                    color=new SettingColor(Color.RED);
                    break;
                }
            }



            String chunk_string = chunk.x + " " + chunk.z + " - " + chunk.getCount() + " сущностей:";
            strings.add(new RenderStringClass(chunk_string, color));


            if (shouldSplit) {

                //прохожу по списку сущностей в чанке
                for (EntitiesInChunkClass entity : chunk.entities) {
                    SettingColor color_text = defaultTextColor.get();
                    if (isPlayerChunk) {
                        color_text = chunkYouInTextColor.get();
                    }
                    if (isOverThanLimit(entity, typeLimits)) {
                        color_text = new SettingColor(java.awt.Color.RED);
                    }

                    String subchunk_string = "           " + entity.entity_type.getDescription().getString() + " - " + entity.count;
                    strings.add(new RenderStringClass(subchunk_string, color_text));
                }

        }


        }


    }



    public boolean isOverThanLimit(EntitiesInChunkClass entities,Set<EntityTypeLimit>limits){

        for(EntityTypeLimit limit : limits){
            if(!limit.isEnabled)continue;

            if(limit.type.equals(entities.entity_type) && limit.limit>=0 && entities.count>=limit.limit){
                return true;
            }
        }
        return false;
    }


    public ArrayList<EntitiesInChunkClass> addEntityToChunk(ArrayList<EntitiesInChunkClass> entities,EntityType<?> entity_type){
        boolean isFind = false;
        for(EntitiesInChunkClass entity : entities){
            if(entity.entity_type.equals(entity_type)){
                entity.count++;
                isFind = true;
                break;
            }
        }
        if(!isFind){
            entities.add(new EntitiesInChunkClass(entity_type,1));
        }
        return entities;
    }



    public void updateSize(HudRenderer renderer, String string, boolean isFirst,SettingColor color){
        double moduleWidth = renderer.textWidth(string) + renderer.textWidth(" ");
        double x = this.x + alignX(moduleWidth, alignment.get());
        //SettingColor color = defaultTextColor.get();
//        if(color){
//            color =  chunkYouInTextColor.get();
//        }
        x = renderer.text(string, x, y_temp, color, textShadow.get());
        //renderer.text(string, x, y, moduleColor.get(), textShadow.get());
        y_temp += renderer.textHeight() + 2;
        width = Math.max(width, moduleWidth);
        height += renderer.textHeight();
       // if(!isFirst)
            height += 2;
    }
    public Entity getPlayerByUUID(UUID uuid){
        if(mc==null || mc.level==null)return null;
        return mc.level.getEntity(uuid);
    }



}
