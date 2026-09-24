package com.dedokok.systems.modules.Feature;

import com.dedokok.events.render.Render3DEvent;
import com.dedokok.events.world.TickEvent;
import com.dedokok.settings.*;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Feature.blockesp.ESPBlock;
import com.dedokok.systems.modules.Module;
import com.dedokok.systems.modules.Modules;
import com.dedokok.utils.classes.Coords;
import com.dedokok.utils.classes.Row;
import com.dedokok.utils.classes.Vein;
import com.dedokok.utils.misc.Keybind;
import com.dedokok.utils.render.color.Color;
import com.dedokok.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;

public class BlockBreakFinder extends Module {
    private int startTicks = 0;
    private BlockBreakFinder blockBreakFinder;
    public BlockBreakFinder() {
        super(Categories.Feature, "BlockBreakFinder", "Find broken blocks",null);
        runInMainMenu=true;
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public static List<Block> ORES = List.of(Blocks.DEEPSLATE_DIAMOND_ORE);
    public static HashSet<String> ORES_String = new HashSet<>();
    public static HashMap<Long,Row> rows= new HashMap<>();
    private int maxPages = -1;
    private int commandCooldown = 0;
    private int pageNow = 1;
    private int maxRowsCount = 0;

    private boolean isHandledRowsCountMessage = false;
    private boolean isStarted = false;
    private boolean isHandledStartMessage = false;
    private boolean isHandledEndMessage = false;
    private boolean isHandledRow = false;
    private boolean isSentCountCommand = false;
    private boolean isSentLookupCommand = false;

    public static int veinNow = 0;
    HashSet<Long>checkedRows = new HashSet<>();



    public static HashSet<Vein>veinsHashSet = new HashSet<>();
    public static ArrayList<Vein>veinsArrayList = new ArrayList<>();

    public boolean waitForTPMessage = false;
    public int tpCommandTicks = 0;
    public int veinStatsHudTicks = 0;

    public static Vein teleported_vein = null;

    public static ArrayList<Vein>veinsPlayerNow = new ArrayList<>();
    public static String prevUsername = null;
    public static ArrayList<ESPBlock> blocks = new ArrayList<>();




    private final Setting<TimeZones> timeZoneSetting = sgGeneral.add(new EnumSetting.Builder<TimeZones>()
            .name("timezone")
            .description("Timezone of your CoreProtect")
            .defaultValue(TimeZones.Moscow)
            .onChanged(mode -> {
                disable();
                stopProcess();
            })
            .build()
    );
    public enum TimeZones {
        Moscow,
        UTC
    }


    private final Setting<Boolean>enableESPSetting = sgGeneral.add(new BoolSetting.Builder()
            .name("enable-esp")
            .description("Enable BlockESP and Tracers")
            .defaultValue(true)
            .build()
    );
    private final Setting<SettingColor>espColorSetting = sgGeneral.add(new ColorSetting.Builder()
            .name("esp-color")
            .description("BlockESP and tracers color")
            .defaultValue(Color.WHITE)
            .visible(enableESPSetting::get)
            .build()
    );

    private final Setting<List<Block>> blocksSetting = sgGeneral.add(new BlockListSetting.Builder()
            .name("blocks-to-search")
            .description("Whats blocks you need to look")
            .defaultValue(ORES)
            .onChanged(_ -> {
                updateOres();
                if(isActive())this.disable();
            })
            .build()
    );

    private final Setting<HashSet<Vein>> veinsSetting = sgGeneral.add(new VeinsListSetting.Builder()
            .name("your-veins")
            .description("All your gotten veins")
            .onChanged(_ -> {
                updateOres();
                if(isActive())this.disable();
            })
            .build()
    );


//    private final Setting<Integer> delayBeforeNextSessionSetting = sgGeneral.add(new IntSetting.Builder()
//            .name("delay-before-next-session")
//            .description("Max time between two mining sessions in seconds")
//            .defaultValue(3600)
//            .build()
//    );

    private final Setting<Integer> timeBeforeStopSetting = sgGeneral.add(new IntSetting.Builder()
            .name("time-before-stop")
            .description("Time before disable module if it can't handle any CoreProtect message in seconds")
            .defaultValue(10)
            .build()
    );


    private final Setting<String> timeToLookupSetting = sgGeneral.add(new StringSetting.Builder()
                    .name("time-to-lookup")
                    .description("What time in lookup command should use")
                    .defaultValue("1d")
                    .build()
    );
    private final Setting<Integer> maxRowsAmountSetting = sgGeneral.add(new IntSetting.Builder()
            .name("max-rows-amount")
            .description("Amount of CoreProtect rows you'll get at one page")
            .defaultValue(100)
            .min(1)
            .build()
    );

    private final Setting<Integer> maxPagesAmountSetting = sgGeneral.add(new IntSetting.Builder()
            .name("max-pages-amount")
            .description("Amount of CoreProtect pages you'll get")
            .defaultValue(5)
            .min(1)
            .build()
    );


    private final SettingGroup sgUsersToLookup = settings.createGroup("UsersToLookup");

    private final Setting<List<String>> usersToLookupSetting = sgUsersToLookup.add(new StringListSetting.Builder()
            .name("users-to-lookup")
            .description("Whats users in lookup command should use")
            .build()
    );


    private final SettingGroup sgExcludeUsersToLookup = settings.createGroup("ExcludeUsersToLookup");

    private final Setting<ExcludeTypes> excludeType = sgExcludeUsersToLookup.add(new EnumSetting.Builder<ExcludeTypes>()
            .name("exclude-users-type")
            .description("Type of how mod excludes users. Mod - get all rows and after remove excluded players. Command - add players to \"exclude:\" coreprotect attribute")
            .defaultValue(ExcludeTypes.Mod)
            .onChanged(mode -> {
                disable();
                stopProcess();
            })
            .build()
    );
    public enum ExcludeTypes {
        Mod,
        Command
    }

    public final Setting<List<String>> excludeEsersToLookupSetting = sgExcludeUsersToLookup.add(new StringListSetting.Builder()
            .name("exclude-users-to-lookup")
            .description("Whats users in lookup command should skip")
            .build()
    );

    private final Setting<String> radiusToLookupSetting = sgGeneral.add(new StringSetting.Builder()
            .name("radius-to-lookup")
            .description("What radius in lookup command should use. F.e. \"#world\"")
            .build()
    );

    private final Setting<Integer> timeBetweenCommandsSetting = sgGeneral.add(new IntSetting.Builder()
            .name("time-between-commands")
            .description("Time between page commands in ticks")
            .defaultValue(20)
            .min(0)
            .build()
    );
    private final ButtonSetting startGettingRowsSetting = sgGeneral.add(new ButtonSetting.Builder()
            .defaultValue(screen)
            .width(50)
            .onChanged(_ -> {
                this.enable();
                rows.clear();
                newRow=new Row();
                startTicks=0;
                updateOres();
                isStarted = true;
            })
            .name("start-getting-rows")
            .build()
    );

    private final Setting<Keybind>keyBindNextSetting = sgGeneral.add(new KeybindSetting.Builder()
            .name("keybind-next-vein")
            .description("Keybind to teleport to the next vein")
            .defaultValue(Keybind.fromKey(GLFW_KEY_RIGHT))
                    .action(()->{
                        if(isActive() && mc!=null && mc.gui.screen()==null) {
                            nextVein();
                        }
                    })
            .build()
    );
    private final Setting<Keybind>keyBindPrevSetting = sgGeneral.add(new KeybindSetting.Builder()
            .name("keybind-prev-vein")
            .description("Keybind to teleport to the previous vein")
            .defaultValue(Keybind.fromKey(GLFW_KEY_LEFT))
            .action(()->{
                if(isActive() && mc!=null && mc.gui.screen()==null) {
                    prevVein();
                }
            })
            .build()
    );
    private final Setting<Boolean> tpJustInSpectator = sgGeneral.add(new BoolSetting.Builder()
            .name("tp-only-in-spectator")
            .description("Teleport to veins just in spectator")
            .defaultValue(true)
            .build()
    );
    public final Setting<Boolean> veinStatsHudSetting = sgGeneral.add(new BoolSetting.Builder()
            .name("enable-vein-stats-hud")
            .description("Enables teleported vein stats")
            .defaultValue(true)
            .build()
    );
    private final Setting<Integer> timeBeforeHudRemoveHudSetting = sgGeneral.add(new IntSetting.Builder()
            .name("time-before-remove-hud")
            .description("Time before remove hud after tp to vein in seconds")
            .defaultValue(20)
            .min(0)
            .visible(veinStatsHudSetting::get)
            .build()
    );


    public void nextVein(){
        if(mc==null || mc.player==null)return;
        //mc.player.sendSystemMessage(Component.literal("next"));
        veinNow++;
        if(veinNow==veinsArrayList.size()){
            mc.player.sendSystemMessage(Component.literal("Reached list's end, went to the start"));
            veinNow=0;
        }
        teleportToVein(veinNow);
        return;
    }
    public void prevVein(){
        veinNow--;
        if(veinNow==-1){
            mc.player.sendSystemMessage(Component.literal("Reached list's start, went to the end"));
            veinNow=veinsArrayList.size()-1;
        }
        teleportToVein(veinNow);
    }


    public void teleportToVein(int veinId){
        if(mc != null && mc.player != null){
            if(!mc.player.isSpectator() && tpJustInSpectator.get()){
                return;
            }

            if(veinsArrayList.isEmpty()){
                mc.player.sendSystemMessage(Component.literal("Veins list is empty!").withColor(TextColor.RED));
                return;
            }

            if(veinsArrayList.size()>veinId && veinId>=0){
                waitForTPMessage=true;
                int[]coords = unpack(veinsArrayList.get(veinId).rows.getFirst().getCoords());
                int x = coords[0], y = coords[1], z = coords[2];
                String world = veinsArrayList.get(veinId).rows.getFirst().getWrld();
                String command = "co teleport "+world+" "+x+" "+y+" "+z;
                mc.player.connection.sendCommand(command);
                teleported_vein=veinsArrayList.get(veinId);
                veinStatsHudTicks=0;
            }
        }
    }



    @EventHandler
    private void onTick(TickEvent.Post event){
        tpCommandTicks++;
        veinStatsHudTicks++;
        if(tpCommandTicks/20>=timeBeforeStopSetting.get()){
            waitForTPMessage=false;
        }
        if(veinStatsHudTicks/20>=timeBeforeHudRemoveHudSetting.get()){
            veinStatsHudTicks=0;
            teleported_vein=null;
        }

        if(prevUsername==null && !veinsArrayList.isEmpty()){
            prevUsername = "";
            //veinsPlayerNow = getPlayerVeins(prevUsername);
        }

        if(!veinsArrayList.isEmpty()){
            if(!prevUsername.equals(veinsArrayList.get(veinNow).rows.getFirst().getUser())) {
                prevUsername = veinsArrayList.get(veinNow).rows.getFirst().getUser();
                veinsPlayerNow = getPlayerVeins(prevUsername);
                blocks.clear();
                getPlayerBlocks(prevUsername);
            }
        }




        if(!isStarted){return;}
        startTicks++;
        if(startTicks/20>= timeBeforeStopSetting.get()){
            stopProcess();
        }
        commandCooldown++;
        if(commandCooldown>= timeBetweenCommandsSetting.get()){
            if(!isSentLookupCommand){
                sendCoreProtectLookupCommand();
            }
            else{
                if(pageNow<maxPages){
                    pageNow++;
                    sendListCommand();
                }
            }
        }
        if(((isHandledEndMessage && rows.size() >= maxRowsCount) || rows.size() >= maxRowsCount) && isHandledRowsCountMessage) {

            mc.player.sendSystemMessage(Component.literal("Got " + rows.size() + " rows"));
            stopProcess();
            findVeins();
            if(excludeType.get()==ExcludeTypes.Mod) excludeUsers();
            teleportToVein(0);


        }
    }

    public void excludeUsers(){
        List<String> excludeUsers = excludeEsersToLookupSetting.get();
        if(excludeUsers.isEmpty())return;

        ArrayList<Vein>newVeins = new ArrayList<>();
        for(Vein vein : veinsArrayList){
            if(excludeUsers.contains(vein.rows.getFirst().getUser()))continue;
            newVeins.add(vein);
        }
        veinsArrayList=newVeins;
    }



    public void findVeins(){
        veinsHashSet.clear();
        veinsArrayList.clear();
        checkedRows.clear();
        for(Row row : rows.values()){
            if(checkedRows.contains(row.getCoords()))continue;
            checkedRows.add(row.getCoords());
            Vein vein = findAllOresINVein(row);
            veinsHashSet.add(vein);
        }
        veinsArrayList.addAll(veinsHashSet);
        veinsHashSet.clear();

        veinsArrayList.sort(Comparator.comparing(
                (Vein value) -> value.rows.getFirst().getTimestamp()
        ).reversed());
        int id = 0;
        for(Vein vein :veinsArrayList){
            vein.id=id;
            id++;
        }

    }

    public Vein findAllOresINVein(Row row){
        Vein vein = new Vein();
        vein.rows.add(row);

        int[]coords;
        int bX;
        int bY;
        int bZ;

        for(int i = 0; i<vein.rows.size();i++){
            row = vein.rows.get(i);
            coords = unpack(row.getCoords());
            bX = coords[0];
            bY = coords[1];
            bZ = coords[2];

            for(int x = -1; x<2;x++) {
                for (int y = -1; y < 2; y++) {
                    for (int z = -1; z < 2; z++) {
                        if(rows.containsKey(pack(bX+x, bY+y, bZ+z))){
                            Row temp_row = rows.get(pack(bX+x, bY+y, bZ+z));
                            if(row.getBlock().equals(temp_row.getBlock()) && !vein.rows.contains(temp_row)){
                                vein.rows.add(temp_row);
                                checkedRows.add(temp_row.getCoords());
                            }
                        }
                    }
                }
            }
        }
        return vein;
    }




    @Override
    public void onDeactivate(){
        stopProcess();


    }
    @Override
    public void onActivate(){
        if(Modules.get().isActive(Patrol.class)){
            Modules.get().get(Patrol.class).disable();
        }
    }

    private void updateOres(){
        ORES_String.clear();
        for(Block block : ORES){
            ORES_String.add(block.getName().toString());
        }
    }


    private void stopProcess() {

        count=0;
        isHandledRowsCountMessage=false;
        isStarted = false;
        isHandledStartMessage = false;
        isHandledEndMessage = false;
        startTicks = 0;
        commandCooldown = 0;
        isSentLookupCommand = false;
        isSentCountCommand = false;
        isHandledRow=false;
        pageNow=1;
        teleported_vein=null;
        veinsArrayList.clear();
        veinsHashSet.clear();
        tpCommandTicks=0;
        veinStatsHudTicks=0;
        prevUsername=null;
        blocks.clear();

    }
    int count = 0;
    public Row newRow = new Row();
    public boolean onGameMessage(Component message, boolean overlay) {
        if (isStarted) {

            if (!isHandledStartMessage && checkStartMessage(message.getString())) {
                isHandledStartMessage = true;
                isHandledEndMessage = false;
                startTicks = 0;
                return false;
            }


            if (checkRowMessage(message.getString()) && !isHandledRow) {
                if (!isHandledStartMessage || isHandledEndMessage) {
                    return false;
                }

                startTicks = 0;
                count++;

                //parseSiblings(message, "1");
                String block_String = "";
                String user_String = "";
                if (message.getSiblings().size() == 2) {
                    block_String = message.getSiblings().get(1).getSiblings().get(3).getString();
                    user_String = message.getSiblings().get(1).getSiblings().get(1).getString();
                } else {
                    block_String = message.getSiblings().get(3).getSiblings().get(1).getString();
                    user_String = message.getSiblings().get(2).getString();
                }
                HoverEvent hoverEvent = message.getSiblings().get(0).getStyle().getHoverEvent();
                HoverEvent.ShowText hoverEventValue = (HoverEvent.ShowText) hoverEvent;
                long timestamp = convertStringToDate(hoverEventValue.value().getString());

                newRow.setBlock(block_String);
                newRow.setUser(user_String);
                newRow.setTimestamp(timestamp);

                isHandledRow = true;
                return false;
            } else if (checkCoordsMessage(message.getString())) {
                if (!isHandledStartMessage || isHandledEndMessage || !isHandledRow) {
                    return false;
                }

                startTicks = 0;

                String coords_String = message.getSiblings().get(2).getString();
                if (coords_String.equals("^ ")) {
                    coords_String = message.getSiblings().get(3).getString();
                }

                Coords coords = convertStringToCoords(coords_String);


                newRow.setWorld(coords.getWorld());
                int x = coords.getX();
                int y = coords.getY();
                int z = coords.getZ();
                Long row_coords = pack(x, y, z);
                newRow.setCoords(row_coords);
                if (rows.containsKey(row_coords)) {
                    maxRowsCount--;
                }
                rows.put(pack(x, y, z), newRow);
                newRow = new Row();
                isHandledRow = false;
                return false;
            } else if (!isHandledRowsCountMessage && checkRowsCountMessage(message.getString())) {
                int rows_count = getRowsCount(message.getString());
                int maxPages_temp = getPagesCountFromCount(rows_count);
                maxPages = Math.min(maxPagesAmountSetting.get(), maxPages_temp);
                mc.player.sendSystemMessage(Component.literal("Max rows amount: " + rows_count + ". Pages: " + maxPages));
                maxRowsCount = Math.min(maxPages * maxRowsAmountSetting.get(), rows_count);
                isHandledRowsCountMessage = true;
                return false;
            } else if (checkCoreProtectMessage(message.getString())) {
                return false;
            } else if (checkPagesCountMessage(message.getString())) {
                isHandledEndMessage = true;
                isHandledStartMessage = false;
                return false;
            }
        }
        else if(waitForTPMessage || checkTPMessage(message.getString())) {
            waitForTPMessage=false;
            return false;
        }
        return true;


    }
    public void parseSiblings(Component sibling, String level){
        List<Component>siblings = sibling.getSiblings();
        int count = 1;
        for(Component component : siblings){
            //System.out.println(level+"."+count+" "+component.getString());
            if(!component.getSiblings().isEmpty()){
                parseSiblings(component,level+"."+count);
            }
            count++;
        }

    }

    public Coords convertStringToCoords(String string){
        String regex = "\\(x(.+)\\/y(.+)\\/z(.+)\\/(.+)\\)$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);

        if(!matcher.find())return null;
        int x = Integer.parseInt(matcher.group(1));
        int y = Integer.parseInt(matcher.group(2));
        int z = Integer.parseInt(matcher.group(3));
        String world = matcher.group(4);

        return new Coords(x,y,z,world);
    }


    public long convertStringToDate(String string){

        String regex = "^(\\d{4}(-\\d{2}){2} \\d{2}(:\\d{2}){2})";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);
        if(!matcher.find())return -1;

        String date_string = matcher.group(1);


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(date_string, formatter);
        ZoneId zoneid = timeZoneSetting.get()==TimeZones.Moscow ? ZoneId.of("Europe/Moscow") : ZoneId.of("UTC");


        long seconds = localDateTime.atZone(zoneid)
                .toInstant()
                .toEpochMilli();
        return seconds/1000;
    }


