package com.dedokok.gui.screens.settings;

import com.dedokok.events.render.GUIRenderEvent;
import com.dedokok.gui.GuiHitTest;
import com.dedokok.gui.GuiTheme;
import com.dedokok.gui.screens.settings.base.CollectionContainerSettingScreen;
import com.dedokok.gui.utils.Cell;
import com.dedokok.gui.widgets.WBBFItem;
import com.dedokok.gui.widgets.WVeinChoose;
import com.dedokok.gui.widgets.WWidget;
import com.dedokok.gui.widgets.containers.WContainer;
import com.dedokok.gui.widgets.input.WTextBox;
import com.dedokok.gui.widgets.pressable.WPressable;
import com.dedokok.settings.VeinsListSetting;
import com.dedokok.systems.modules.Feature.BlockBreakFinder;
import com.dedokok.utils.classes.Vein;
import com.dedokok.utils.render.DisplayItemUtils;
import com.mojang.blaze3d.platform.MacosUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static com.dedokok.DedTools.mc;
import static org.lwjgl.glfw.GLFW.*;

public class VeinsListSettingScreen extends CollectionContainerSettingScreen<Vein> {
    public VeinsListSettingScreen(GuiTheme theme, VeinsListSetting setting) {
        super(theme, "All veins", setting, BlockBreakFinder.veinsArrayList, BlockBreakFinder.veinsArrayList);
    }



    @Override
    protected WWidget getValueWidget(Vein value) {
        if(value.isRemoved)return null;
        Identifier itemId = Identifier.parse(value.rows.getFirst().getBlock());

        String tooltip = "";
        tooltip = tooltip+value.rows.getFirst().getBlock();
        tooltip = tooltip+"\n"+value.rows.getFirst().getUser();
        int[] coords = BlockBreakFinder.unpack(value.rows.getFirst().getCoords());
        tooltip = tooltip+"\n"+value.rows.getFirst().getWrld() + " "+ coords[0] + " "+coords[1]+" "+coords[2]+"\n";


        SimpleDateFormat sdfEST = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        sdfEST.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        String date = sdfEST.format(value.rows.getFirst().getTimestamp()*1000);

        tooltip = tooltip+date;

        tooltip=tooltip+"\nsize: "+value.rows.size();


        //BlockBreakFinder.teleported_vein=value;
        WBBFItem item = new WBBFItem(this,value.id,theme.item(DisplayItemUtils.toStack(BuiltInRegistries.ITEM.getValue(itemId))),
            coords[0],coords[1],coords[2],value.rows.getFirst().getUser(),date,value.rows.getFirst().getWrld());
        tooltip=tooltip+"\nid: "+value.id;
        item.tooltip=tooltip;



        return item;
    }

    @Override
    protected String[] getValueNames(Vein value) {
        return new String[]{
                value.rows.getFirst().getUser(),
                //BuiltInRegistries.BLOCK.getKey(BuiltInRegistries.BLOCK.getValue(Identifier.parse(value.getBlock()))).toString(),
        };
    }

    @Override
    protected boolean includeValue(Vein value) {

        Predicate<Vein> filter = ((VeinsListSetting) setting).filter;
        if (filter == null) return BuiltInRegistries.BLOCK.getValue(Identifier.parse(value.rows.getFirst().getBlock())) != Blocks.AIR;
        return filter.test(value);
    }



    @Override
    public void initWidgets() {
        // Filter
        WTextBox filter = add(theme.textBox("")).minWidth(400).expandX().widget();
        filter.setFocused(true);

        filter.action = () -> {
            filterText = filter.get().trim();
            table.clear();
            initTable();
        };

        table = add(theme.table()).expandX().widget();

        initTable();
    }




    @Override
    protected Long getTimestamp(Vein value){
        return value.rows.getFirst().getTimestamp();
    }

    private Screen owner = null;
    public static WVeinChoose veinChoose = null;
    public static boolean isRender = false;
    double item_x = -1;
    double item_y = -1;
    boolean needItemMouse = false;

    public Vein targetVein = null;

    public void closeWidget(){
        isRender=false;
        item_x=-1;
        item_y=-1;
        targetVein=null;
        veinChoose.visible=false;
        resetPressed(veinChoose);
        veinChoose=null;
        owner=null;
    }

    public void openWidget(int id){
        if(veinChoose!=null){
            closeWidget();
        }
        isRender=true;
        owner = mc.gui.screen();
        targetVein=BlockBreakFinder.veinsArrayList.get(id);
        needItemMouse=true;
    }

