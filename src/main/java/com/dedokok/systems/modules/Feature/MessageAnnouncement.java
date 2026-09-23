package com.dedokok.systems.modules.Feature;

import com.dedokok.events.world.TickEvent;
import com.dedokok.gui.utils.StarscriptTextBoxRenderer;
import com.dedokok.settings.*;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Module;
import com.dedokok.utils.misc.text.MessageToSend;
import com.mojang.authlib.GameProfile;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageAnnouncement extends Module {

    public MessageAnnouncement() {
        super(Categories.Feature, "MessageAnnouncer", "Sound and title announcements for messages",null);
        runInMainMenu=true;
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();


    private final SettingGroup globalChat = settings.createGroup("Global chat");
    private final SettingGroup localChat = settings.createGroup("Local chat");
    private final SettingGroup directMessages = settings.createGroup("Direct messages");
    private final SettingGroup questions = settings.createGroup("Questions");
    private final Setting<Boolean> isGlobalChat= sgGeneral.add(new BoolSetting.Builder()
            .name("announce-global-chat")
            .description("Announce global chat messages")
            .defaultValue(false)
            .build()
    );
    private final Setting<Boolean> isLocalChat= sgGeneral.add(new BoolSetting.Builder()
            .name("announce-local-chat")
            .description("Announce local chat messages")
            .defaultValue(false)
            .build()
    );
    private final Setting<Boolean> isDirectMessages= sgGeneral.add(new BoolSetting.Builder()
            .name("announce-direct-messages")
            .description("Announce direct messages")
            .defaultValue(false)
            .build()
    );
    private final Setting<Boolean> isQuestionsChat= sgGeneral.add(new BoolSetting.Builder()
            .name("announce-questions")
            .description("Announce questions messages")
            .defaultValue(false)
            .build()
    );






    private final Setting<String> globalChatExampleSetting = globalChat.add(new StringSetting.Builder()
            .name("global-chat-message-get")
            .description("Global chat messages regular expression")
            .defaultValue("^栗")
            .visible(isGlobalChat::get)
            .build()
    );
    private final Setting<String> globalSoundPathSetting = globalChat.add(new StringSetting.Builder()
            .name("global-sound-path")
            .description("Path of the sound in minecraft mappings")
            .defaultValue("minecraft:block.anvil.land")
            .renderer(StarscriptTextBoxRenderer.class)
            .visible(isGlobalChat::get)
            .build()
    );
    private final Setting<Boolean> globalTitleAnnounce= globalChat.add(new BoolSetting.Builder()
            .name("title-announce")
            .description("Send title to screen")
            .defaultValue(false)
            .visible(isGlobalChat::get)
            .build()
    );
    private final Setting<String> globalTitleText = globalChat.add(new StringSetting.Builder()
            .name("global-chat-title-text")
            .description("Text of global chat titles")
            .defaultValue("Получил глобальное сообщение")
            .renderer(StarscriptTextBoxRenderer.class)
            .visible(()->globalTitleAnnounce.get() && isGlobalChat.get())
            .build()
    );




    private final Setting<String> localChatExampleSetting = localChat.add(new StringSetting.Builder()
            .name("local-chat-message-get")
            .description("Local chat messages regular expression")
            .defaultValue("^隆")
            .visible(isLocalChat::get)
            .build()
    );
    private final Setting<String> localSoundPathSetting = localChat.add(new StringSetting.Builder()
            .name("local-sound-path")
            .description("Path of the sound in minecraft mappings")
            .defaultValue("minecraft:block.anvil.land")
            .renderer(StarscriptTextBoxRenderer.class)
            .visible(isLocalChat::get)
            .build()
    );
    private final Setting<Boolean> localTitleAnnounce= localChat.add(new BoolSetting.Builder()
            .name("title-announce")
            .description("Send title to screen")
            .defaultValue(false)
            .visible(isLocalChat::get)
            .build()
    );
    private final Setting<String> localTitleText = localChat.add(new StringSetting.Builder()
            .name("local-chat-title-text")
            .description("Text of local chat titles")
            .defaultValue("Получил локальное сообщение")
            .renderer(StarscriptTextBoxRenderer.class)
            .visible(()->localTitleAnnounce.get() && isLocalChat.get())
            .build()
    );



    private final Setting<String> directMessageExampleSetting = directMessages.add(new StringSetting.Builder()
            .name("direct-message-get")
            .description("Direct messages regular expression")
            .defaultValue("^\\| .+ -> я: ")
            .visible(isDirectMessages::get)
            .build()
    );
    private final Setting<String> directSoundPathSetting = directMessages.add(new StringSetting.Builder()
            .name("direct-sound-path")
            .description("Path of the sound in minecraft mappings")
            .defaultValue("minecraft:block.anvil.land")
            .renderer(StarscriptTextBoxRenderer.class)
            .visible(isDirectMessages::get)
            .build()
    );
    private final Setting<Boolean> directTitleAnnounce= directMessages.add(new BoolSetting.Builder()
            .name("title-announce")
            .description("Send title to screen")
            .defaultValue(false)
            .visible(isDirectMessages::get)
            .build()
    );
    private final Setting<String> directTitleText = directMessages.add(new StringSetting.Builder()
            .name("direct-title-text")
            .description("Text of direct message titles")
            .defaultValue("Получил личное сообщение")
            .renderer(StarscriptTextBoxRenderer.class)
            .visible(()->directTitleAnnounce.get() && isDirectMessages.get())
            .build()
    );




    private final Setting<String> questionMessageExampleSetting = questions.add(new StringSetting.Builder()
            .name("question-message-get")
            .description("Question messages regular expression")
            .defaultValue("^栗.+\\?")
            .visible(isQuestionsChat::get)
            .build()
    );
    private final Setting<String> questionSoundPathSetting = questions.add(new StringSetting.Builder()
            .name("question-sound-path")
            .description("Path of the sound in minecraft mappings")
            .defaultValue("minecraft:block.anvil.land")
            .renderer(StarscriptTextBoxRenderer.class)
            .visible(isQuestionsChat::get)
            .build()
    );
    private final Setting<Boolean> questionTitleAnnounce= questions.add(new BoolSetting.Builder()
            .name("title-announce")
            .description("Send title to screen")
            .defaultValue(false)
            .visible(isQuestionsChat::get)
            .build()
    );
    private final Setting<String> questionTitleText = questions.add(new StringSetting.Builder()
            .name("question-title-text")
            .description("Text of question titles")
            .defaultValue("Получил вопрос")
            .renderer(StarscriptTextBoxRenderer.class)
            .visible(()->questionTitleAnnounce.get() && isQuestionsChat.get())
            .build()
    );






    //@EventHandler
    public void onGameMessage(Component message, boolean overlay){
        //if(sender !=null && mc!=null && mc.player !=null && sender.name().equals(mc.player.getName().getString()))return;
        //doMessage(message);
        //mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.SILVERFISH_DEATH,1.0F));
        //mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.,1.0F));
        if(!isActive())return;
        String message_string = message.getString();
        if(isGlobalChat.get()&& checkGlobalChat(message_string))makeAnnounce(0);
        else if(isLocalChat.get()&& checkLocalChat(message_string))makeAnnounce(1);
        else if(isDirectMessages.get()&& checkDirectChat(message_string))makeAnnounce(2);
        else if(isQuestionsChat.get() && checkQuestionChat(message_string))makeAnnounce(3);

    }



    public void makeAnnounce(int id) {

        String soundString = "no";
        switch(id) {
            case 0: {
                soundString=globalSoundPathSetting.get();
                if(globalTitleAnnounce.get()) {
                    mc.gui.hud.setTitle(Component.literal(globalTitleText.get()).withColor(TextColor.GREEN));
                }
                break;
            }
            case 1: {
                soundString=localSoundPathSetting.get();
                if(localTitleAnnounce.get()) {
                    mc.gui.hud.setTitle(Component.literal(localTitleText.get()).withColor(TextColor.GREEN));
                }
                break;
            }
            case 2: {
                soundString=directSoundPathSetting.get();
                if(directTitleAnnounce.get()) {
                    mc.gui.hud.setTitle(Component.literal(directTitleText.get()).withColor(TextColor.GREEN));
                }
                break;
            }
            case 3: {
                soundString=questionSoundPathSetting.get();
                if(questionTitleAnnounce.get()) {
                    mc.gui.hud.setTitle(Component.literal(questionTitleText.get()).withColor(TextColor.GREEN));
                }
                break;}
        }
        Identifier soundId = Identifier.parse(soundString); // Use new Identifier(soundString) for 1.20.6 and below

        Optional<SoundEvent> soundEvent = BuiltInRegistries.SOUND_EVENT.getOptional(soundId);

        if (soundEvent.isPresent()) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(soundEvent.get(), 1.0F));
        }
    }


    public boolean checkGlobalChat(String message){
        String regex = globalChatExampleSetting.get();

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        return matcher.find();
    }

    public boolean checkLocalChat(String message){
        String regex = localChatExampleSetting.get();

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        return matcher.find();
    }

    public boolean checkDirectChat(String message){
        String regex = directMessageExampleSetting.get();

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        return matcher.find();
    }

    public boolean checkQuestionChat(String message){
        String regex = questionMessageExampleSetting.get();

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        return matcher.find();
    }

}
