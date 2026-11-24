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
import com.dedokok.gui.widgets.pressable.WPressable;
import com.dedokok.renderer.Texture;
import com.dedokok.systems.config.Config;
import com.dedokok.systems.modules.Feature.CoreProtectTweaks;
import com.dedokok.utils.RegExUtils;
import com.dedokok.utils.classes.CoreProtect.HistoryRecordClass;
import com.dedokok.utils.classes.CoreProtect.MessagesTypes;
import com.dedokok.utils.classes.CoreProtect.RowFromRecordClass;
import com.dedokok.utils.classes.Vein;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dedokok.DedTools.mc;
import static com.dedokok.systems.modules.Feature.CoreProtectTweaks.connection;

public class CoreProtectCommandsHistoryScreen extends WindowScreen {
    private static Texture EMPTY_SPAWN_EGG_TEXTURE;

    private ArrayList<HistoryRecordClass>history = new ArrayList<>();

    private WVerticalList list;
    private final WTextBox filter;

    private String filterText = "";


    protected WTable table;

    public CoreProtectCommandsHistoryScreen(GuiTheme theme) {
        super(theme, "Select row");
        getHistories();
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
        if(history!=null) {
            history.forEach(t -> {
                if(!RegExUtils.isFind(t.command,filterText))return;
                //WWidget widget = getValueWidget(t);
                //if(widget!=null) {
                WLabel label_id = table.add(theme.label("" + t.id)).minWidth(60).expandX().widget();
                WTextBox textBox = table.add(theme.textBox(t.command)).minWidth(600).expandX().widget();
                textBox.enabled = false;
                WLabel label = table.add(theme.label("" + t.rowsAmount)).minWidth(60).expandX().widget();
                WButton button = table.add(theme.button("open")).minWidth(60).expandX().widget();
                button.action = () ->{
                    //if(screen==null)return;
                    CoreProtectHistoryRowScreen screen2 = new CoreProtectHistoryRowScreen(theme,t.id);
                    // screen2.onClosed(() -> );
                    mc.gui.setScreen(screen2);
                };

                WPressable button_remove = table.add(theme.minus()).expandCellX().right().widget();
                button_remove.action = () ->{
                    removeHistory(t);
                };
                //button.action = () -> buttonAction.accept(t);
//
//                rowIdx[0]++;
//                if (rowIdx[0] == 11) {
//                    table.row();
//                    rowIdx[0] = 0;
//                }
                table.row();
                //}
            });
        }


        if (!table.cells.isEmpty()){
            cell.expandX();
        }

        return table;
    }

    public void removeHistory(HistoryRecordClass record) {
        Thread.startVirtualThread(() -> {
            history.remove(record);
            String sql = "DELETE FROM rows WHERE history_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, record.id);
                pstmt.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
            sql = "DELETE FROM history WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, record.id);
                pstmt.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
            afterReload=true;
            invalidateTable();
        });

    }

    protected String[] getValueNames(Vein value) {
        return new String[]{
                value.rows.getFirst().getUser(),
                //BuiltInRegistries.BLOCK.getKey(BuiltInRegistries.BLOCK.getValue(Identifier.parse(value.getBlock()))).toString(),
        };
    }

//    protected boolean includeValue(Vein value) {
//
//        Predicate<Vein> filter = ((VeinsListSetting) setting).filter;
//        if (filter == null) return BuiltInRegistries.BLOCK.getValue(Identifier.parse(value.rows.getFirst().getBlock())) != Blocks.AIR;
//        return filter.test(value);
//    }


//    protected WWidget getValueWidget(HistoryRecordClass historyRow) {
//        //if(value.isRemoved)return null;
//        //Identifier itemId = Identifier.parse(value.rows.getFirst().getBlock());
//
//        //String tooltip = "";
//        //tooltip = tooltip+value.rows.getFirst().getBlock();
//        //tooltip = tooltip+"\n"+value.rows.getFirst().getUser();
//        //int[] coords = BlockBreakFinder.unpack(value.rows.getFirst().getCoords());
//        //tooltip = tooltip+"\n"+value.rows.getFirst().getWrld() + " "+ coords[0] + " "+coords[1]+" "+coords[2]+"\n";
//
//
//        //SimpleDateFormat sdfEST = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
//
//       // sdfEST.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
//       // String date = sdfEST.format(value.rows.getFirst().getTimestamp()*1000);
//
//        //tooltip = tooltip+date;
//
//        //tooltip=tooltip+"\nsize: "+value.rows.size();
//
//
//        //BlockBreakFinder.teleported_vein=value;
//        //WBBFItem item = new WBBFItem(this,value.id,theme.item(DisplayItemUtils.toStack(BuiltInRegistries.ITEM.getValue(itemId))),
//       //         coords[0],coords[1],coords[2],value.rows.getFirst().getUser(),date,value.rows.getFirst().getWrld());
//        //tooltip=tooltip+"\nid: "+value.id;
//        //item.tooltip=tooltip;
//
//
//
//        return item;
//    }
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

    //ArrayList<HistoryRecordClass> history = null
    //получить список обезьян
    public void getHistories(){
       // ArrayList<HistoryRecordClass> monkeyList = new ArrayList<>();
        String sql = "SELECT * FROM history ORDER BY id DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)){
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                int id = rs.getInt("id");
                String command = rs.getString("command");
                int rows_amount = rs.getInt("rows_amount");

                //monkeyList.put(username,new MonkeyClass(jail_name,username, time_left, admin_username, reason,0));
                HistoryRecordClass history_obj = new HistoryRecordClass();
                history_obj.id = id;
                history_obj.command = command;
                history_obj.rowsAmount = rows_amount;
                history.add(history_obj);
            }
        } catch (SQLException e) {
            System.out.println("Не удалось получить список обезьян");
        }

        //return monkeyList;
    }

    public ArrayList<RowFromRecordClass> getHistoryRows(int id){
        //String sql = "UPDATE jails SET spawn_key = ?, jail_name = ? WHERE id = ?";
        ArrayList<RowFromRecordClass> list = new ArrayList<>();
        String sql = "SELECT * FROM rows WHERE history_id = ?";
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
                //getLogger().info("id: "+jailId);
            }
        } catch (SQLException e) {
            System.out.println("Не удалось получить записи");
            return null;
        }
        return list;
    }


}
