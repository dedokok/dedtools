package com.dedokok.systems.modules.Feature;
import com.dedokok.settings.BoolSetting;
import com.dedokok.settings.Setting;
import com.dedokok.settings.SettingGroup;
import com.dedokok.systems.modules.Module;

import com.dedokok.systems.modules.Categories;
import meteordevelopment.orbit.EventHandler;

public class CoreProtectTweaks extends Module{
    public CoreProtectTweaks() {
        super(Categories.Feature, "CoreProtectTweaks", "Tweaks for coreprotect plugin",null);
        runInMainMenu=true;
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();



    private final Setting<Boolean> enableRecordHistorySetting = sgGeneral.add(new BoolSetting.Builder()
            .name("enable-history-recording")
            .description("Record every coreprotect command and result")
            .defaultValue(false)
            .build()
    );


}