    public boolean checkStartMessage(String message){
        String regex = "^CoreProtect - Lookup searching. Please wait...$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        return matcher.find();
    }

    public boolean checkRowMessage(String message){
        String regex = "^\\d+.+ago - .+ broke .+\\.$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        return matcher.find();
    }

    public boolean checkCoordsMessage(String message){
        String regex = "\\(x.+\\/y.+\\/z.+\\/.+\\)$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        return matcher.find();
    }

    public boolean checkPagesCountMessage(String message){
        String regex = "(Page \\d+\\/.+ \\(.+\\))";
        //^----- CoreProtect \|  Lookup Results -----$

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        return matcher.find();
    }

    public boolean checkCoreProtectMessage(String message){
        String regex = "(^CoreProtect - Lookup searching\\. Please wait\\.\\.\\.$)|(^----- CoreProtect \\|  Lookup Results -----$)|(^CoreProtect - Teleported to)";
        //^----- CoreProtect \|  Lookup Results -----$

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        return matcher.find();
    }

    public boolean checkTPMessage(String message){
        String regex = "(^CoreProtect - Teleported to)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        return matcher.find();
    }

    public boolean checkRowsCountMessage(String message){
        String regex = "^CoreProtect - (.+) rows found.$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);
        return matcher.find();
    }

