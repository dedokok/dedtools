package com.dedokok.systems.modules.Feature;

import com.dedokok.DedTools;
import com.dedokok.events.CoreProtect.CoreProtectEvent;
import com.dedokok.events.CoreProtect.CoreProtectOnMessage;
import com.dedokok.events.packets.PacketEvent;
import com.dedokok.events.render.Render3DEvent;
import com.dedokok.events.world.TickEvent;
import com.dedokok.gui.screens.settings.CoreProtectCommandsHistoryScreen;
import com.dedokok.settings.*;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Feature.blockesp.ESPBlock;
import com.dedokok.systems.modules.Module;
import com.dedokok.utils.classes.CoreProtect.HistoryRecordClass;
import com.dedokok.utils.classes.CoreProtect.MessagesTypes;
import com.dedokok.utils.classes.CoreProtect.RowFromRecordClass;
import com.dedokok.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoreProtectTweaks extends Module {
    public CoreProtectTweaks() {
        super(Categories.Feature, "CoreProtectTweaks", "Tweaks for coreprotect",null);
        runInMainMenu=true;
    }
    ArrayList<RowFromRecordClass>rows = new ArrayList<>();
    //ArrayList<HistoryRecordClass>history = new ArrayList<>();
    HistoryRecordClass history_now = new HistoryRecordClass();
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    int ticksAfterLastMessage = 0;
    boolean waitResults = false;
    public static int maxId = -1;
    public boolean isEdit = false;
    public static int ticksAfterTp = 0;
    public boolean canClearInspector = false;
   // public static HistoryRecordClass renderHistory = null;



    private final Setting<Boolean>enableRowsEditing = sgGeneral.add(new BoolSetting.Builder()
            .name("enable-rows-amount-edit")
            .description("Module will edit your /co lookup command adding rows:X")
            .defaultValue(false)
            .build()
    );
    private final Setting<Integer> rowsAmountSetting = sgGeneral.add(new IntSetting.Builder()
            .name("rows-amount")
            .description("Amount of rows that module will set in your commands")
            .defaultValue(100)
                    .min(1)
                    .max(100)
                    .sliderMin(1)
                    .sliderMax(100)
            .visible(enableRowsEditing::get)
            .build()
    );



    private final Setting<Boolean>enableESPSetting = sgGeneral.add(new BoolSetting.Builder()
            .name("enable-esp-setting")
            .description("Trace blocks where actions happened")
            .defaultValue(false)
            .build()
    );
    private final Setting<Integer> timeRemoveESPSetting = sgGeneral.add(new IntSetting.Builder()
            .name("time-remove")
            .description("Time in seconds after tp to history before esp will be removed. -1 to disable")
            .defaultValue(10)
            .min(-1)
            .sliderMin(-1)
            .sliderMax(100)
            .visible(enableESPSetting::get)
            .build()
    );

    private final ButtonSetting openScreenSetting = sgGeneral.add(new ButtonSetting.Builder()
            .defaultValue(screen)
            .width(50)
            .onChanged(_ -> {
                countcheckedrows=0;
                countrows=0;
                if(screen==null)return;
                CoreProtectCommandsHistoryScreen screen2 = new CoreProtectCommandsHistoryScreen(screen.getRenderer().theme);
                // screen2.onClosed(() -> );

                mc.gui.setScreen(screen2);
            })
            .name("open-history")
            .build()
    );

    public final Setting<Languages> timeZoneSetting = sgGeneral.add(new EnumSetting.Builder<Languages>()
            .name("language")
            .description("Language of coreprotect")
            .defaultValue(Languages.ENG)
            .build()
    );
    public enum Languages {
        ENG,
        RU
    }

    int countrows = 0;
    int countcheckedrows= 0;
    @EventHandler
    public void onRowMessage(CoreProtectEvent.Row event){
        countrows++;
        if(!waitResults) return;
        countcheckedrows++;
        history_now.rowsAmount++;
        ticksAfterLastMessage=0;
        RowFromRecordClass newRow = new RowFromRecordClass(event);
        newRow.message = newRow.message.replaceAll("§.","");
        rows.add(newRow);
        System.out.println(rows.size() +" " +countrows + " "+ countcheckedrows);
//        history.add(history_now);
       // rows.add(new RowFromRecordClass(event));
    }

    @EventHandler
    public void onContainer(CoreProtectEvent.RowContainerAdd event){
        if(!waitResults) return;
    }
    @EventHandler
    public void onContainerRemove(CoreProtectEvent.RowContainerRemove event){
        if(!waitResults) return;
    }

    @Override
    public void onDeactivate(){
        rows.clear();
        espBlocks.clear();
    }

    @EventHandler
    public void onCordsMessage(CoreProtectEvent.SystemCords event){
        if(!waitResults || rows.isEmpty()) return;
        rows.getLast().x = event.x;
        rows.getLast().y = event.y;
        rows.getLast().z = event.z;
        rows.getLast().world = event.world;
    }

    @EventHandler
    public void onPagesMessage(CoreProtectEvent.SystemPages event){
        if(!waitResults)return;
        //waitResults = false;
        //stop();
    }

    @EventHandler
    public void onInspector(CoreProtectEvent.SystemInspector event){
//        if(isEdit && !history_now.rows.isEmpty()){
////            int hnx = history_now.rows.getFirst().x;
////            int hny = history_now.rows.getFirst().y;
////            int hnz = history_now.rows.getFirst().z;
////            if(hnz ==  )
//        }
        ticksAfterLastMessage = 0;
        if(!isEdit)history_now = new HistoryRecordClass();
        history_now.command=event.type.name();
        waitResults = true;
    }


    static boolean checkedMaxId = false;
    @EventHandler
    public void onTick(TickEvent.Server event){
        ticksAfterTp++;
        if(ticksAfterTp/20>timeRemoveESPSetting.get() && !espBlocks.isEmpty() && timeRemoveESPSetting.get()!=-1){
            espBlocks.clear();
            ticksAfterTp=0;
        }


        if(connection==null) {
            CoreProtectTweaks.checkDataBase();
            return;
        }


        if(maxId==-1 && !checkedMaxId){
            getMaxId();
            checkedMaxId = true;
        }
        ticksAfterLastMessage++;
    }

    @EventHandler
    public void onMessage(CoreProtectEvent.Message event){

        if(event.type==MessagesTypes.systemResults){

            if(!waitResults){
                count_ticks=0;
                //System.out.println("start getting");
                waitResults = true;
            }

        }
        //System.out.println("пакет чата "+rows.size());
    }

    public void getMaxId(){
        Thread.startVirtualThread(() -> {
            String sql = "SELECT * FROM history ORDER BY id DESC LIMIT 1";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    maxId = rs.getInt("id");
                }
            } catch (SQLException e) {
                System.out.println("Не удалось получить записи");
            }
        });
    }

    public void getMaxRowId(int history_id){
        Thread.startVirtualThread(() -> {
            String sql = "SELECT * FROM rows WHERE history_id = ? ORDER BY id DESC LIMIT 1";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, history_id);

                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    insertRow(history_id,rs.getInt("id")+1);
                }
            } catch (SQLException e) {
                System.out.println("Не удалось получить записи");
            }
        });
    }
    public void stop(){
        //history.add(history_now);

        if(history_now.rowsAmount==0)return;
        if(!isEdit)maxId++;
        history_now.id=new Integer(maxId);
        insertHistory(history_now);


        //history.clear();
        //history_now = new HistoryRecordClass();
        //history_now = new HistoryRecordClass();
    }

    @EventHandler
    public void onPacket(PacketEvent.Send event){
        if(event.packet instanceof ServerboundChatCommandPacket packet){
            if(isFind("(co|coreprotect) (lookup|l).+rows",packet.command()) || isFind("(co|coreprotect) near",packet.command())){
                ticksAfterLastMessage = 0;
                history_now = new HistoryRecordClass();
                history_now.command=packet.command();
                //waitResults = true;
            }
            else if(isFind("^co l \\d",packet.command())){
                //waitResults=true;
                isEdit=true;
                ticksAfterLastMessage = 0;
            }
        }

    }


    public String onCommand(String command){
        if(!enableRowsEditing.get()){return command;}
        if(isFind("(co|coreprotect) lookup",command) && command.length()+8<256) {

            command = removeRows(command);

            command = command + " rows:" + rowsAmountSetting.get();
            return command;
        }
        return command;
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

    public String removeRows(String value){
        try {
            String regex = "rows:\\d+";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(value);
            if(matcher.find()){
                value = matcher.replaceAll("");
            }
        }
        catch(Exception _){

        }
        return value;
    }




    public static Connection connection = null;
    public static void createDB(){
        Thread.startVirtualThread(() -> {
            try {
                File dbFile = new File(DedTools.FOLDER, "coreprotect_log.db");
                String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                connection = DriverManager.getConnection(url);

                String sql = "CREATE TABLE IF NOT EXISTS rows (" +
                        "id INTEGER, " +
                        "history_id INTEGER NOT NULL," +
                        "type TEXT NOT NULL, " +
                        "message TEXT, " +
                        "username TEXT, " +
                        "time INTEGER, " +
                        "block TEXT, " +
                        "item TEXT, " +
                        "entity TEXT, " +
                        "new_username TEXT, " +
                        "text TEXT, " +
                        "clicked TEXT, " +
                        "amount INTEGER, " +
                        "x INTEGER, " +
                        "y INTEGER, " +
                        "z INTEGER, " +
                        "world TEXT, " +
                        "page_now INTEGER, " +
                        "page_max INTEGER)";

                connection.prepareStatement(sql).executeUpdate();

                sql = "CREATE TABLE IF NOT EXISTS history (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "command TEXT, " +
                        "rows_amount INTEGER)";
                connection.prepareStatement(sql).executeUpdate();
            } catch (SQLException e) {
                System.out.println("Не удалось создать таблицы: " + e.getMessage());
            }
        });
    }

    //подключение к бд обезьян
    public static void connectDB() {
        try {
            File dbFile = new File(DedTools.FOLDER, "coreprotect_log.db");
            String url = "jdbc:sqlite:"+dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            System.out.println("Не удалось подключиться к БД");
        }
    }
    public static void checkDataBase() {
        File dbFile = new File(DedTools.FOLDER, "coreprotect_log.db");
        if (dbFile.exists()) {
            connectDB();
            System.out.println("База данных найдена");
        } else {
            System.out.println("Файл базы данных не найден, будет создан новый");
            createDB();
        }
    }


    public synchronized void insertHistory(HistoryRecordClass history_now) {
        isPastedHistoryInDB=false;
        Thread.startVirtualThread(() -> {
            String sql = "INSERT OR REPLACE INTO history(id, command, rows_amount) VALUES (?, ?,?)";
            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, history_now.id);
                ps.setString(2, history_now.command);
                ps.setInt(3, history_now.rowsAmount);
                ps.executeUpdate();
                if(isEdit){
                    isEdit=false;
                    getMaxRowId(history_now.id);
                }
                else{
                    insertRow(history_now.id,0);
                }
                isPastedHistoryInDB=true;
            } catch (SQLException e) {
                System.out.println("Не удалось вставить историю");
            }
        });
    }


    boolean isPastedRowsInDB = false;
    boolean isPastedHistoryInDB = false;
    public void insertRow(int history_id, int maxRowId) {
        isPastedRowsInDB=false;
        Thread.startVirtualThread(() -> {
            String sql = "INSERT INTO rows(id,history_id, type, message,username,time,block,item,entity,new_username,text," +
                    "clicked,amount,x,y,z,world,page_now,page_max) VALUES (?,?,?, ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            int id =maxRowId;
            int count = 0;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (RowFromRecordClass row : rows) {
                    ps.setInt(1, id);
                    ps.setInt(2, history_id);
                    ps.setString(3, row.type.name());
                    ps.setObject(4, row.message);
                    ps.setObject(5, row.username);
                    ps.setObject(6, row.time);
                    ps.setObject(7, row.block);
                    ps.setObject(8, row.item);
                    ps.setObject(9, row.entity);
                    ps.setObject(10, row.newUsername);
                    ps.setObject(11, row.text);
                    ps.setObject(12, row.clicked);
                    ps.setObject(13, row.amount);
                    ps.setObject(14, row.x);
                    ps.setObject(15, row.y);
                    ps.setObject(16, row.z);
                    ps.setObject(17, row.world);
                    ps.setObject(18, row.pageNow);
                    ps.setObject(19, row.pageMax);
                    ps.addBatch();
                    id++;
                    count++;
                    if(count==1000){
                        ps.executeBatch();
                        count=0;
                    }
                }
                ps.executeBatch();
                connection.commit();
                isPastedRowsInDB = true;
            }
            catch (SQLException e) {
                System.out.println("Не удалось вставить запись: " + e.getMessage());
            }
            rows.clear();
        });

        return;
    }


    public static ArrayList<ESPBlock>espBlocks = new ArrayList<>();

    public static void convertRowsToESP(ArrayList<RowFromRecordClass> rows){
        espBlocks.clear();
        for (RowFromRecordClass row : rows) {
            if(row.x!=null){
                ESPBlock block = new ESPBlock(row.x,row.y,row.z);
                espBlocks.add(block);
                block.update();

            }
        }
    }


    @EventHandler
    public void onRender(Render3DEvent event){
        if(!enableESPSetting.get())return;
       // if(!enableESPSetting.get())return;
        if(espBlocks.isEmpty())return;
        double[] prevCoords = null;
        HashSet<ESPBlock> checkedBlocks = new HashSet<>();
        for(ESPBlock block : espBlocks){
            if(!checkedBlocks.contains(block)) {
                block.render(event, Color.WHITE);
                checkedBlocks.add(block);
            }
            if(prevCoords!=null){
                tracer(event,new double[]{block.x,block.y,block.z},prevCoords);
            }
            prevCoords= new double[]{block.x, block.y, block.z};

        }
        checkedBlocks.clear();
    }

    public void tracer(Render3DEvent event, double[]coords_1, double[]coords_2) {

        double x_1 = coords_1[0], y_1 = coords_1[1], z_1 = coords_1[2];
        double x_2 = coords_2[0], y_2 = coords_2[1], z_2 = coords_2[2];

        x_1+=0.5;
        x_2+=0.5;
        y_1+=0.5;
        y_2+=0.5;
        z_1+=0.5;
        z_2+=0.5;



        Color color = Color.WHITE;
        event.renderer.line(x_1, y_1, z_1, x_2, y_2, z_2, color);

    }




    static int count_ticks = 0;
    @EventHandler
    public void onTickPacket(TickEvent.Server event) {
        if (mc.isSameThread()) return;
        mc.execute(() -> {
            //if (event.packet instanceof ClientboundSetTimePacket) {
                if (mc.player != null) {

                    if(waitResults){
                        count_ticks++;
                        //System.out.println("пакет тика "+count_ticks);
                        //сделал методом тыка. Из-за того, что посреди нескольких results от корпротекта могут быть вставки пакетов тиков, пришлось сделать фору в 5 тиков
                        if(count_ticks>4) {
                            waitResults = false;
                            stop();
                            count_ticks=0;
                        }
                    }
                    //mc.player.sendSystemMessage(Component.literal("пакет тика"));

                }
            //}
//            else if (event.packet instanceof ClientboundSystemChatPacket chat) {
////                if(CoreProtectOnMessage.checkMessage(MessagesTypes.systemResults,chat.content().getString())){
////
////                    if(!waitResults){
////                        count_ticks=0;
////                        System.out.println("start getting");
////                        //ticksAfterLastMessage = 0;
////                        //history_now = new HistoryRecordClass();
////                        //history_now.command=packet.command();
////                        waitResults = true;
////                        //stop();
////                    }
////
////                }
////                System.out.println("пакет чата "+rows.size());
//                //if (mc != null && mc.player != null) {
//                    //mc.player.sendSystemMessage(Component.literal("пакет чата "+ chat.content().getString()));
//                    //System.out.println("пакет чата "+ chat.content().getString());
//                //}
//            }
        });
    }

}
