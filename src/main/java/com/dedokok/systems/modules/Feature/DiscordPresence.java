/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.systems.modules.Feature;

//Created by squidoodly

import dev.firstdark.rpc.DiscordRpc;
import dev.firstdark.rpc.enums.ActivityType;
import dev.firstdark.rpc.enums.ErrorCode;
import dev.firstdark.rpc.handlers.RPCEventHandler;
import dev.firstdark.rpc.models.DiscordRichPresence;
import dev.firstdark.rpc.models.User;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
//import meteordevelopment.discordipc.DiscordIPC;
//import meteordevelopment.discordipc.RichPresence;
import com.dedokok.DedTools;
import com.dedokok.events.game.OpenScreenEvent;
import com.dedokok.events.world.TickEvent;
import com.dedokok.gui.GuiTheme;
import com.dedokok.gui.WidgetScreen;
import com.dedokok.gui.utils.StarscriptTextBoxRenderer;
import com.dedokok.gui.widgets.WWidget;
import com.dedokok.gui.widgets.pressable.WButton;
import com.dedokok.settings.*;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Module;
import com.dedokok.utils.Utils;
import com.dedokok.utils.misc.MeteorStarscript;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.*;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.Util;
import org.meteordev.starscript.Script;

import java.util.ArrayList;
import java.util.List;

public class DiscordPresence extends Module {
    public enum SelectMode {
        Random,
        Sequential
    }



    private final SettingGroup options = settings.createGroup("Optiongs");
    private final SettingGroup sgLine1 = settings.createGroup("Line 1");
    private final SettingGroup sgLine2 = settings.createGroup("Line 2");


    //options
    private final Setting<String> appIDString = options.add(new StringSetting.Builder()
            .name("applicationID")
            .description("Your discord Application ID.")
            .defaultValue("1538291145426075768")
            .onChanged(_ -> recompileLine1())
            .renderer(StarscriptTextBoxRenderer.class)
            .build()
    );

    private final Setting<Boolean> enableButtonOneOption = options.add(new BoolSetting.Builder()
            .name("enable-button-one")
            .description("Enable your URL button 1.")
            .defaultValue(true)
            .onChanged(_ -> recompileLine1())
            .build()
    );
    private final Setting<String> buttonOneURLOption = options.add(new StringSetting.Builder()
            .name("button-one-url")
            .description("URL you can open with button click.")
            .defaultValue("https://t.me/dedushka_11")
            .onChanged(_ -> recompileLine1())
            .renderer(StarscriptTextBoxRenderer.class)
            .visible(enableButtonOneOption::get)
            .build()
    );
    private final Setting<String> buttonOneNameOption = options.add(new StringSetting.Builder()
            .name("button-one-name")
            .description("Button 1 name. Text on button in presence.")
            .defaultValue("My telegram")
            .onChanged(_ -> recompileLine1())
            .renderer(StarscriptTextBoxRenderer.class)
            .visible(enableButtonOneOption::get)
            .build()
    );

    private final Setting<Boolean> enableButtonTwoOption = options.add(new BoolSetting.Builder()
            .name("enable-button-two")
            .description("Enable your second URL button.")
            .defaultValue(true)
            .onChanged(_ -> recompileLine1())
            .build()
    );
    private final Setting<String> buttonTwoURLOption = options.add(new StringSetting.Builder()
            .name("button-two-url")
            .description("URL you can open with button click.")
            .defaultValue("https://t.me/dedushka_11")
            .onChanged(_ -> recompileLine1())
            .renderer(StarscriptTextBoxRenderer.class)
            .visible(enableButtonTwoOption::get)
            .build()
    );
    private final Setting<String> buttonTwoNameOption = options.add(new StringSetting.Builder()
            .name("button-two-name")
            .description("Button 2 name. Text on button in presence.")
            .defaultValue("My telegram")
            .onChanged(_ -> recompileLine1())
            .renderer(StarscriptTextBoxRenderer.class)
            .visible(enableButtonTwoOption::get)

            .build()
    );




    // Line 1

    private final Setting<List<String>> line1Strings = sgLine1.add(new StringListSetting.Builder()
        .name("line-1-messages")
        .description("Messages used for the first line.")
        .defaultValue("Тут самые сочные мужики")
        .onChanged(_ -> recompileLine1())
        .renderer(StarscriptTextBoxRenderer.class)
        .build()
    );

    private final Setting<Integer> line1UpdateDelay = sgLine1.add(new IntSetting.Builder()
        .name("line-1-update-delay")
        .description("How fast to update the first line in ticks.")
        .defaultValue(200)
        .min(10)
        .sliderRange(10, 200)
        .build()
    );

    private final Setting<SelectMode> line1SelectMode = sgLine1.add(new EnumSetting.Builder<SelectMode>()
        .name("line-1-select-mode")
        .description("How to select messages for the first line.")
        .defaultValue(SelectMode.Sequential)
        .build()
    );

    // Line 2