    public int getRowsCount(String message){
        //System.out.println(message);
        String regex = "^CoreProtect - (.+) rows found.$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);
        matcher.find();
        String rows_count_string = matcher.group(1);
        if(rows_count_string.contains(",")) {
            rows_count_string = rows_count_string.replaceAll(",", "");
        }
        return Integer.parseInt(rows_count_string);
    }

    public int stringToPages(String message){
        return Integer.parseInt(message);
    }

    public int getPagesCountFromCount(int count){
        return ((count + maxRowsAmountSetting.get() - 1) / maxRowsAmountSetting.get());
    }


    public void sendCoreProtectLookupCommand(){

        if(mc.player==null)return;
        if(!mc.isMultiplayerServer()){
            disable();
            return;
        }
        String radius = radiusToLookupSetting.get();
        if(radius != null && !radius.equals("")){
            radius = " r:"+radius;
        }
        String blocks_string = "";
        for(int i = 0; i< blocksSetting.get().size(); i++){
            Block block = blocksSetting.get().get(i);
            blocks_string=blocks_string+ BuiltInRegistries.BLOCK.getKey(block).getPath();
            if(i< blocksSetting.get().size()-1){
                blocks_string=blocks_string+",";
            }
        }
        String users = " u:";
        if(usersToLookupSetting.get()!=null && !usersToLookupSetting.get().isEmpty()){
            for(String username :  usersToLookupSetting.get()){
                users=users+ username;
            }
        }
        else{
            users="";
        }
        String excludeUsers = "";
        if(excludeType.get()==ExcludeTypes.Command) {
            if (excludeEsersToLookupSetting.get() != null && !excludeEsersToLookupSetting.get().isEmpty()) {
                excludeUsers = " exclude:";
                for (String username : excludeEsersToLookupSetting.get()) {
                    if(!excludeUsers.isEmpty())excludeUsers = excludeUsers + ",";
                    excludeUsers = excludeUsers + username;
                }
            }
        }
        String command = "co l a:-block t:"+ timeToLookupSetting.get()+users+excludeUsers+radius+" i:"+blocks_string+" rows: "+ maxRowsAmountSetting.get();
        if(!isSentCountCommand){
            command = command + " #count";
            isSentCountCommand=true;
            mc.player.sendOverlayMessage(Component.literal("Send coreprotect #count lookup").withColor(TextColor.GREEN));

        }
        else{
            isSentLookupCommand=true;
            mc.player.sendOverlayMessage(Component.literal("Send coreprotect lookup command").withColor(TextColor.GREEN));
        }
        if(command.length()>255){
            mc.player.sendSystemMessage(Component.literal("Error, command size cant be more that 255").withColor(TextColor.RED));
            return;
        }
        mc.player.connection.sendCommand(command);

        startTicks=0;
        commandCooldown=0;
    }






