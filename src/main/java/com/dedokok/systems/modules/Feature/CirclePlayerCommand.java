package com.dedokok.systems.modules.Feature;

import com.dedokok.events.meteor.MouseClickEvent;

import com.dedokok.events.render.Render2DEvent;
import com.dedokok.events.render.Render3DEvent;
import com.dedokok.events.world.TickEvent;
import com.dedokok.gui.GuiTheme;
import com.dedokok.gui.widgets.WLabel;
import com.dedokok.gui.widgets.WWidget;
import com.dedokok.gui.widgets.containers.WTable;
import com.dedokok.gui.widgets.containers.WVerticalList;
import com.dedokok.gui.widgets.input.WDropdown;
import com.dedokok.gui.widgets.input.WTextBox;
import com.dedokok.gui.widgets.pressable.WButton;
import com.dedokok.gui.widgets.pressable.WCheckbox;
import com.dedokok.gui.widgets.pressable.WMinus;
import com.dedokok.renderer.Renderer2D;
import com.dedokok.renderer.ShapeMode;
import com.dedokok.settings.*;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Feature.CirclePlayer.CirclePlayerBlock;
import com.dedokok.systems.modules.Feature.CirclePlayer.CirclePlayerBlockFactory;
import com.dedokok.systems.modules.Feature.CirclePlayer.CirclePlayerClass;
import com.dedokok.systems.modules.Module;
import com.dedokok.utils.misc.input.KeyAction;
import com.dedokok.utils.render.NametagUtils;
import com.dedokok.utils.render.RenderUtils;
import com.dedokok.utils.render.color.Color;
import com.dedokok.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

import static com.dedokok.gui.renderer.GuiRenderer.ARROW_DOWN;
import static com.dedokok.gui.renderer.GuiRenderer.ARROW_UP;

public class CirclePlayerCommand extends Module {
    public CirclePlayerCommand() {
        super(Categories.Feature, "CirclePlayerCommand", "Select player with circle and execute command on it",null);
        runInMainMenu = true;
    }

    public static List<CirclePlayerBlock> commands = new ArrayList<>();
    private final CirclePlayerBlockFactory factory = new CirclePlayerBlockFactory();

    private final SettingGroup sgStyle = settings.createGroup("Style");


    public static List<CirclePlayerClass>sends = new ArrayList<>();

    public enum MessageTypes {
        Command,
        Chat,
        System
    }
//    private final Setting<String> executeSendSetting = sgGeneral.add(new StringSetting.Builder()
//            .name("string-to-send")
//            .description("String to send. All %nickname% will replaced with target nickname")
//            .defaultValue((mc!=null&&mc.player!=null) ? ("msg "+mc.player.getName()+" "+"%nickname%") : ("thats %nickname%"))
//            .renderer(StarscriptTextBoxRenderer.class)
//            .build()
//    );

    private final Setting<Integer> circleRadiusSetting = sgStyle.add(new IntSetting.Builder()
            .name("circle-radius")
            .description("Radius of center circle")
            .defaultValue(70)
            .sliderMax(500)
            .sliderMin(0)
            .build()
    );

    private final Setting<Integer> circleWidthSetting = sgStyle.add(new IntSetting.Builder()
            .name("circle-width")
            .description("Circle border width")
            .defaultValue(3)
            .sliderMax(100)
            .sliderMin(1)
            .build()
    );

    private final Setting<Integer> circleSegmentsSetting = sgStyle.add(new IntSetting.Builder()
            .name("circle-segments")
            .description("Circle segments amount")
            .defaultValue(32)
            .sliderMax(100)
            .sliderMin(1)
            .build()
    );

