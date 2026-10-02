package com.dedokok.systems.modules.Feature;

import com.dedokok.events.render.Render3DEvent;
import com.dedokok.events.world.TickEvent;
import com.dedokok.renderer.ShapeMode;
import com.dedokok.settings.*;
import com.dedokok.systems.hud.HudRenderer;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Module;
import com.dedokok.systems.modules.Modules;
import com.dedokok.utils.classes.*;
import com.dedokok.utils.misc.text.MessageToSend;
import com.dedokok.utils.render.RenderUtils;
import com.dedokok.utils.render.color.Color;
import com.dedokok.utils.render.color.SettingColor;
import com.mojang.authlib.GameProfile;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dedokok.DedTools.mc;

public class EntitiesInChunk extends Module {

    public EntitiesInChunk() {
        super(Categories.Feature, "EntitiesInChunk", "Amount of entities in chunk", null);
        runInMainMenu = true;
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> chunkRadiusSetting = sgGeneral.add(new IntSetting.Builder()
            .name("chunk-radius")
            .description("Radius in chunks")
            .defaultValue(0)
            .min(0)
            .max(1000)
            .sliderMax(100)
            .build()
    );

    private final Setting<Integer> updateDelaySetting = sgGeneral.add(new IntSetting.Builder()
            .name("update-delay")
            .description("How many ticks need to update entities in chunks")
            .defaultValue(0)
            .min(0)
            .sliderMax(100)
            .build()
    );

    public final Setting<Boolean> splitEntitiesTypesSetting = sgGeneral.add(new BoolSetting.Builder()
            .name("split-entities-types")
            .description("Render what entities types are in chunk")
            .defaultValue(true)
            .build()
    );


    public final Setting<RenderType> renderTypeSetting = sgGeneral.add(new EnumSetting.Builder<RenderType>()
            .name("render-type")
            .description("Type of render info about chunks. All - all chunks. MoreThan - render chunks where amount of entities more than X")
            .defaultValue(RenderType.All)
            .build()
    );

    public enum RenderType {
        All,
        MoreThan
    }

    public final Setting<Integer> moreThanIntSetting = sgGeneral.add(new IntSetting.Builder()
            .name("min-entities-in-chunk")
            .description("Minimal amount of entities to render info")
            .defaultValue(0)
            .min(0)
            .sliderMax(100)
            .visible(() -> renderTypeSetting.get() == RenderType.MoreThan)
            .build()
    );


    private final Setting<Boolean> enableFilters = sgGeneral.add(new BoolSetting.Builder()
            .name("enable-entities-filters")
            .description("You can choose what entities amount to render")
            .defaultValue(true)
            .build()
    );


    public final Setting<Set<EntityTypeLimit>> entitiesTypeLimits = sgGeneral.add(new EntityTypeLimitsSetting.Builder()
            .name("entities-limits")
            .description("Limits of entitites")
            .visible(enableFilters::get)
            .build()
    );

    private final Setting<Boolean> renderChunkBorders = sgGeneral.add(new BoolSetting.Builder()
            .name("render-chunk-borders")
            .description("Does need to render chunk borders")
            .defaultValue(true)
            .build()
    );

    public final Setting<ChunkBorderType> chunkRenderTypeSetting = sgGeneral.add(new EnumSetting.Builder<ChunkBorderType>()
            .name("chunk-border-type")
            .description("Type of chunk border render")
            .defaultValue(ChunkBorderType.Full)
            .visible(renderChunkBorders::get)
            .build()
    );

    public enum ChunkBorderType {
        Full,
        Layer
    }

    public final Setting<Double> layerWidthSetting = sgGeneral.add(new DoubleSetting.Builder()
            .name("layer-width")
            .description("Width of layer")
            .defaultValue(1)
            .min(0)
            .sliderMax(1)
            .visible(() -> chunkRenderTypeSetting.get() == ChunkBorderType.Layer)
            .build()
    );

    public final Setting<chunkBorderLayerHeightType> chunkBorderLayerHeightTypeSetting = sgGeneral.add(new EnumSetting.Builder<chunkBorderLayerHeightType>()
            .name("chunk-border-height-type")
            .description("Height type of chunk border")
            .defaultValue(chunkBorderLayerHeightType.Player)
            .visible(()->
                renderChunkBorders.get() && (chunkRenderTypeSetting.get() == ChunkBorderType.Layer)
            )
            .build()
    );

    public enum chunkBorderLayerHeightType {
        Player,
        Set
    }

    public final Setting<Integer> chunkBorderLayerHeightSetting = sgGeneral.add(new IntSetting.Builder()
            .name("chunk-border-layer-height")
            .description("Height of chunk border layer")
            .defaultValue(100)
            .sliderMin(-64)
            .sliderMax(320)
            .visible(() -> (chunkBorderLayerHeightTypeSetting.get()==chunkBorderLayerHeightType.Set)&&(renderChunkBorders.get()))
            .build()
    );

    private final Setting<SettingColor> defaultChunkBorderColor = sgGeneral.add(new ColorSetting.Builder()
            .name("default-chunk-border-color")
            .description("Color of default chunk borders")
            .defaultValue(new SettingColor(java.awt.Color.GREEN))
            .visible(renderChunkBorders::get)
            .build()
    );
    private final Setting<SettingColor> overThanLimitChunkBorderColor = sgGeneral.add(new ColorSetting.Builder()
            .name("over-than-limit-chunk-border-color")
            .description("Color of chunk borders where entities amount over than limit")
            .defaultValue(new SettingColor(java.awt.Color.RED))
            .visible(renderChunkBorders::get)
            .build()
    );


    public int ticks = 0;
    public static HashMap<ChunkPos, EntityChunk> chunks = new HashMap<>();

    //public static ChunkPos lastChunkPos = new ChunkPos(0,0);
    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc == null || mc.player == null || mc.level == null) return;