    public void sendListCommand(){
        mc.player.connection.sendCommand("co l "+pageNow);
        mc.player.sendOverlayMessage(Component.literal("Получил страницу "+pageNow).withColor(TextColor.GREEN));
        commandCooldown=0;
    }





    @EventHandler
    private void onRender(Render3DEvent event) {
        if(!enableESPSetting.get())return;
        if(blocks==null || blocks.isEmpty())return;
        double[] prevCoords = null;
        for(ESPBlock block : blocks){
            block.render(event, espColorSetting.get());
            if(prevCoords!=null){
                tracer(event,new double[]{block.x,block.y,block.z},prevCoords);
            }
            prevCoords= new double[]{block.x, block.y, block.z};
        }
    }

    public static ArrayList<Vein>getPlayerVeins(String username){
        ArrayList<Vein>newVeins = new ArrayList<>();
        for(Vein vein : veinsArrayList){
            if(vein.rows.getFirst().getUser().equals(username)){
                newVeins.add(vein);
            }
        }
        return newVeins;
    }

    public static void getPlayerBlocks(String username){

        for(Vein vein : veinsArrayList){
            if(vein.rows.getFirst().getUser().equals(username)){
                addVeinBlocks(vein.rows);
            }
        }
    }

    public static void addVeinBlocks(ArrayList<Row>rows){
        for(Row row : rows){
            int[] coords = unpack(row.getCoords());
            int x = coords[0], y = coords[1], z = coords[2];
            addBlock(new BlockPos(x,y,z),true);
        }
    }