    public final Setting<SettingColor> circleColorSetting = sgStyle.add(new ColorSetting.Builder()
            .name("circle-color")
            .description("Circle color")
            .defaultValue(new SettingColor(255, 255, 255))
            .build()
    );
    private final Setting<Integer> circleXOffset = sgStyle.add(new IntSetting.Builder()
            .name("circle-x-pos-offset")
            .description("X circle offset")
            .defaultValue(0)
            .sliderMax(20)
            .sliderMin(-20)
            .build()
    );
    private final Setting<Integer> circleYOffset = sgStyle.add(new IntSetting.Builder()
            .name("circle-y-pos-offset")
            .description("Y circle offset")
            .defaultValue(0)
            .sliderMax(20)
            .sliderMin(-20)
            .build()
    );
    public final Setting<Double> fillOpacity = sgStyle.add(new DoubleSetting.Builder()
            .name("fill-opacity")
            .description("The opacity of the shape fill.")
            //.visible(() -> shapeMode.get() != ShapeMode.Lines && mode.get() != ESP.Mode.Glow)
            .defaultValue(0.3)
            .range(0, 1)
            .sliderMax(1)
            .build()
    );

    private final SettingGroup sgSettings = settings.createGroup("Settings");
    private final Setting<Integer> timeBetwSends = sgStyle.add(new IntSetting.Builder()
            .name("time-sends")
            .description("Time between sends in ticks")
            .defaultValue(0)
            .sliderMax(100)
            .sliderMin(0)
            .build()
    );

//    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
//            .name("scale")
//            .description("The scale of the nametag.")
//            .defaultValue(1.1)
//            .min(0.1)
//            .build()
//    );




    private final Color lineColor = new Color();
    private final Color sideColor = new Color();


    Entity hover_entity = null;
    private Entity target_entity = null;


    @EventHandler
    private void onRender2D(Render2DEvent event) {

        if(mc.player==null || RenderUtils.center==null || mc.level==null)return;
        if (mc.gameRenderer.gameRenderState().guiRenderState.isHudHidden)
            return;

        if (Renderer2D.COLOR == null) return;

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


        Renderer2D.COLOR.render();
    }


    private Double distanceToScreenCenter(Entity entity, float tickDelta, double cx, double cy) {
        double x = Mth.lerp(tickDelta, entity.xOld, entity.getX());
        double y = Mth.lerp(tickDelta, entity.yOld, entity.getY());
        double z = Mth.lerp(tickDelta, entity.zOld, entity.getZ());

        double height = entity.getBoundingBox().maxY - entity.getBoundingBox().minY;
        y += height / 2;

        Vector3d pos = new Vector3d(x, y, z);
        if (!NametagUtils.to2D(pos, 1)) return null;

        double dx = pos.x - cx;
        double dy = pos.y - cy;
        return Math.sqrt(dx * dx + dy * dy);
    }



