package com.dedokok.systems.modules.Feature;

import com.dedokok.events.world.TickEvent;
import com.dedokok.settings.*;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Module;
import com.dedokok.utils.misc.text.MessageToSend;
import com.mojang.authlib.GameProfile;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoLogin extends Module {

    public AutoLogin() {
        super(Categories.Feature, "AutoLogin", "AutoLogin",null);
        runInMainMenu=true;
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> triggerMessageSetting = sgGeneral.add(new StringSetting.Builder()
            .name("pattern")
            .description("RegEx pattern of trigger message (f.e. \'please login\'")
            .build()
    );

    private final Setting<Boolean> showLoginCommandSetting = sgGeneral.add(new BoolSetting.Builder()
            .name("show")
            .description("Hide or show login command (if you dont want to show password for someone)")
            .defaultValue(false)
            .build()
    );
    private final Setting<String> loginCommandSetting = sgGeneral.add(new StringSetting.Builder()
            .name("command")
            .description("Command you send to login")
            .visible(showLoginCommandSetting::get)
            .build()
    );

    public void onChatMessage(Component message, boolean overlay){// Шаблон для поиска email
        String regex = triggerMessageSetting.get();

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message.getString());
        boolean isFind = matcher.find();
        if(isFind) {
            if (mc != null) {
                if (mc.getConnection() != null) {
                    mc.getConnection().sendCommand(loginCommandSetting.get());
                }
            }
        }
    }


}
