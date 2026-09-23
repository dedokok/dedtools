package com.dedokok.systems.modules.Feature;

import com.dedokok.events.meteor.MouseClickEvent;

import com.dedokok.events.render.Render2DEvent;
import com.dedokok.events.render.Render3DEvent;
import com.dedokok.events.world.TickEvent;
import com.dedokok.gui.utils.StarscriptTextBoxRenderer;
import com.dedokok.renderer.Renderer2D;
import com.dedokok.renderer.ShapeMode;
import com.dedokok.renderer.text.TextRenderer;
import com.dedokok.settings.*;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Module;
import com.dedokok.utils.entity.EntityUtils;
import com.dedokok.utils.misc.input.KeyAction;
import com.dedokok.utils.player.PlayerUtils;
import com.dedokok.utils.render.NametagUtils;
import com.dedokok.utils.render.RenderUtils;
import com.dedokok.utils.render.color.Color;
import com.dedokok.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class CirclePlayerCommand extends Module {
    public CirclePlayerCommand() {
        super(Categories.Feature, "CirclePlayerCommand", "Select player with circle and execute command on it",null);
        runInMainMenu = true;
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> isJustSpectatorSetting = sgGeneral.add(new BoolSetting.Builder()
            .name("work-only-in-spectator")
            .description("Does module will work just in spectator")
            .defaultValue(true)
            .build()
    );
    private final Setting<MessageTypes> sendTypeSetting = sgGeneral.add(new EnumSetting.Builder<MessageTypes>()
            .name("send-type")
            .description("What type of string need to send")
            .defaultValue(MessageTypes.Command)
            .build()
    );
    public enum MessageTypes {
        Command,
        Chat,
        System
    }
    private final Setting<String> executeSendSetting = sgGeneral.add(new StringSetting.Builder()
            .name("string-to-send")
            .description("String to send. All %nickname% will replaced with target nickname")
            .defaultValue((mc!=null&&mc.player!=null) ? ("msg "+mc.player.getName()+" "+"%nickname%") : ("thats %nickname%"))
            .renderer(StarscriptTextBoxRenderer.class)
            .build()
    );

    private final Setting<Integer> circleRadiusSetting = sgGeneral.add(new IntSetting.Builder()
            .name("circle-radius")
            .description("Radius of center circle")
            .defaultValue(70)
            .sliderMax(500)
            .sliderMin(0)
            .build()
    );

    private final Setting<Integer> circleWidthSetting = sgGeneral.add(new IntSetting.Builder()
            .name("circle-width")
            .description("Circle border width")
            .defaultValue(3)
            .sliderMax(100)
            .sliderMin(1)
            .build()
    );

    private final Setting<Integer> circleSegmentsSetting = sgGeneral.add(new IntSetting.Builder()
            .name("circle-segments")
            .description("Circle segments amount")
            .defaultValue(32)
            .sliderMax(100)
            .sliderMin(1)
            .build()
    );

    public final Setting<SettingColor> circleColorSetting = sgGeneral.add(new ColorSetting.Builder()
            .name("circle-color")
            .description("Circle color")
            .defaultValue(new SettingColor(255, 255, 255))
            .build()
    );
    private final Setting<Integer> circleXOffset = sgGeneral.add(new IntSetting.Builder()
            .name("circle-x-pos-offset")
            .description("X circle offset")
            .defaultValue(0)
            .sliderMax(20)
            .sliderMin(-20)
            .build()
    );
    private final Setting<Integer> circleYOffset = sgGeneral.add(new IntSetting.Builder()
            .name("circle-y-pos-offset")
            .description("Y circle offset")
            .defaultValue(0)
            .sliderMax(20)
            .sliderMin(-20)
            .build()
    );
    public final Setting<Double> fillOpacity = sgGeneral.add(new DoubleSetting.Builder()
            .name("fill-opacity")
            .description("The opacity of the shape fill.")
            //.visible(() -> shapeMode.get() != ShapeMode.Lines && mode.get() != ESP.Mode.Glow)
            .defaultValue(0.3)
            .range(0, 1)
            .sliderMax(1)
            .build()
    );

//    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
//            .name("scale")
//            .description("The scale of the nametag.")
//            .defaultValue(1.1)
//            .min(0.1)
//            .build()
//    );


    private int count;

    private final Color lineColor = new Color();
    private final Color sideColor = new Color();
    private final Color baseColor = new Color();

    private final Vector3d pos1 = new Vector3d();
    private final Vector3d pos2 = new Vector3d();
    private final Vector3d pos = new Vector3d();

    private int count_2;

    Entity hover_entity = null;
    private Entity target_entity = null;






    boolean stop = true;
    @EventHandler
    private void onRender2D(Render2DEvent event) {

        if(mc.player==null || RenderUtils.center==null)return;
        if (mc.gameRenderer.gameRenderState().guiRenderState.isHudHidden)
            return;
        //count = 0;


        if (Renderer2D.COLOR == null) return;


        //event.graphics.drawString(mc.font, "TEST123", 100, 100, 0xFFFFFF, true);
//        event.graphics.text(mc.font,"TEST123", 100, 100, 0xFFFFFF, true);
       // event.graphics.item(new ItemStack(Items.DIAMOND),200,100);


       // var scaledW = mc.getWindow().getGuiScaledWidth();
       // var scaledH = mc.getWindow().getGuiScaledHeight();
       // event.graphics.text(mc.font, "TEST123", scaledW / 2, scaledH / 2, 0xFFFFFF, true);
        //event.graphics.scissorStack(0, 0, scaledW, scaledH); // или как называется push-метод у тебя
       // event.graphics.text(mc.font, "TEST123", scaledW / 2, scaledH / 2, 0xFFFFFF, true);
        //event.graphics.popScissor();
        //event.graphics.
        //Font.PreparedText prepared = mc.font.prepareText(Language.getInstance().getVisualOrder(FormattedText.of("TEST123")), 100f, 100f, 0xFFFFFF, true, false, 0);
        //System.out.println("prepared bounds: " + prepared.bounds());


        var window = mc.getWindow();
        double cx = Math.round(window.getWidth() / 2.0)+circleXOffset.get();
        double cy = Math.round(window.getHeight() / 2.0)+circleYOffset.get();
        Renderer2D.COLOR.begin();




        //circle
        int radius = circleRadiusSetting.get();
        int outerRadius = radius+circleWidthSetting.get();
        int segments = circleSegmentsSetting.get();
        Color color = circleColorSetting.get();
        Renderer2D.COLOR.ring(cx, cy, radius, outerRadius, segments, color);

        double minDist=-1;

        Entity temp_target_entity = null;


        if(target_entity!=null){
            boolean isFind = false;
            for (Entity entity : mc.level.entitiesForRendering()) {
                if(entity.equals(target_entity)){
                    isFind = true;
                    break;
                }
            }
            if(!isFind){
                isPressed=false;
                isReleased=false;
                target_entity=null;
            }
        }


        if(target_entity==null) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (!(entity instanceof Player) || entity.getUUID().equals(mc.player.getUUID())) continue;
                Double dist = distanceToScreenCenter(entity, event.tickDelta, cx, cy);
                if (dist == null) continue;
                if (dist > (circleRadiusSetting.get() + circleWidthSetting.get())) continue;


                if (minDist == -1) minDist = dist;
                if (dist <= minDist) {
                    minDist = dist;
                    temp_target_entity = entity;
                }
            }
            hover_entity = temp_target_entity;
        }


//        Entity temp_en = target_entity!=null?target_entity:hover_entity;
//        if(temp_en!=null) {
//
//            Utils.set(pos, temp_en, event.tickDelta);
//            pos.add(0, getHeight(temp_en), 0);
//            boolean shadow = Config.get().customFont.get();
//            if (NametagUtils.to2D(pos, scale.get())) {
//                if (temp_en instanceof Player player) renderNametagPlayer(event, player, shadow);
//            }
//        }

        Renderer2D.COLOR.render();
    }
    private double getHeight(Entity entity) {
        double height = entity.getEyeHeight(entity.getPose());

        if (entity.getType() == EntityTypes.ITEM || entity.getType() == EntityTypes.ITEM_FRAME || entity.getType() == EntityTypes.GLOW_ITEM_FRAME)
            height += 0.2;
        else height += 0.5;

        return height;
    }

    private Double distanceToScreenCenter(Entity entity, float tickDelta, double cx, double cy) {
        double x = Mth.lerp(tickDelta, entity.xOld, entity.getX());
        double y = Mth.lerp(tickDelta, entity.yOld, entity.getY());
        double z = Mth.lerp(tickDelta, entity.zOld, entity.getZ());

        double height = entity.getBoundingBox().maxY - entity.getBoundingBox().minY;
        y += height / 2; // центр сущности по высоте

        Vector3d pos = new Vector3d(x, y, z);
        if (!NametagUtils.to2D(pos, 1)) return null; // за камерой / не проецируется

        double dx = pos.x - cx;
        double dy = pos.y - cy;
        return Math.sqrt(dx * dx + dy * dy);
    }



    @EventHandler
    public void onTick(TickEvent.Post event){
        if(mc.player==null)return;
        if(!mc.player.isSpectator() && isJustSpectatorSetting.get()){
            disable();
        }
    }
    @Override
    public void onActivate(){
        if(mc.player==null)return;
        if(!mc.player.isSpectator() && isJustSpectatorSetting.get()){
            disable();
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.player == null || RenderUtils.center == null) return;
        count = 0;

        Color color = Color.WHITE;

        Entity entity = target_entity!=null ?target_entity: hover_entity;
        if (entity == null) return;
        double x = entity.xo + (entity.getX() - entity.xo) * event.tickDelta;
        double y = entity.yo + (entity.getY() - entity.yo) * event.tickDelta;
        double z = entity.zo + (entity.getZ() - entity.zo) * event.tickDelta;

        double height = entity.getBoundingBox().maxY - entity.getBoundingBox().minY;
        y += height / 2;

        event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, x, y, z, color);

        event.renderer.line(x, entity.getY(), z, x, entity.getY() + height, z, color);
        drawBoundingBox(event, entity);
        count++;


    }

    private double angleToCrosshair(Entity entity, float tickDelta) {
        Vec3 eyePos = mc.player.getEyePosition(tickDelta);
        Vec3 lookVec = mc.player.getViewVector(tickDelta).normalize();

        double x = entity.xo + (entity.getX() - entity.xo) * tickDelta;
        double y = entity.yo + (entity.getY() - entity.yo) * tickDelta;
        double z = entity.zo + (entity.getZ() - entity.zo) * tickDelta;

        double height = entity.getBoundingBox().maxY - entity.getBoundingBox().minY;
        y += height / 2;

        Vec3 toEntity = new Vec3(x, y, z).subtract(eyePos).normalize();

        double dot = lookVec.dot(toEntity);
        double angleRad = Math.acos(Mth.clamp(dot, -1.0, 1.0));

        return Math.toDegrees(angleRad);
    }


    public boolean isPressed = false;
    public boolean isReleased = false;
    @EventHandler
    public void mouseClickEvent(MouseClickEvent event) {
        if(mc.gui.screen() != null) return;
        if(event.action == KeyAction.Press && event.input.button()==1){
            isPressed=true;
            target_entity=hover_entity;
            event.cancel();
        }
        else if(event.action == KeyAction.Release && event.input.button()==1){
            isReleased=false;
            isPressed=false;
            target_entity=null;
        }
        else if(isPressed && event.action == KeyAction.Press && event.input.button()==2){
            isPressed=false;
            isReleased=false;
            sendString();
            event.cancel();
        }

    }

    private void sendString(){
        switch(sendTypeSetting.get()){
            case MessageTypes.Command: {
                sendCommand();
                break;
            }
            case MessageTypes.Chat: {
                sendChat();
                break;
            }
            case MessageTypes.System:{
                sendSystem();
                break;
            }
        }
    }

    private void sendCommand(){
        if(mc==null || mc.getConnection()==null)return;
        String command = executeSendSetting.get();
        if(command==null)return;
        String target_name = target_entity.getName().getString();
        command = command.replaceAll("%nickname%", target_name);

        mc.getConnection().sendCommand(command);
        target_entity=null;
    }

    private void sendChat(){
        if(mc==null || mc.getConnection()==null) return;
        String message = executeSendSetting.get();
        if(message==null)return;
        String target_name = target_entity.getName().getString();
        message = message.replaceAll("%nickname%", target_name);
        mc.getConnection().sendChat(message);
        target_entity=null;
    }
    private void sendSystem(){
        if(mc==null ||mc.player==null) return;
        String message = executeSendSetting.get();
        if(message==null)return;
        String target_name = target_entity.getName().getString();
        message = message.replaceAll("%nickname%", target_name);
        mc.player.sendSystemMessage(Component.literal(message));
        target_entity=null;
    }


    private void drawBoundingBox(Render3DEvent event, Entity entity) {
        Color color = circleColorSetting.get();
        if (color != null) {
            lineColor.set(color);
            sideColor.set(color).a((int) (sideColor.a * fillOpacity.get()));
        }


        double x = Mth.lerp(event.tickDelta, entity.xOld, entity.getX()) - entity.getX();
        double y = Mth.lerp(event.tickDelta, entity.yOld, entity.getY()) - entity.getY();
        double z = Mth.lerp(event.tickDelta, entity.zOld, entity.getZ()) - entity.getZ();

        ShapeMode shape = ShapeMode.Both;

        AABB box = entity.getBoundingBox();
        event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ, sideColor, lineColor, shape, 0);
    }

    private final Color WHITE = new Color(255, 255, 255);
    private final Color RED = new Color(255, 25, 25);
    private final Color AMBER = new Color(255, 105, 25);
    private final Color GREEN = new Color(25, 252, 25);
    private final Color GOLD = new Color(232, 185, 35);
    private void renderNametagPlayer(Render2DEvent event, Player player, boolean shadow) {
        String name = player.getName().getString();
        //System.out.println("renderNametagPlayer called for: " + name + " pos=" + pos.x + "," + pos.y + "," + pos.z);

        TextRenderer text = TextRenderer.get();
        // Name

        NametagUtils.begin(pos, event.graphics);


        Color nameColor = circleColorSetting.get();



        // Health
        float absorption = player.getAbsorptionAmount();
        int health = Math.round(player.getHealth() + absorption);
        double healthPercentage = health / (player.getMaxHealth() + absorption);

        String healthText = " " + health;
        Color healthColor;

        if (healthPercentage <= 0.333) healthColor = RED;
        else if (healthPercentage <= 0.666) healthColor = AMBER;
        else healthColor = GREEN;

        // Ping
        int ping = EntityUtils.getPing(player);
        String pingText = " [" + ping + "ms]";

        // Distance
        double dist = Math.round(PlayerUtils.distanceToCamera(player) * 10.0) / 10.0;
        String distText = " " + dist + "m";

        // Calc widths
        //double gmWidth = text.getWidth(gmText, shadow);
        double nameWidth = text.getWidth(name, shadow);
        double healthWidth = text.getWidth(healthText, shadow);
        double pingWidth = text.getWidth(pingText, shadow);
        double distWidth = text.getWidth(distText, shadow);

        double width = nameWidth;

        width += healthWidth;
        width += pingWidth;
        width += distWidth;

        double widthHalf = width / 2;
        double heightDown = text.getHeight(shadow);

        //drawBg(-widthHalf, -heightDown, width, heightDown);

        //drawBg(pos.x, pos.y, -widthHalf, -heightDown, width, heightDown);

        // Render texts
        text.beginBig(event.graphics);
        double hX = -widthHalf;
        double hY = -heightDown;

        //if (displayGameMode.get()) hX = text.render(gmText, hX, hY, gamemodeColor.get(), shadow);
        //System.out.println("about to render text, hX=" + hX + " hY=" + hY);
        hX = text.render(name, hX, hY, nameColor, shadow);
        //System.out.println("text rendered, new hX=" + hX);
        hX = text.render(healthText, hX, hY, healthColor, shadow);
        hX = text.render(pingText, hX, hY, circleColorSetting.get(), shadow);

        text.render(distText, hX, hY, circleColorSetting.get(), shadow);


        text.end();
        NametagUtils.end(event.graphics);


    }
    private void drawBg(double screenX, double screenY, double x, double y, double width, double height) {
       // Renderer2D.COLOR.begin();
        Renderer2D.COLOR.quad(screenX + x - 1, screenY + y - 1, width + 2, height + 2, Color.WHITE);
        //Renderer2D.COLOR.render();
    }




}