    @EventHandler
    public void onTick(TickEvent.Post event){
        if(mc.player==null)return;
        if(!mc.player.isSpectator()){
            disable();
        }
    }
    @Override
    public void onActivate(){
        if(mc.player==null)return;
        if(!mc.player.isSpectator()){
            disable();
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.player == null || RenderUtils.center == null) return;

        Color color = circleColorSetting.get();

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
            sendName = target_entity.getName().getString();
            startSend(name);
            //sendString();
            event.cancel();
        }

    }

    int ticks_command = 0;
    int send_index = 0;
    boolean needSend = false;
    String sendName = null;
    public void startSend(String name){


        ticks_command = 0;
        send_index = 0;
        needSend = true;
        isFirst=true;
    }

    @EventHandler
    private void onTick2(TickEvent.Post event){
        if(needSend){
            doSend(send_index,timeBetwSends.get());
            ticks_command++;
        }
    }

    boolean isFirst = false;

    public void doSend(int index, int command_delay){
        if(index>=sends.size()){
            needSend = false;
            return;
        }
        if(isFirst || command_delay<=ticks_command) {
            CirclePlayerClass send = sends.get(index);
            if (send.enabled) {
                sendString(send);
                ticks_command = 0;
                isFirst=false;

                //return;
            }
            send_index=index+1;
            //if (send_index< sends.size()) {
            doSend(send_index, command_delay);
            //}
        }
    }

    private void sendString(CirclePlayerClass send){
//        if(target_entity==null){
//            needSend=false;
//            return;
//        }
        String message = send.sendString.replaceAll("%nickname%", sendName);
//        String message = send.sendString.replaceAll("%nickname%", target_entity.getName().getString());
        switch(send.type){
            case MessageTypes.Command: {
                sendCommand(message);
                break;
            }
            case MessageTypes.Chat: {
                sendChat(message);
                break;
            }
            case MessageTypes.System:{
                sendSystem(message);
                break;
            }
        }
    }


    private void sendCommand(String command){
        if(mc==null || mc.getConnection()==null)return;
        if(command==null)return;
        mc.getConnection().sendCommand(command);
        target_entity = null;
    }

    private void sendChat(String message){
        if(mc==null || mc.getConnection()==null) return;
        if(message==null)return;
        mc.getConnection().sendChat(message);
        target_entity = null;
    }
    private void sendSystem(String message){
        if(mc==null ||mc.player==null) return;
        if(message==null)return;
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










    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();

        ListTag list = new ListTag();
//        for (CirclePlayerBlock command : commands) {
//            CompoundTag mTag = new CompoundTag();
//            mTag.putString("type", command.sendTypeSetting.get().name());
//            mTag.putString("command", command.commandSetting.get());
//            mTag.putBoolean("enabled",command.enabled);
//
//
//            list.add(mTag);
//        }

        for (CirclePlayerClass send : sends) {
            CompoundTag mTag = new CompoundTag();
            mTag.putString("type", send.type.name());
            mTag.putString("command", send.sendString);
            mTag.putBoolean("enabled",send.enabled);


            list.add(mTag);
        }
        tag.put("commands", list);
        return tag;
    }
    public static List<CirclePlayerClass> readCommandsFromTag(CompoundTag tag, CirclePlayerBlockFactory factory) {
    //public static List<CirclePlayerBlock> readCommandsFromTag(CompoundTag tag, CirclePlayerBlockFactory factory) {
        //List<CirclePlayerBlock> commandList = new ArrayList<>();
        List<CirclePlayerClass> commandList = new ArrayList<>();
        ListTag list = tag.getListOrEmpty("commands");

//        for (Tag tagII : list) {
//            CompoundTag tagI = (CompoundTag) tagII;
//            String type = tagI.getStringOr("type", "");
//            CirclePlayerBlock command = factory.createInstruction(type);
//
//
//            if (command != null) {
//                CompoundTag commandTag = (CompoundTag) tagI.get("command");
//                if (commandTag != null) command.fromTag(commandTag);
//                command.enabled = tagI.getBooleanOr("enabled", false);
//                commandList.add(command);
//            }
//        }

        for (Tag tagII : list) {
            CompoundTag tagI = (CompoundTag) tagII;
            CirclePlayerClass command = new CirclePlayerClass();


            if (command != null) {
                //CompoundTag commandTag = (CompoundTag) tagI.get("command");
                //if (commandTag != null) command.fromTag(commandTag);
                command.sendString=tagI.getStringOr("command", "");
                String type = tagI.getStringOr("type", "");
                command.type = MessageTypes.valueOf(type);
                command.enabled = tagI.getBooleanOr("enabled", false);
                commandList.add(command);
            }
        }

        return commandList;
    }
    public Module fromTag(CompoundTag tag) {
        super.fromTag(tag);

        //commands.clear();
        //commands = readCommandsFromTag(tag, factory);

        sends.clear();
        sends = readCommandsFromTag(tag, factory);

        return this;
    }



    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList list = theme.verticalList();
        fillWidget(theme, list);
        return list;
    }

    private void fillWidget(GuiTheme theme, WVerticalList list) {
        list.clear();
        WTable table = theme.table();



        boolean areAllEnabled = true;
        for(CirclePlayerClass send : sends){
            if(!send.enabled){
                areAllEnabled = false;
                break;
            }
        }
        final boolean aAE_2 = areAllEnabled;
        WCheckbox check_all = table.add(theme.checkbox(areAllEnabled)).widget();
        check_all.tooltip = "Check all sends";
        check_all.action = ()->{

              for(CirclePlayerClass send : sends){
                  send.enabled = !aAE_2;
              }
              reload();
        };

        WLabel label_send = table.add(theme.label("Send string")).widget();
        WLabel label_type = table.add(theme.label("Type")).widget();
        table.row();


//        for (CirclePlayerBlock command : commands) {
//            command.screen = screen;
//            //WDropdown<MessageTypes>dropdown = list.add(theme.dropdown(command.sendTypeSetting.get())).widget();
//            WTextBox textBox = table.add(theme.textBox(command.commandSetting.get())).minWidth(100).widget();
//            WDropdown<MessageTypes>dropdown = table.add(theme.dropdown(command.sendTypeSetting.get())).widget();
//            WCheckbox checkbox = table.add(theme.checkbox(command.enabled)).widget();
//            checkbox.action = ()->{
//                command.enabled = checkbox.checked;
//            };
//            //list.add(theme.settings(command.settings)).expandX();
//
//            //WContainer container = list.add(theme.horizontalList()).expandX().widget();
//
//
////            WButton copy = container.add(theme.button("copy")).right().widget();
//            WButton copy = table.add(theme.button("copy")).widget();
//            copy.tooltip = "Duplicate send.";
//            copy.action = () -> {
//                commands.add(commands.indexOf(command), command.copy());
//                fillWidget(theme, list);
//            };
//
////            WMinus remove = container.add(theme.minus()).right().widget();
//            WMinus remove = table.add(theme.minus()).widget();
//
//            remove.tooltip = "Remove send.";
//            remove.action = () -> {
//                commands.remove(command);
//                fillWidget(theme, list);
//            };
//            table.row();
//        }

        for (CirclePlayerClass send : sends) {
            //command.screen = screen;
            //WDropdown<MessageTypes>dropdown = list.add(theme.dropdown(command.sendTypeSetting.get())).widget();

            WCheckbox checkbox = table.add(theme.checkbox(send.enabled)).widget();
            checkbox.action = ()->{
                send.enabled = checkbox.checked;
                reload();
            };

            WTextBox textBox = table.add(theme.textBox(send.sendString)).minWidth(300).widget();
            textBox.action = () -> {
                send.sendString=textBox.get();
            };

            WDropdown<MessageTypes>dropdown = table.add(theme.dropdown(send.type)).widget();
            dropdown.action = () -> {
              send.type = dropdown.get();
            };
            //list.add(theme.settings(command.settings)).expandX();

            //WContainer container = list.add(theme.horizontalList()).expandX().widget();

            int index = sends.indexOf(send);
            WButton arrow_down = table.add(theme.button(ARROW_DOWN)).widget();
            arrow_down.tooltip = "Move down "+send.sendString;
            arrow_down.visible = index <sends.size()-1;
            arrow_down.action = () -> {
                int index_2 = sends.indexOf(send);
                sends.add(index_2 +2,send);
                sends.remove(index_2);
                //index_2++;
                //sends.add(sends.indexOf(send), new CirclePlayerClass(send.sendString,send.enabled,send.type));
                //fillWidget(theme, list);
                reload();
            };

            WButton arrow_up = table.add(theme.button(ARROW_UP)).widget();
            arrow_up.tooltip = "Move up "+send.sendString;
            arrow_up.visible = index >0;

            arrow_up.action = () -> {
                int index_2 = sends.indexOf(send);
                sends.add(index_2 -1,send);
                sends.remove(index_2 +1);
                //index--;
                //fillWidget(theme, list);
                reload();
            };

            WMinus remove = table.add(theme.minus()).widget();

            remove.tooltip = "Remove send.";
            remove.action = () -> {
                sends.remove(send);
                fillWidget(theme, list);
            };
            table.row();
        }
        list.add(table);

        if (!sends.isEmpty()) list.add(theme.horizontalSeparator()).expandX();

        WTable controls = list.add(theme.table()).expandX().widget();

        WButton createCommand = controls.add(theme.button("New send")).expandX().widget();
        createCommand.action = () -> {
            CirclePlayerClass announceMessage = new CirclePlayerClass();
            sends.add(announceMessage);
            fillWidget(theme, list);
        };

        WButton removeAll = controls.add(theme.button("Remove all sends")).expandX().widget();
        removeAll.action = () -> {
            sends.clear();
            fillWidget(theme, list);
        };

        //MeteoristConfigManager.configManager(theme, list, this);
    }


}