    private final Setting<List<String>> line2Strings = sgLine2.add(new StringListSetting.Builder()
        .name("line-2-messages")
        .description("Messages used for the second line.")
        .defaultValue("{server}, онлайн {server.player_count}")
        .onChanged(_ -> recompileLine2())
        .renderer(StarscriptTextBoxRenderer.class)
        .build()
    );

    private final Setting<Integer> line2UpdateDelay = sgLine2.add(new IntSetting.Builder()
        .name("line-2-update-delay")
        .description("How fast to update the second line in ticks.")
        .defaultValue(60)
        .min(10)
        .sliderRange(10, 200)
        .build()
    );

    private final Setting<SelectMode> line2SelectMode = sgLine2.add(new EnumSetting.Builder<SelectMode>()
        .name("line-2-select-mode")
        .description("How to select messages for the second line.")
        .defaultValue(SelectMode.Sequential)
        .build()
    );

//    private static final RichPresence rpc = new RichPresence();
private static final DiscordRpc rpc = new DiscordRpc();

    private SmallImage currentSmallImage;
    private int ticks;
    private boolean forceUpdate, lastWasInMainMenu;

    private final List<Script> line1Scripts = new ArrayList<>();
    private int line1Ticks, line1I;

    private final List<Script> line2Scripts = new ArrayList<>();
    private int line2Ticks, line2I;

    private long startTimestamp = 0;

    String state = "state";
    String details = "details";
    String largeImageKey = "";
    static String smallImageKey = "";
    public static final Object2ObjectLinkedOpenHashMap<String, String> customStates = new Object2ObjectLinkedOpenHashMap<>();

    static {
        registerCustomState("com.terraformersmc.modmenu.gui", "Browsing mods");
        registerCustomState("me.jellysquid.mods.sodium.client", "Changing options");
    }

    public DiscordPresence() {
        super(Categories.Feature, "discord-presence", "Displays DedTools as your presence on Discord.",null);

        runInMainMenu = true;
    }



    RPCEventHandler handler = new RPCEventHandler() {
        @Override
        public void ready(User user) {
            System.out.println("Ready");
            //enableDiscordPresence();
            System.out.println(user.getUsername());
        }

        @Override
        public void disconnected(ErrorCode errorCode, String message) {
            System.out.println("Disconnected " + errorCode + " - " + message);
        }

        @Override
        public void errored(ErrorCode errorCode, String message) {
            System.out.println("Errored " + errorCode + " - " + message);
        }
    };


    /**
     * Registers a custom state to be used when the current screen is a class in the specified package.
     */
    public static void registerCustomState(String packageName, String state) {
        customStates.put(packageName, state);
    }

    /**
     * The package name must match exactly to the one provided through {@link #registerCustomState(String, String)}.
     */
    public static void unregisterCustomState(String packageName) {
        customStates.remove(packageName);
    }



    public void enableDiscordPresence(){
        ticks=0;
        try{
            startTimestamp = System.currentTimeMillis() / 1000L;
            String applicationID = appIDString.get();
            rpc.init(applicationID, handler, false);
        }
        catch(Exception e){
            System.out.println("no");
        }


        String largeText = "%s %s".formatted(DedTools.NAME, DedTools.VERSION);
        largeText += " Build: #42";
        largeImageKey="dedushka_1_1";

        currentSmallImage = SmallImage.Snail;

        recompileLine1();
        recompileLine2();

        ticks = 0;
        line1Ticks = 0;
        line2Ticks = 0;
        lastWasInMainMenu = false;

        line1I = 0;
        line2I = 0;
    }


    @Override
    public void onActivate() {
        enableDiscordPresence();
    }

    @Override
    public void onDeactivate() {
//        DiscordIPC.stop();
        rpc.shutdown();
    }

    private void recompile(List<String> messages, List<Script> scripts) {
        scripts.clear();

        for (String message : messages) {
            Script script = MeteorStarscript.compile(message);
            if (script != null) scripts.add(script);
        }

        forceUpdate = true;
    }

    private void recompileLine1() {
        recompile(line1Strings.get(), line1Scripts);
    }

