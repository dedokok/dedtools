package com.dedokok.systems.modules.Feature;
import com.dedokok.DedTools;
import com.dedokok.events.game.GameJoinedEvent;
import com.dedokok.events.game.OpenScreenEvent;
import com.dedokok.events.world.TickEvent;
import com.dedokok.gui.utils.StarscriptTextBoxRenderer;
import com.dedokok.gui.widgets.pressable.WButton;
import com.dedokok.settings.*;
import com.dedokok.settings.classes.PlayerNote;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Module;
import com.dedokok.systems.modules.Modules;
import com.dedokok.utils.other.TelegramNotifier;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerTracker extends Module {

    private final SettingGroup hudPosGroup = settings.createGroup("Hud Customize");
    private final SettingGroup telegramGroup = settings.createGroup("Telegram");
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup massAddGroup = settings.createGroup("Mass add");

    public static final int LINE_HEIGHT = 11;
    public static final int PADDING = 4;

    public static List<PlayerNote>onlinePlayers = new ArrayList<>();

    public static boolean isEnabled = false;

    private static final Identifier LAYER_ID =
            Identifier.fromNamespaceAndPath(DedTools.MOD_ID, "player_list_hud");


    private final Setting<Boolean> isTelegramNotifySetting = telegramGroup.add(new BoolSetting.Builder()
            .name("telegram-notify")
            .description("Enable telegram bot notifies")
            .defaultValue(false)
            .build()
    );
    private final Setting<String> botTokenSetting = telegramGroup.add(new StringSetting.Builder()
            .name("bot-token")
            .description("Token of your bot")
            .renderer(StarscriptTextBoxRenderer.class)
            .visible(isTelegramNotifySetting::get)
            .build()
    );
    private final Setting<String> chatIdSetting = telegramGroup.add(new StringSetting.Builder()
            .name("chat-id")
            .description("Id of chat where bot send notify")
            .renderer(StarscriptTextBoxRenderer.class)
            .visible(isTelegramNotifySetting::get)
            .build()
    );
    private final ButtonSetting testTGMessageSetting = telegramGroup.add(new ButtonSetting.Builder()
            .defaultValue(screen)
            .width(50)
            .onChanged(_ -> {
                TelegramNotifier.sendAsync("test message",botTokenSetting.get(),chatIdSetting.get());
            })
            .name("send-test-message")
            .visible(isTelegramNotifySetting::get)
            .build()
    );



    private final Setting<String> massAddPlayersString = massAddGroup.add(new StringSetting.Builder()
            .name("mass-add-player-string")
            .description("Type here all players you want to add")
            .renderer(StarscriptTextBoxRenderer.class)
            .build()
    );
    private final Setting<String> massAddPlayersSplitString = massAddGroup.add(new StringSetting.Builder()
            .name("split-chars-between-players")
            .description("Chars between players in list. F.e. if you have 'p1,p2' type ,")
            .renderer(StarscriptTextBoxRenderer.class)
            .build()
    );
    private final ButtonSetting buttonSetting = massAddGroup.add(new ButtonSetting.Builder()
            .defaultValue(screen)
            .width(50)
            .onChanged(_ -> {
                addMassPlayers();
            })
            .name("Confirm mass add")
            .build()
    );

//    private final Setting<List<String>> playerListSetting = sgGeneral.add(new StringListSetting.Builder()
//            .name("player-list")
//            .description("List of tracked players")
//            .renderer(StarscriptTextBoxRenderer.class)
//            .build()
//    );
    public final Setting<List<PlayerNote>> playerListSetting = sgGeneral.add(new PlayerNotesListSetting.Builder()
            .name("player-list")
            .description("List of tracked players")
            .renderer(StarscriptTextBoxRenderer.class)
                .onChanged(value->{
                    ArrayList<String>names = new ArrayList<>();
                    for(PlayerNote playerNote : value){
                        names.add(playerNote.username);
                    }
                    for(PlayerNote playerNote : Modules.get().get(PlayersNoteBook.class).playerNotesSetting.get()){
                        if(!names.contains(playerNote.username)){
                            value.add(playerNote);
                        }
                    }
                    Modules.get().get(PlayersNoteBook.class).playerNotesSetting.set(value);
                })
                .isTracker(true)
            .build()
    );



    public PlayerTracker() {
        super(Categories.Feature, "PlayerTracker", "Track players on server",null);
        runInMainMenu = true;
    }




    public void addMassPlayers(){
        if(massAddPlayersString.get().isEmpty() || massAddPlayersSplitString.get().isEmpty()){return;}
        List<PlayerNote>listNow = playerListSetting.get();

        List<String>newPlayers = List.of(massAddPlayersString.get().split(massAddPlayersSplitString.get()));
        for(String string : newPlayers){
            listNow.add(new PlayerNote(string,null,true));
        }
        playerListSetting.set(listNow);


        massAddPlayersString.set("");

        reload();

    }

    private static final Set<String> previouslyOnline = new HashSet<>();
    private static boolean baselineCaptured = false;
    private boolean isRegistered=false;

    int ticks = 0;

    @EventHandler
    public void onTick(TickEvent.Post event){
        ticks++;
        if(ticks%2!=0)return;
        ticks=0;
        if (mc.player == null) {

            // Not on a server (title screen / singleplayer pause / disconnected).
            if (!previouslyOnline.isEmpty()) {
                previouslyOnline.clear();
            }
            baselineCaptured = false;
            return;
        }

        Set<String> currentOnline = new HashSet<>();
        mc.player.connection.getOnlinePlayers().forEach(player -> {
            String name = player.getProfile().name();
            currentOnline.add(name);
        });



        onlinePlayers.clear();
        for (PlayerNote note : playerListSetting.get()) {
            String name = note.username;
            boolean online = isOnline(name);
            if (!online) continue;
            int color = online ? 0xFF55FF55 : 0xFFFF5555;
            onlinePlayers.add(note);
        }

        if (!baselineCaptured) {
            // We just connected. Don't fire notifications for people who were
            // already on the server before we opened the game - only for
            // genuinely new joins from this point on.
            previouslyOnline.clear();
            previouslyOnline.addAll(currentOnline);
            baselineCaptured = true;
            return;
        }

        for (String name : currentOnline) {
            if (!previouslyOnline.contains(name)) {
                onPlayerJoined(name);
            }
        }

        previouslyOnline.clear();
        previouslyOnline.addAll(currentOnline);
    }

    public boolean isTracked(String name){
        return playerListSetting.get().contains(name);
    }

    private void onPlayerJoined(String name) {
        if (!isTracked(name)) {
            return;
        }


        if (mc.player != null) {
            mc.player.sendSystemMessage(
                    Component.literal( name).withColor(TextColor.GREEN).append(Component.literal( " зашёл на сервер!").withColor(TextColor.WHITE))
            );
        }

        if (isTelegramNotifySetting.get()) {
            String botToken = botTokenSetting.get();
            String chatId = chatIdSetting.get();
            String message = name;
            PlayerNote note = getPlayerNote(name);
            if(note!=null){
                message=message+" - "+note.description;
            }
            message = message + " зашёл на сервер (" + serverAddress(mc) + ")";
            TelegramNotifier.sendAsync(message ,botToken,chatId);
        }
    }

    private PlayerNote getPlayerNote(String username){
        for(PlayerNote note : playerListSetting.get()){
            if(note.username.equals(username)){
                return note;
            }
        }
        return null;
    }

    private  String serverAddress(Minecraft client) {
        if (client.player != null) {
            return client.player.connection.getServerData().ip;
        }
        return "неизвестный сервер";
    }

    public boolean isOnline(String name) {
        for (String online : previouslyOnline) {
            if (online.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

}
