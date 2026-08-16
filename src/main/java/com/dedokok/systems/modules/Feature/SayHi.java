package com.dedokok.systems.modules.Feature;
import com.dedokok.DedTools;
import com.dedokok.settings.IntSetting;
import com.dedokok.settings.Setting;
import com.dedokok.settings.SettingGroup;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Module;
import com.dedokok.utils.player.ChatUtils;
import net.minecraft.network.chat.Component;

public class SayHi extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> count = sgGeneral.add(new IntSetting.Builder()
            .name("count")
            .description("How many times send hi")
            .defaultValue(1)
            .build()
    );

    public SayHi() {
        super(Categories.Feature, "SayHi", "Say hi for multiple times");
    }

    @Override
    public void onActivate() {
        for(int i = 0; i<count.get();i++) {
            mc.player.sendSystemMessage(Component.literal("пока"));
        }
    }
}
