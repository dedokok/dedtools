package com.dedokok.systems.modules.Feature;

import com.dedokok.events.meteor.MouseClickEvent;
import com.dedokok.events.render.GUIRenderEvent;
import com.dedokok.events.render.Render3DEvent;
import com.dedokok.events.world.TickEvent;
import com.dedokok.gui.GuiHitTest;
import com.dedokok.gui.GuiTheme;
import com.dedokok.gui.WidgetScreen;
import com.dedokok.gui.themes.meteor.widgets.WMeteorQuad;
import com.dedokok.gui.themes.meteor.widgets.WMeteorVeinChoose;
import com.dedokok.gui.widgets.*;
import com.dedokok.settings.*;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Module;
import com.dedokok.utils.render.DisplayItemUtils;
import com.dedokok.utils.render.color.Color;
import com.dedokok.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class CoordMaster extends Module {
    public CoordMaster() {
        super(Categories.Feature, "CoordMaster", "Go to coords in spectator",null);
        runInMainMenu = true;
    }
    private Screen owner = null;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> yPosSetting = sgGeneral.add(new IntSetting.Builder()
            .name("y-pos")
            .description("y position of hud")
            .defaultValue(1)
            .sliderMax(2000)
            .sliderMin(0)
            .build()
    );

//    private final Setting<Set<EntityType<?>>>entityTypeSetting = sgGeneral.add(new TestSetting.Builder()
//            .name("entity-type-list")
//            .description("entity type list")
//            .build()
//    );
//    private final ButtonSetting buttonSetting = sgGeneral.add(new ButtonSetting.Builder()
//            .defaultValue(screen)
//            .width(50)
//            .onChanged(value-> {
//                //openVeinChoose();
//                 isRender=!isRender;
//                 if(!isRender){
//                     item_x=-1;
//                     item_y=-1;
//                 }
//                 else{
//                     owner = mc.gui.screen();
//                     needItemMouse=true;
//                 }
//            })
//            .name("Confirm mass add")
//            .build()
//    );
    double item_x = -1;
    double item_y = -1;
    boolean needItemMouse = false;


    public final Setting<SettingColor> localColorSetting = sgGeneral.add(new ColorSetting.Builder()
            .name("local-chat-color")
            .description("Color of local chat")
            .defaultValue(new SettingColor(170, 170, 170))
            .build()
    );

    @Override
    public void onActivate(){
//        if(mc.player ==null || !mc.player.isSpectator()){
//            disable();
//            return;
//        }
//        double newX = mc.player.getX()+yPosSetting.get();
//        mc.player.setPos(newX, mc.player.getY(), mc.player.getZ());
////        mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(
////                newX, mc.player.getY(), mc.player.getZ(),
////                mc.player.onGround(),
////                mc.player.horizontalCollision
////        ));
    }
    private static Vec3 target = null;
    public static void flyTo(double x, double y, double z) {
        target = new Vec3(x, y, z);
    }

    @EventHandler
    public void onTick(TickEvent.Post event){
        if(!isActive())return;
        if(veinChoose == null && screen!=null){
            //ensureVeinChoose(screen.getRenderer().theme);
        }
    }



    public static WVeinChoose veinChoose = null;
    public static boolean isRender = false;
    @EventHandler
    private void onRender(GUIRenderEvent event){
        if(mc != null && mc.gui.screen() != owner){
            owner=null;
            isRender=false;
            item_x=-1;
            item_y=-1;
            veinChoose=null;
        }
        if(!isRender)return;

        if(needItemMouse) {
            needItemMouse = false;
            item_x = event.mouseX;
            item_y = event.mouseY;
        }

        double px = mc.mouseHandler.xpos();   // пиксели окна, тот же масштаб, что в GuiHitTest.update
        double py = mc.mouseHandler.ypos();

        if (veinChoose == null) {
            //veinChoose = event.guiRenderer.createVeinMenu();   // создание и init(): один раз
            veinChoose.layer = 2;
        }


        veinChoose = event.guiRenderer.renderVeinMenu(
                veinChoose,
                screen.getRenderer().graphics,
                item_x, item_y,          // позиция меню: статичная
                px, py,                  // курсор: живой
                event.tickDelta / 20
        );        //event.guiRenderer.renderWidget(quad,100,100,event.tickDelta);
        veinChoose.layer = 2;
        GuiHitTest.register(veinChoose);
    }

    public void closeVeinChoose() {
        if (veinChoose != null) veinChoose.visible = false;
        isRender = false;
        owner = null;
    }


    private void ensureVeinChoose(GuiTheme theme) {
        if (veinChoose != null) return;

        //veinChoose = new WMeteorVeinChoose("text");
        veinChoose.theme = theme;
        veinChoose.init();
        veinChoose.layer = 2;
        veinChoose.visible = false;      // скрыт по умолчанию

        screen.add(veinChoose);          // теперь это часть дерева root
    }

    public void openVeinChoose() {
        if(screen==null)return;
        ensureVeinChoose(screen.getRenderer().theme);

        veinChoose.customX = mc.mouseHandler.xpos();   // см. пункт 3
        veinChoose.customY = mc.mouseHandler.ypos();

        veinChoose.calculateSize();
        veinChoose.calculateWidgetPositions();
        veinChoose.visible = true;

        isRender = true;
        owner = mc.gui.screen();

        // сразу проставить hover под текущим курсором, а не ждать движения мыши
        veinChoose.mouseMoved(mc.mouseHandler.xpos(), mc.mouseHandler.ypos(),
                mc.mouseHandler.xpos(), mc.mouseHandler.ypos());
    }


//    @EventHandler
//    private void onTick(TickEvent.Post event){
//        i
//    }

    @EventHandler
    private void onMouseClick(MouseClickEvent event){
        if(veinChoose!=null && mc!=null && mc.gui.screen()==owner){
            double mouse_x = mc.mouseHandler.xpos();
            double mouse_y = mc.mouseHandler.ypos();
            //WWidget top = GuiHitTest.getTopmost();
//            System.out.println("raw=" + veinChoose.isOverRaw(mouse_x, mouse_y)
//                    + " isOver=" + veinChoose.isOver(mouse_x, mouse_y)
//                    + " top=" + (top == null ? "null" : top.getClass().getSimpleName())
//                    + " topIsVein=" + (top == veinChoose));
            if(!veinChoose.isOverRaw(mouse_x,mouse_y)){
                isRender=false;
                item_x=-1;
                item_y=-1;
                veinChoose=null;
                //closeVeinChoose();
            }
        }
    }

}
