package com.dedokok.systems.modules.Feature.MessageAnnounce;

import com.dedokok.gui.WidgetScreen;
import com.dedokok.settings.*;
import com.dedokok.utils.RegExUtils;
import com.dedokok.utils.misc.ISerializable;
import com.dedokok.utils.render.color.SettingColor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import java.util.Optional;

import static com.dedokok.DedTools.mc;

public class MessageAnnouncerBlock implements ISerializable<MessageAnnouncerBlock> {
    public final Settings settings = new Settings();
    public static final String type = "AutoReceive";
    public WidgetScreen screen = null;

    public MessageAnnouncerBlock() {}


    protected final SettingGroup sgGeneral = settings.createGroup("Announce");


    public final Setting<String> messageTextSetting = sgGeneral.add(new StringSetting.Builder()
            .name("message-get")
            .description("On what text should receive (regex)")
            .defaultValue("(.+): ?")
            .build()
    );

    public final Setting<Boolean> enableSoundAnnounceSetting = sgGeneral.add(new BoolSetting.Builder()
            .name("enable-sound")
            .description("Will be sound played on trigger")
            .defaultValue(true)
            .build()
    );

    public final Setting<String> soundPathSetting = sgGeneral.add(new StringSetting.Builder()
            .name("sound-path")
            .description("Path of sound will be played")
            .defaultValue("minecraft:block.anvil.land")
            .visible(enableSoundAnnounceSetting::get)
            .build()
    );

    public final Setting<Boolean> enableTitleSetting = sgGeneral.add(new BoolSetting.Builder()
            .name("enable-title")
            .description("Enable title on screen")
            .defaultValue(true)
            .build()
    );

    public final Setting<String> titleTextSetting = sgGeneral.add(new StringSetting.Builder()
            .name("title-text")
            .description("Text of title on screen")
            .defaultValue("Question")
            .visible(enableTitleSetting::get)
            .build()
    );
    public final Setting<SettingColor> titleColorSetting = sgGeneral.add(new ColorSetting.Builder()
            .name("title-color")
            .description("Color of title")
            .defaultValue(new SettingColor(0, 255, 0))
            .visible(enableTitleSetting::get)
            .build()
    );




    public void doMessage(String message) {
        if(RegExUtils.isFind(message,messageTextSetting.get())){
            if(enableSoundAnnounceSetting.get()) {
                String soundString = soundPathSetting.get();
                Identifier soundId = Identifier.parse(soundString);
                Optional<SoundEvent> soundEvent = BuiltInRegistries.SOUND_EVENT.getOptional(soundId);
                if (soundEvent.isPresent()) {
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(soundEvent.get(), 1.0F));
                }
            }


            if(enableTitleSetting.get()) {
                mc.gui.hud.setTitle(Component.literal(titleTextSetting.get()).withColor(titleColorSetting.get().toTextColor()));
            }

        }
    }



    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.put("settings", settings.toTag());

        return tag;
    }

    public void reload(){
        if(screen==null)return;
        screen.reload();
    }




    @Override
    public MessageAnnouncerBlock fromTag(CompoundTag tag) {
        CompoundTag settingsTag = (CompoundTag) tag.get("settings");
        if (settingsTag != null) settings.fromTag(settingsTag);

        return this;
    }

    public String getTypeName() {
        return type;
    }

    public MessageAnnouncerBlock copy() {
        return new MessageAnnouncerBlock().fromTag(toTag());
    }






}