    public static void addBlock(BlockPos blockPos, boolean update) {
        ESPBlock block = new ESPBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        blocks.add(block);

        if (update) block.update();

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



        Color color = espColorSetting.get();
        event.renderer.line(x_1, y_1, z_1, x_2, y_2, z_2, color);

    }




    public static long pack(int x, int y, int z) {
        // Смещаем Y в положительный диапазон (например, +64)
        long ry = (long) (y + 64) & 0xFFF; // 12 бит для Y (0 - 4095)
        long rx = (long) x & 0x3FFFFFF;   // 26 бит для X
        long rz = (long) z & 0x3FFFFFF;   // 26 бит для Z

        // Собираем в один long: X занимает старшие биты, затем Z, Y — младшие
        return (rx << 38) | (rz << 12) | ry;
    }

    public static int[] unpack(long packed) {
        int y = (int) (packed & 0xFFF) - 64;
        int z = (int) ((packed >> 12) & 0x3FFFFFF);
        int x = (int) ((packed >> 38) & 0x3FFFFFF);

        // Коррекция знака для 26-битных чисел
        if ((x & (1 << 25)) != 0) x -= (1 << 26);
        if ((z & (1 << 25)) != 0) z -= (1 << 26);

        return new int[] { x, y, z };
    }


    public static void updateVeinsIds(){
        int id = 0;
        for(Vein vein : veinsArrayList){
            if(vein.isRemoved)continue;
            vein.id=id;
            id++;
        }
        //veinsHashSet.clear();
        //veinsHashSet.addAll(veinsArrayList);
    }

    public static void deleteVein(Vein vein){
        //veinsHashSet.remove(vein);
        veinsArrayList.remove(vein);
        updateVeinsIds();
    }

    public static void removePlayer(String username){
        ArrayList<Vein>newVeins =  new ArrayList<>();
        for(Vein vein : veinsArrayList){
            if(!vein.rows.getFirst().getUser().equals(username)){
                newVeins.add(vein);
            }
        }
        veinsArrayList=newVeins;
        updateVeinsIds();
        //veinsHashSet.clear();
        //veinsHashSet.addAll(veinsArrayList);
    }




}
