/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.gui.widgets;

import com.dedokok.gui.GuiHitTest;
import com.dedokok.gui.WindowScreen;
import com.dedokok.gui.renderer.GuiRenderer;
import com.dedokok.gui.screens.settings.VeinsListSettingScreen;
import com.dedokok.gui.widgets.containers.WContainer;
import com.dedokok.gui.widgets.containers.WTable;
import com.dedokok.gui.widgets.pressable.WButton;
import com.dedokok.systems.modules.Feature.BlockBreakFinder;
import com.dedokok.systems.modules.Modules;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

import static com.dedokok.DedTools.mc;

public abstract class WVeinChoose extends WContainer implements WRoot {
    private boolean valid;

    protected BlockBreakFinder.Vein vein;
    public VeinsListSettingScreen screen;

    public WVeinChoose(VeinsListSettingScreen screen, BlockBreakFinder.Vein vein) {
        this.screen=screen;
        this.vein=vein;
    }

    @Override
    public void init() {


        WTable table = add(theme.table()).pad(4).widget();
        table.layer=layer+1;
        // строка 1
//        table.add(theme.label(text));
//        table.row();

        // строка 2
        table.add(theme.label("Скрыть жилы игрока "+vein.rows.getFirst().getUser()));
        WButton button_1 = theme.button("Скрыть");
        button_1.layer=table.layer+1;
        button_1.action = () -> {
            if(button_1.isOver(mc.mouseHandler.xpos(),mc.mouseHandler.ypos())) {
                BlockBreakFinder.removePlayer(vein.rows.getFirst().getUser());
                System.out.println("veinchoose screen: "+screen.getClass().getSimpleName());
                List<String>newUsers = Modules.get().get(BlockBreakFinder.class).excludeEsersToLookupSetting.get();
                newUsers.add(vein.rows.getFirst().getUser());
                Modules.get().get(BlockBreakFinder.class).excludeEsersToLookupSetting.set(newUsers);
                Modules.get().get(BlockBreakFinder.class).screen.reload();
                screen.closeWidget();
                screen.reload();
                sendMessage("кнопка скрыть");
            }
            else{
                button_1.pressed=false;
            }
        };
        table.add(button_1).expandWidgetX();
        table.row();

        // строка 3
        table.add(theme.label("Удалить жилу "));
        WButton button_2 = theme.button("Удалить");
        button_2.layer=table.layer+1;
        button_2.action = () -> {
            if(button_2.isOver(mc.mouseHandler.xpos(),mc.mouseHandler.ypos())) {
                BlockBreakFinder.deleteVein(vein);
                screen.closeWidget();
                VeinsListSettingScreen.veinChooseUpdateScreen=true;
                sendMessage("Кнопка удалить");
            }
            else{
                button_2.pressed=false;
            }
        };
        table.add(button_2).expandWidgetX();
        table.row();


    }

    public void sendMessage(String text){
        if(mc!=null && mc.player!=null){
            mc.player.sendSystemMessage(Component.literal(text));
        }
    }

    @Override
    public void invalidate() {
        valid = false;
    }


    public double customX, customY;
    @Override
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!valid) {
            calculateSize();
            calculateWidgetPositions();

            valid = true;
        }

        return super.render(renderer, mouseX, mouseY, delta);
    }


}