    public void openCloseWidget(int veinId){
        isRender=!isRender;
        if(!isRender || veinId==-1){
            closeWidget();
        }
        else{
            openWidget(veinId);
        }
    }

    private void resetPressed(WWidget w) {
        if (w instanceof WPressable p) p.pressed = false;   // сделайте pressed доступным (не private)

        if (w instanceof WContainer c) {
            for (Cell<?> cell : c.cells) resetPressed(cell.widget());
        }
    }

    public static boolean veinChooseUpdateScreen = false;

    @Override
    public void reload() {


        collection = BlockBreakFinder.veinsArrayList;
        registry=BlockBreakFinder.veinsArrayList;
        filterText="";
        ((VeinsListSetting) setting).filter = null;
        clear();
        initWidgets();
    }


    @EventHandler
    private void onRender(GUIRenderEvent event){

        if(mc != null && mc.gui.screen() != owner && targetVein!=null){
            openCloseWidget(-1);
        }

//        if(isUpdatedScreen && veinChooseUpdateScreen){
//            reload();
//            isUpdatedScreen=false;
//            veinChooseUpdateScreen=false;
//        }
        if(!isRender)return;

        if(needItemMouse) {
            needItemMouse = false;
            item_x = event.mouseX;
            item_y = event.mouseY;
        }

        double px = mc.mouseHandler.xpos();   // пиксели окна, тот же масштаб, что в GuiHitTest.update
        double py = mc.mouseHandler.ypos();

        if (veinChoose == null && targetVein!=null) {
            veinChoose = event.guiRenderer.createVeinMenu(this,targetVein);   // создание и init(): один раз
            veinChoose.layer = 2;
        }

        if(veinChoose ==null)return;
        veinChoose = event.guiRenderer.renderVeinMenu(
                veinChoose,
                getRenderer().graphics,
                item_x, item_y,          // позиция меню: статичная
                px, py,                  // курсор: живой
                event.tickDelta / 20
        );        //event.guiRenderer.renderWidget(quad,100,100,event.tickDelta);
        veinChoose.layer = 2;
        GuiHitTest.register(veinChoose);
    }

    @Override
    public void onClose() {
        if (!locked || lockedAllowClose) {
            System.out.println("Тот");
            if(veinChoose!=null)closeWidget();
            closing = true;
        }
        else{
            System.out.println("Не тот onClose");
        }
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent input) {
        System.out.println("list screen: "+this.getClass().getSimpleName());

        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {

            if(veinChoose!=null){
                closeWidget();
            }
            else {
                onClose();
            }
            return true;
        }
        if (locked) return false;

        boolean shouldReturn = root.keyPressed(input) || super.keyPressed(input);
//        if(this instanceof VeinsListSettingScreen){
//
//            System.out.println("shouldreturn: "+shouldReturn+". Key = "+input.key());
//            //new Throwable("key pressed").printStackTrace();
//        }
        if (shouldReturn) return true;

        // Select next text box if TAB was pressed
        if (input.key() == GLFW_KEY_TAB) {
            AtomicReference<WTextBox> firstTextBox = new AtomicReference<>(null);
            AtomicBoolean done = new AtomicBoolean(false);
            AtomicBoolean foundFocused = new AtomicBoolean(false);

            loopWidgets(root, wWidget -> {
                if (done.get() || !(wWidget instanceof WTextBox textBox)) return;

                if (foundFocused.get()) {
                    textBox.setFocused(true);
                    textBox.setCursorMax();

                    done.set(true);
                } else {
                    if (textBox.isFocused()) {
                        textBox.setFocused(false);
                        foundFocused.set(true);
                    }
                }

                if (firstTextBox.get() == null) firstTextBox.set(textBox);
            });

            if (!done.get() && firstTextBox.get() != null) {
                firstTextBox.get().setFocused(true);
                firstTextBox.get().setCursorMax();
            }

            return true;
        }

        boolean control = MacosUtil.IS_MACOS ? input.modifiers() == GLFW_MOD_SUPER : input.modifiers() == GLFW_MOD_CONTROL;

        return (control && input.key() == GLFW_KEY_C && toClipboard())
                || (control && input.key() == GLFW_KEY_V && fromClipboard());

        
    }



//    @Override
//    protected void initTable(){
//
//    }
}
