package com.dedokok.systems.modules.Feature;
import com.dedokok.gui.utils.StarscriptTextBoxRenderer;
import com.dedokok.settings.*;
import com.dedokok.utils.classes.PlayerNote;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Module;

import java.util.List;

public class PlayersNoteBook extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public static int usernameWidth = 100;
    public static int descriptionWidth = 100;


    public final Setting<List<PlayerNote>> playerNotesSetting = sgGeneral.add(new PlayerNotesListSetting.Builder()
            .name("player-notes-list")
            .description("List of player notes")
            .renderer(StarscriptTextBoxRenderer.class)
            .build()
    );

    private final Setting<Integer> usernameTextBoxWidth = sgGeneral.add(new IntSetting.Builder()
            .name("username-textbox-width")
            .description("Width of username textbox")
            .defaultValue(100)
            .min(1)
            .max(2000)
            .sliderMin(100)
            .sliderMax(1000)
            .onChanged((value) -> {
                usernameWidth = value;
                reload();
            })
            .build()
    );
    private final Setting<Integer> descriptionTextBoxWidth = sgGeneral.add(new IntSetting.Builder()
            .name("description-textbox-width")
            .description("Width of description textbox")
            .defaultValue(100)
            .min(1)
            .max(2000)
            .sliderMin(100)
            .sliderMax(1000)
            .onChanged((value) -> {
                descriptionWidth = value;
                reload();
            })
            .build()
    );

    public String getPlayerDesc(String username) {
        for (PlayerNote playerNote : playerNotesSetting.get()) {
            if (playerNote.getUsername().equals(username)) {
                return playerNote.getDescription();
            }
        }
        return null;
    }

    public boolean needChange = false;


//    @EventHandler
//    public void onInitWidget(InitWidgetEvent event){
//
//        if(event.widget.getTheme().)
//
//        HashSet<PlayerNote>toDelete = new HashSet<>();
//        for(PlayerNote playerNote : playerNotesSetting.get()){
//            if(playerNote.fromTracker){
//                if(!Modules.get().get(PlayerTracker.class).playerListSetting.get().contains(playerNote)){
//                    toDelete.add(playerNote);
//                }
//            }
//        }
//        List<PlayerNote> newPlayerNotes = playerNotesSetting.get();
//        for(PlayerNote needDelete : toDelete){
//            newPlayerNotes.remove(needDelete);
//        }
//        playerNotesSetting.set(newPlayerNotes);
//    }

//    @EventHandler
//    public void onTick(TickEvent.Post event) {
//
//        if (mc != null && mc.gui.screen() == screen && !needChange) {
//            HashSet<PlayerNote> toDelete = new HashSet<>();
//            for (PlayerNote playerNote : playerNotesSetting.get()) {
//                if (playerNote.fromTracker) {
//                    if (!Modules.get().get(PlayerTracker.class).playerListSetting.get().contains(playerNote)) {
//                        toDelete.add(playerNote);
//                    }
//                }
//            }
//            List<PlayerNote> newPlayerNotes = playerNotesSetting.get();
//            for (PlayerNote needDelete : toDelete) {
//                newPlayerNotes.remove(needDelete);
//
//            }
//            playerNotesSetting.set(newPlayerNotes);
//        }
//        else{
//            needChange = true;
//        }
//    }





    public PlayersNoteBook() {
        super(Categories.Feature, "PlayersNoteBook", "Make notes about players",null);
        runInMainMenu = true;
    }


}
