package com.dedokok.systems.modules.Feature;
import com.dedokok.settings.IntSetting;
import com.dedokok.settings.Setting;
import com.dedokok.settings.SettingGroup;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Module;
import net.minecraft.network.chat.Component;

public class ShowRepairCost extends Module {
    public static boolean isActiveBool;

    public ShowRepairCost() {
        super(Categories.Feature, "ShowRepairCost", "Show how many xp levels you need to do smth with item in anvil",null);
    }

    @Override
    public void onActivate() {
        isActiveBool = true;
    }

    @Override
    public void onDeactivate() {
        isActiveBool = false;
    }
}
