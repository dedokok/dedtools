/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.gui.screens.settings;

import com.dedokok.gui.GuiTheme;
import com.dedokok.gui.WindowScreen;
import com.dedokok.gui.utils.Cell;
import com.dedokok.gui.widgets.WLabel;
import com.dedokok.gui.widgets.WWidget;
import com.dedokok.gui.widgets.containers.WTable;
import com.dedokok.gui.widgets.containers.WVerticalList;
import com.dedokok.gui.widgets.input.WTextBox;
import com.dedokok.gui.widgets.pressable.WButton;
import com.dedokok.systems.config.Config;
import com.dedokok.systems.modules.Feature.CoreProtectTweaks;
import com.dedokok.utils.RegExUtils;
import com.dedokok.utils.classes.CoreProtect.MessagesTypes;
import com.dedokok.utils.classes.CoreProtect.RowFromRecordClass;
import com.dedokok.utils.classes.Vein;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dedokok.DedTools.mc;
import static com.dedokok.systems.modules.Feature.CoreProtectTweaks.connection;
import static com.dedokok.systems.modules.Feature.CoreProtectTweaks.convertRowsToESP;

public class CoreProtectHistoryRowScreen extends WindowScreen {

    private ArrayList<RowFromRecordClass>rows = new ArrayList<>();

    private WVerticalList list;
    private final WTextBox filter;

    private String filterText = "";

    int history_id;
    protected WTable table;

    public CoreProtectHistoryRowScreen(GuiTheme theme, int id) {
        super(theme, "Select row");
        history_id = id;
        rows = getHistoryRows(history_id);
//        // Filter
        filter = super.add(theme.textBox("")).minWidth(400).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();

            list.clear();
            initWidgets();
        };

        list = super.add(theme.verticalList()).expandX().widget();

    }

    @Override
    public <W extends WWidget> Cell<W> add(W widget) {
        return list.add(widget);
    }


    boolean afterReload = false;
    @Override
    public void initWidgets() {
        if(afterReload){
            int a = 0;
            a++;
            afterReload=false;
        }
        // Filter
//        WTextBox filter = add(theme.textBox("")).minWidth(400).expandX().widget();
//        filter.setFocused(true);
//
//        filter.action = () -> {
//            filterText = filter.get().trim();
//            table.clear();
//            initTable();
//        };

        table = add(theme.table()).expandX().widget();

        initTable();

    }



    protected void initTable() {
        WTable left = abc(true);
    }


    protected void invalidateTable() {
        table.clear();
        initTable();
    }

    private WTable abc(boolean isLeft) {
        // Create
        Cell<WTable> cell = this.table.add(theme.table()).top();
        if (Config.get().syncListSettingWidths.get()) cell.group("sync-width");
        WTable table = cell.widget();

//        // Sort
//        Predicate<T> predicate = isLeft
//                ? value -> this.includeValue(value)
//                : this::includeValue;

        //Iterable<T> sorted = SortingHelper.sort(iterable, predicate, this::getValueNames, filterText);

        final int[] rowIdx = {0};
        //if(rows.is) {
            rows.forEach(t -> {
                //WWidget widget = getValueWidget(t);
                //if(widget!=null) {
                if(!RegExUtils.isFind(t.message,filterText)){
                    return;
                }
                WTextBox textBox_message = table.add(theme.textBox(t.message)).minWidth(500).widget();
                textBox_message.enabled = false;

                if(t.x!=null && t.world!=null) {
                    WTextBox textBox_cords = table.add(theme.textBox(t.x + " "+t.y+ " "+t.z + " "+t.world)).minWidth(300).expandX().widget();
                    textBox_cords.enabled = false;

                    WButton button = table.add(theme.button("tp")).minWidth(50).expandX().widget();
                    button.action = () -> {
                        if(mc!=null && mc.getConnection()!=null){
                            mc.getConnection().sendCommand("co tp "+t.world + " "+t.x+" "+t.y+" "+t.z);
                            convertRowsToESP(rows);
                            CoreProtectTweaks.ticksAfterTp=0;
                        }
                    };
                }

                SimpleDateFormat sdfEST = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                sdfEST.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
                String date = sdfEST.format(t.time*1000);
                WLabel label = table.add(theme.label(date)).minWidth(100).expandX().widget();
                table.row();
                //}
            });
        //}


        if (!table.cells.isEmpty()){
            cell.expandX();
        }

        return table;
    }


    protected String[] getValueNames(Vein value) {
        return new String[]{
                value.rows.getFirst().getUser(),
                //BuiltInRegistries.BLOCK.getKey(BuiltInRegistries.BLOCK.getValue(Identifier.parse(value.getBlock()))).toString(),
        };
    }




    public ArrayList<RowFromRecordClass> getHistoryRows(int id){
        //String sql = "UPDATE jails SET spawn_key = ?, jail_name = ? WHERE id = ?";
        ArrayList<RowFromRecordClass> list = new ArrayList<>();
        String sql = "SELECT * FROM rows WHERE history_id = ? ORDER BY id DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)){
            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();;

            while(rs.next()) {
                RowFromRecordClass row = new RowFromRecordClass();
               // row.amount = rs.getInt("amount");
                row.type =MessagesTypes.valueOf(rs.getString("type"));
                row.message = rs.getString("message");
                row.username =  rs.getString("username");
                row.time = rs.getLong("time");
                row.block = rs.getString("block");
                row.item =  rs.getString("item");
                row.entity = rs.getString("entity");
                row.newUsername =  rs.getString("new_username");
                row.text =  rs.getString("text");
                row.clicked = rs.getString("clicked");
                row.amount =  rs.getInt("amount");
                row.x = rs.getInt("x");
                row.y = rs.getInt("y");
                row.z =  rs.getInt("z");
                row.world = rs.getString("world");
                row.pageNow = rs.getInt("page_now");
                row.pageMax =  rs.getInt("page_now");
                list.add(row);
            }
        } catch (SQLException e) {
            System.out.println("Не удалось получить записи");
            return null;
        }
        return list;
    }


    public boolean isFind(String pattern_str,String value){
        try {
            String regex = pattern_str;
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(value);
            return matcher.find();
        }
        catch(Exception _){

        }
        return false;
    }

}