    private void recompileLine2() {
        recompile(line2Strings.get(), line2Scripts);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        boolean update = false;
        //System.out.println("123");

        // Image
        if (ticks >= 200 || forceUpdate) {
            currentSmallImage = currentSmallImage.next();
            currentSmallImage.apply();
            update = true;

            ticks = 0;
        } else ticks++;

        if (Utils.canUpdate()) {
            // Line 1
            if (line1Ticks >= line1UpdateDelay.get() || forceUpdate) {
                if (!line1Scripts.isEmpty()) {
                    int i = Utils.random(0, line1Scripts.size());
                    if (line1SelectMode.get() == SelectMode.Sequential) {
                        if (line1I >= line1Scripts.size()) line1I = 0;
                        i = line1I++;
                    }

                    String message = MeteorStarscript.run(line1Scripts.get(i));
                    if (message != null) details=message;
                }
                update = true;

                line1Ticks = 0;
            } else line1Ticks++;

            // Line 2
            if (line2Ticks >= line2UpdateDelay.get() || forceUpdate) {
                if (!line2Scripts.isEmpty()) {
                    int i = Utils.random(0, line2Scripts.size());
                    if (line2SelectMode.get() == SelectMode.Sequential) {
                        if (line2I >= line2Scripts.size()) line2I = 0;
                        i = line2I++;
                    }

                    String message = MeteorStarscript.run(line2Scripts.get(i));
                    if (message != null) state=message;
                }
                update = true;

                line2Ticks = 0;
            } else line2Ticks++;
        } else {
            if (!lastWasInMainMenu) {
                details = (DedTools.NAME + " " + DedTools.VERSION + " #42");

                if (mc.gui.screen() instanceof TitleScreen) state = ("Looking at title screen");
                else if (mc.gui.screen() instanceof SelectWorldScreen) state = ("Selecting world");
                else if (mc.gui.screen() instanceof CreateWorldScreen || mc.gui.screen() instanceof AbstractGameRulesScreen)
                    state = ("Creating world");
                else if (mc.gui.screen() instanceof EditWorldScreen) state = ("Editing world");
                else if (mc.gui.screen() instanceof LevelLoadingScreen) state = ("Loading world");
                else if (mc.gui.screen() instanceof JoinMultiplayerScreen) state = ("Selecting server");
                else if (mc.gui.screen() instanceof ManageServerScreen) state = ("Adding server");
                else if (mc.gui.screen() instanceof ConnectScreen || mc.gui.screen() instanceof DirectJoinServerScreen)
                    state = ("Connecting to server");
                else if (mc.gui.screen() instanceof WidgetScreen) state = ("Browsing Meteor's GUI");
                else if (mc.gui.screen() instanceof OptionsScreen || mc.gui.screen() instanceof SkinCustomizationScreen || mc.gui.screen() instanceof SoundOptionsScreen || mc.gui.screen() instanceof VideoSettingsScreen || mc.gui.screen() instanceof ControlsScreen || mc.gui.screen() instanceof LanguageSelectScreen || mc.gui.screen() instanceof ChatOptionsScreen || mc.gui.screen() instanceof PackSelectionScreen || mc.gui.screen() instanceof AccessibilityOptionsScreen)
                    state = ("Changing options");
                else if (mc.gui.screen() instanceof WinScreen) state = ("Reading credits");
                else if (mc.gui.screen() instanceof RealmsScreen) state = ("Browsing Realms");
                else {
                    boolean setState = false;
                    if (mc.gui.screen() != null) {
                        String className = mc.gui.screen().getClass().getName();
                        for (var entry : customStates.object2ObjectEntrySet()) {
                            if (className.startsWith(entry.getKey())) {
                                state = (entry.getValue());
                                setState = true;
                                break;
                            }
                        }
                    }
                    if (!setState) state = ("In main menu");
                }

                update = true;
            }
        }

        // Update
        //if (update) DiscordIPC.setActivity(rpc);

        if (update) {
            updateDiscordPresence();
        }

        forceUpdate = false;
        lastWasInMainMenu = !Utils.canUpdate();
    }

    public void updateDiscordPresence(){
        Boolean enableButtonOne = enableButtonOneOption.get();
        Boolean enableButtonTwo = enableButtonTwoOption.get();


        DiscordRichPresence.DiscordRichPresenceBuilder builder = DiscordRichPresence.builder()
                .details(details)
                .state(state)
                .largeImageKey(largeImageKey)
                .smallImageKey(smallImageKey)
                .activityType(ActivityType.PLAYING)
                .startTimestamp(startTimestamp);

        if(enableButtonOne){
            String buttonOneName = buttonOneNameOption.get();
            String buttonOneURL = buttonOneURLOption.get();
            builder.button(DiscordRichPresence.RPCButton.of(buttonOneName, buttonOneURL));
        }
        if(enableButtonTwo){
            String buttonTwoName = buttonTwoNameOption.get();
            String buttonTwoURL = buttonTwoURLOption.get();
            builder.button(DiscordRichPresence.RPCButton.of(buttonTwoName, buttonTwoURL));
        }
        rpc.updatePresence(builder.build());
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!Utils.canUpdate()) lastWasInMainMenu = false;
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WButton help = theme.button("Open documentation.");
        help.action = () -> Util.getPlatform().openUri("https://github.com/MeteorDevelopment/meteor-client/wiki/Starscript");

        return help;
    }



    private enum SmallImage {
        MineGame("minegame", "MineGame159"),
        Snail("seasnail", "seasnail8169");

        private final String key, text;

        SmallImage(String key, String text) {
            this.key = key;
            this.text = text;
        }

        void apply() {
            //rpc.setSmallImage(key, text);
            smallImageKey = key;

        }

        SmallImage next() {
            if (this == MineGame) return Snail;
            return MineGame;
        }
    }

    public void onStopEvent(Minecraft client){
        rpc.shutdown();
    }
}