        if (ticks < updateDelaySetting.get()) {
            ticks++;
            return;
        } else {
            ticks = 0;
        }

        ChunkPos chunkPos = mc.player.chunkPosition();
        //chunkPos = new ChunkPos(chunkPos.x()+1, chunkPos.z()+1);
        chunks.clear();
        addChunksInRadius(chunkPos);

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (enableFilters.get() && !isContainsAndEnabledByType(entity.getType())) {
                continue;
            }
            if (chunks.containsKey(entity.chunkPosition())) {
                EntityChunk entityChunk = chunks.get(entity.chunkPosition());
                entityChunk.increaseEntity(entity.getType());
            }
        }
    }


    public boolean isContainsAndEnabledByType(EntityType<?> entityType) {
        for (EntityTypeLimit typeLimit : entitiesTypeLimits.get()) {
            if (typeLimit.type.equals(entityType)) {
                return typeLimit.isEnabled;
            }
        }
        return false;
    }

    public void addChunksInRadius(ChunkPos player_chunk) {
        int radius = chunkRadiusSetting.get();

        int p_x = player_chunk.x(), p_z = player_chunk.z();
        int count = 0;
        if (radius == 0) {
            chunks.put(new ChunkPos(p_x, p_z), new EntityChunk(p_x, p_z, count, new ArrayList<>()));
        }

        for (int x = p_x - radius; x <= p_x + radius; x++) {
            for (int z = p_z - radius; z <= p_z + radius; z++) {
                chunks.put(new ChunkPos(x, z), new EntityChunk(x, z, count, new ArrayList<>()));
                count++;
            }
        }
    }


    //HashMap<ChunkPos,EntityChunk>last_chunks = new HashMap<>();


    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc == null || mc.player == null) return;

        List<EntityChunk> normalChunks = new ArrayList<>();
        List<EntityChunk> overLimitChunks = new ArrayList<>();


        ChunkRenderClass playerChunk = null;
        for (EntityChunk chunk : chunks.values()) {
            boolean overLimit = false;
            for (EntitiesInChunkClass entities : chunk.entities) {
                if (isOverThanLimit(entities, entitiesTypeLimits.get())) {
                    overLimit = true;
                    break;
                }
            }

            if(mc.player.chunkPosition().equals(new ChunkPos(chunk.x,chunk.z))) {
                playerChunk = new ChunkRenderClass((overLimit ? overThanLimitChunkBorderColor.get() : defaultChunkBorderColor.get()),chunk);
                continue;
            }

            (overLimit ? overLimitChunks : normalChunks).add(chunk);

        }


        for (EntityChunk chunk : normalChunks) {
            drawChunkBorder(chunk, defaultChunkBorderColor.get(), event);
        }
        for (EntityChunk chunk : overLimitChunks) {
            drawChunkBorder(chunk, overThanLimitChunkBorderColor.get(), event);
        }

        if(playerChunk != null) {
            drawChunkBorder(playerChunk.chunk,playerChunk.color,event);
        }

        //}
    }

    public void drawChunkBorder(EntityChunk chunk, Color color, Render3DEvent event) {
        double x = (chunk.x + 1) * 16;
        double z = (chunk.z + 1) * 16;


        double y1 = 0,y2=0;
        if(chunkRenderTypeSetting.get()==ChunkBorderType.Full) {
            y1=-64;
            y2=320;
        }
        else if(chunkRenderTypeSetting.get()==ChunkBorderType.Layer) {
            if(chunkBorderLayerHeightTypeSetting.get()==chunkBorderLayerHeightType.Player) {
                y1 = Mth.lerp(event.tickDelta, mc.player.yo, mc.player.getY());
                y2 = y1 - layerWidthSetting.get();
            }
            else if(chunkBorderLayerHeightTypeSetting.get()==chunkBorderLayerHeightType.Set) {
                y1 = chunkBorderLayerHeightSetting.get();
                y2 = y1- layerWidthSetting.get();
            }
        }


        double inset = 0.015;

        event.renderer.quadVertical(x - inset, y1, z - inset, x - 16 + inset, y2, z - inset, color);
        event.renderer.quadVertical(x - inset, y1, z - 16 + inset, x - 16 + inset, y2, z - 16 + inset, color);
        event.renderer.quadVertical(x - inset, y1, z - inset, x - inset, y2, z - 16 + inset, color);
        event.renderer.quadVertical(x - 16 + inset, y1, z - inset, x - 16 + inset, y2, z - 16 + inset, color);

    }


    //private void drawChunkBorder(Render3DEvent event, boolean) {}
    public boolean isOverThanLimit(EntitiesInChunkClass entities,Set<EntityTypeLimit>limits){

        for(EntityTypeLimit limit : limits){
            if(!limit.isEnabled)continue;

            if(limit.type.equals(entities.entity_type) && limit.limit>=0 && entities.count>=limit.limit){
                return true;
            }
        }
        return false;
    }
}




