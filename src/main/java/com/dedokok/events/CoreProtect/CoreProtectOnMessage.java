package com.dedokok.events.CoreProtect;

import com.dedokok.DedTools;
import com.dedokok.events.Cancellable;
import com.dedokok.systems.modules.Feature.CoreProtectTweaks;
import com.dedokok.systems.modules.Modules;
import com.dedokok.utils.classes.CoreProtect.MessagesTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoreProtectOnMessage {
    public static boolean hideMessages = false;
    public static HashMap<MessagesTypes,String>messagesTypesENG = new HashMap<>(Map.ofEntries(
            Map.entry(MessagesTypes.rowBlockPlace,"^.+ ago . (.+) placed (.+)\\.$"),
            Map.entry(MessagesTypes.rowBlockBreak,"^.+ ago . (.+) broke (.+)\\.$"),
            Map.entry(MessagesTypes.rowItemPickup,"^.+ ago . (.+) picked up x(.+) (.+)\\.$"),
            Map.entry(MessagesTypes.rowItemDrop,"^.+ ago . (.+) dropped x(.+) (.+)\\.$"),
            Map.entry(MessagesTypes.rowItemThrew,"^.+ ago . (.+) threw x(.+) (.+)\\.$"),
            Map.entry(MessagesTypes.rowSessionJoin,"^.+ ago . (.+) logged in\\.$"),
            Map.entry(MessagesTypes.rowSessionExit,"^.+ ago . (.+) logged out\\.$"),
            //Map.entry(MessagesTypes.rowInventoryAdd,"^.+ ago . (.+) deposited x(.+) (.+)\\.$"),
            //Map.entry(MessagesTypes.rowInventoryRemove,"^.+ ago . (.+) withdrew x(.+) (.+)\\.$"),
            Map.entry(MessagesTypes.rowKill,"^.+ ago . (.+) killed (.+)\\.$"),
            Map.entry(MessagesTypes.rowUsername,"^.+ ago . (.+) logged in as (.+)\\.$"),
            Map.entry(MessagesTypes.rowText,"^.+ ago . (.+): (.+)\\.$"),
            Map.entry(MessagesTypes.rowClick,"^.+ ago . (.+) clicked (.+)\\.$"),
            Map.entry(MessagesTypes.rowContainerAdd,"^.+ ago . (.+) added x(.+) (.+)\\.$"),
            Map.entry(MessagesTypes.rowContainerRemove,"^.+ ago . (.+) removed x(.+) (.+)\\.$"),
            //26.2 Map.entry(MessagesTypes.systemFoundRowsAmount,"^CoreProtect - (.+) rows? found\\.$"),
            Map.entry(MessagesTypes.systemFoundRowsAmount,"^----- CoreProtect \\| (.+) rows found\\."),
            Map.entry(MessagesTypes.systemWaitLookup,"^CoreProtect - Lookup searching\\. Please wait\\.\\.\\.$"),
            //26.2 Map.entry(MessagesTypes.systemResults,"^----- CoreProtect Lookup Results -----$"),
            Map.entry(MessagesTypes.systemResults,"^----- CoreProtect \\|  Lookup Results -----$"),

            //----- CoreProtect |  Lookup Results -----
                Map.entry(MessagesTypes.systemCords,"\\^ \\(x(.+)\\/y(.+)\\/z(.+)\\/(\\S+)\\)"),
            Map.entry(MessagesTypes.systemPagesNext,"^Page (\\d+)\\/(\\d+) ▶ \\(.+\\)$"),
            Map.entry(MessagesTypes.systemPagesBoth,"^◀ Page (\\d+)\\/(\\d+) ▶ \\(.+\\)$"),
            Map.entry(MessagesTypes.systemPagesBack,"^◀ Page (\\d+)\\/(\\d+) \\(.+\\)$"),
            Map.entry(MessagesTypes.systemEnabled,"^CoreProtect - Inspector now enabled\\.$"),
            Map.entry(MessagesTypes.systemDisabled,"^CoreProtect - Inspector now disabled\\.$"),
            Map.entry(MessagesTypes.systemDataBaseBusy,"^CoreProtect - Database busy\\. Please try again later\\.$"),
            Map.entry(MessagesTypes.systemNoData,"^CoreProtect - No data found at (.+)\\.$"),
            Map.entry(MessagesTypes.systemNothingFound,"^CoreProtect - No results found\\.$"),
            Map.entry(MessagesTypes.systemInspectorCords,"^----- CoreProtect ----- \\(x(.+)\\/y(.+)\\/z(.+)\\)$"),
            Map.entry(MessagesTypes.systemInspectorContainer,"^----- Container Transactions ----- \\(x(.+)\\/y(.+)\\/z(.+)\\)$"),
            Map.entry(MessagesTypes.systemInspectorInteraction,"^----- Player Interactions ----- \\(x(.+)\\/y(.+)\\/z(.+)\\)$"),
            Map.entry(MessagesTypes.systemTeleport,"^CoreProtect - Teleported to x(.+)\\/y(.+)\\/z(.+)\\/(.+)\\.$"),
            Map.entry(MessagesTypes.systemPleaseWait,"^CoreProtect - Please wait a moment and try again\\.$")
    ));
    public static HashMap<MessagesTypes,String>messagesTypesRU = new HashMap<>(Map.ofEntries(
            Map.entry(MessagesTypes.rowBlockPlace,"^.+ назад . (.+) поставил (.+)\\.$"),
            Map.entry(MessagesTypes.rowBlockBreak,"^.+ назад . (.+) сломал (.+)\\.$"),
            Map.entry(MessagesTypes.rowItemPickup,"^.+ назад . (.+) подобрал x(.+) (.+)\\.$"),
            Map.entry(MessagesTypes.rowItemDrop,"^.+ назад . (.+) бросил x(.+) (.+)\\.$"),
            Map.entry(MessagesTypes.rowItemThrew,"^.+ назад . (.+) выбросил x(.+) (.+)\\.$"),
            Map.entry(MessagesTypes.rowSessionJoin,"^.+ назад . (.+) зашёл\\.$"),
            Map.entry(MessagesTypes.rowSessionExit,"^.+ назад . (.+) вышел\\.$"),
            // Map.entry(MessagesTypes.rowInventoryAdd,"^.+ назад . (.+) положил x(.+) (.+)\\.$"),
            //Map.entry(MessagesTypes.rowInventoryRemove,"^.+ назад . (.+) забрал x(.+) (.+)\\.$"),
            Map.entry(MessagesTypes.rowKill,"^.+ назад . (.+) убил (.+)\\.$"),
            Map.entry(MessagesTypes.rowUsername,"^.+ назад . (.+) зашёл под ником (.+)\\.$"),
            Map.entry(MessagesTypes.rowText,"^.+ назад . (.+): (.+)$"),
            Map.entry(MessagesTypes.rowClick,"^.+ назад . (.+) кликнул (.+)$"),
            Map.entry(MessagesTypes.rowContainerAdd,"^.+ назад . (.+) положил x(.+) (.+)$"),
            Map.entry(MessagesTypes.rowContainerRemove,"^.+ назад . (.+) забрал x(.+) (.+)$"),
            Map.entry(MessagesTypes.systemFoundRowsAmount,"^CoreProtect - Найдено записей: (.+)\\.$"),
            Map.entry(MessagesTypes.systemWaitLookup,"^CoreProtect - Идёт поиск\\. Подожди\\.\\.\\.$"),
            Map.entry(MessagesTypes.systemResults,"^----- Результаты Поиска CoreProtect \\|  -----$"),
            Map.entry(MessagesTypes.systemCords,"\\^ \\(x(.+)\\/y(.+)\\/z(.+)\\/(.+)\\)$"),
            Map.entry(MessagesTypes.systemPagesNext,"^Страница (.+)\\/(.+) ▶ \\(.+\\)$"),
            Map.entry(MessagesTypes.systemPagesBoth,"^◀ Страница (.+)\\/(.+) ▶ \\(.+\\)$"),
            Map.entry(MessagesTypes.systemPagesBack,"^◀ Страница (.+)\\/(.+) \\(.+\\)$"),
            Map.entry(MessagesTypes.systemEnabled,"^CoreProtect - Инспектор включён\\.$"),
            Map.entry(MessagesTypes.systemDisabled,"^CoreProtect - Инспектор выключен\\.$"),
            Map.entry(MessagesTypes.systemDataBaseBusy,"^CoreProtect - База занята\\. Попробуй позже\\.$"),
            Map.entry(MessagesTypes.systemNoData,"^CoreProtect - Нет данных в (.+)\\.$"),
            Map.entry(MessagesTypes.systemNothingFound,"^CoreProtect - Ничего не найдено.$"),
            Map.entry(MessagesTypes.systemInspectorCords,"^----- CoreProtect ----- \\(x(.+)\\/y(.+)\\/z(.+)\\)$"),
            Map.entry(MessagesTypes.systemTeleport,"^CoreProtect - Телепорт в x(.+)\\/y(.+)\\/z(.+)\\/(.+)\\.$"),
            Map.entry(MessagesTypes.systemPleaseWait,"^CoreProtect - Подожди немного и попробуй снова\\.$")
    ));
    static HashMap<MessagesTypes,String>messagesTypes = new HashMap<>();
    public boolean onMessage(Component message, boolean overlay){
        CoreProtectTweaks.Languages lang = Modules.get().get(CoreProtectTweaks.class).timeZoneSetting.get();
        messagesTypes = lang == CoreProtectTweaks.Languages.ENG ? messagesTypesENG : messagesTypesRU;
        cancelled = false;
        String message_text = message.getString().replaceAll("§.","");//removeColors(message.getString());
        if(message_text.contains("Page")){
            int a = 0;
            a++;
        }
        for(MessagesTypes type : messagesTypes.keySet()){
            if(type == MessagesTypes.systemPagesBack || type == MessagesTypes.systemPagesNext || type == MessagesTypes.systemPagesBoth){
                int b = 0;
                b++;
            }
            if(checkMessage(type,message_text)){
                getValues(type,message, messagesTypes.get(type));
                //return false;
                //break;

            }
        }
        return !cancelled;

    }

    public static String removeColors(String message){
        String regex = "§.";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        if(matcher.find()){
            message = matcher.replaceAll("");

        }
        return message;
    }

    public static boolean checkMessage(MessagesTypes type, String message){
        String regex = messagesTypes.get(type);

        if(regex==null){
            return false;
        }
        try {
            //System.out.println(regex);
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message);
            return matcher.find();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }


    public static void getValues(MessagesTypes type, Component message, String regex){
        String message_text = message.getString().replaceAll("§.","");//removeColors(message.getString());
        switch(type){
            case rowBlockPlace:
            case rowBlockBreak:
            case rowKill:
            case rowUsername:
            case rowText:
            case rowClick: {
                long time = getTimeFromRow(message);
                String value = getGroup(type, message_text, 2,regex);
                String username = getGroup(type, message_text, 1,regex);
                getEvent(type,message,username,value,time,null,null,null);
                break;
            }
            case rowItemPickup:
            case rowItemDrop:
            case rowItemThrew:
            //case rowInventoryAdd:
           // case rowInventoryRemove:
            case rowContainerRemove:
            case rowContainerAdd:{
                long time = getTimeFromRow(message);
                String value = getGroup(type, message_text, 3,regex);
                String username = getGroup(type, message_text, 1,regex);
                int amount = -1;
                try {
                    amount = Integer.parseInt(getGroup(type, message_text, 2,regex));
                }
                catch (NumberFormatException _){}
                if(amount==-1)return;
                getEvent(type,message,username,value,time,null,amount,null);
                break;
            }
            case rowSessionJoin:
            case rowSessionExit:{
                long time = getTimeFromRow(message);
                String username = getGroup(type, message_text, 1,regex);
                getEvent(type,message,username,null,time,null,null,null);
                break;
            }

            case systemFoundRowsAmount:{
                String amount_str =  getGroup(type, message_text, 1,regex);
                amount_str = amount_str.replace(",","");
                int  amount = 0;
                try {
                    amount = Integer.parseInt(amount_str);
                }
                catch (NumberFormatException _){}
                //getEvent(type,message,null,null,-1,null,amount);
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.System.get(type,message,amount,null,null,null,null,null,null,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemFoundRowsAmount.get(type,message,amount));
                break;
            }

            case systemCords:{
                Integer x = null,y=null,z=null;
                String world = null;
                try{
                    x = Integer.parseInt(getGroup(type,message_text,1,regex));
                    y = Integer.parseInt(getGroup(type,message_text,2,regex));
                    z = Integer.parseInt(getGroup(type,message_text,3,regex));
                    world =  getGroup(type,message_text,4,regex);
                }
                catch (NumberFormatException _){}
                if(x==null || y==null || z==null || world == null){
                    return;
                }
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.System.get(type,message,null,x,y,z,null,null,null,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemCords.get(type,message,x,y,z,world));
                break;
            }

            case systemPagesNext:
            case systemPagesBack:
            case systemPagesBoth:{
                int pageNow = -1, maxPages = -1;
                try{
                    pageNow = Integer.parseInt(getGroup(type,message_text,1,regex));
                    maxPages = Integer.parseInt(getGroup(type,message_text,2,regex));
                }
                catch (NumberFormatException _){}
                if(pageNow==-1 || maxPages==-1){
                    return;
                }

                getEvent(type,message,null,null,null,null,pageNow,maxPages);
                break;
            }

            case systemNoData:{
                String block = getGroup(type,message_text,1,regex);
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.System.get(type,message,null,null,null,null,null,null,null,block));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemNoData.get(type,message,block));
                break;
            }

            case systemTeleport:{
                int x=-1,y=-1,z=-1;
                String world = null;
                try{
                    x = Integer.parseInt(getGroup(type,message_text,1,regex));
                    y = Integer.parseInt(getGroup(type,message_text,2,regex));
                    z = Integer.parseInt(getGroup(type,message_text,3,regex));
                    world =  getGroup(type,message_text,4,regex);
                }
                catch (NumberFormatException _){}
                if(x==-1 || y==-1 || z==-1 || world == null){
                    return;
                }
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.System.get(type,message,null,x,y,z,null,null,world,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemTeleport.get(type,message,x,y,z,world));
                break;
            }

            case systemInspectorContainer:
            case systemInspectorInteraction:
            case systemInspectorCords:{
                int x=-1,y=-1,z=-1;
                try{
                    x = Integer.parseInt(getGroup(type,message_text,1,regex));
                    y = Integer.parseInt(getGroup(type,message_text,2,regex));
                    z = Integer.parseInt(getGroup(type,message_text,3,regex));
                }
                catch (NumberFormatException _){}
                if(x==-1 || y==-1 || z==-1){
                    return;
                }
                switch(type){
                    case systemInspectorContainer: {
                        post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemInspectorContainer.get(type,message,x,y,z));
                        break;
                    }
                    case systemInspectorInteraction: {
                        post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemInspectorInteraction.get(type,message,x,y,z));
                        break;
                    }
                    case systemInspectorCords:{
                        post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemInspectorCords.get(type,message,x,y,z));
                        break;
                    }
                }

                post(com.dedokok.events.CoreProtect.CoreProtectEvent.System.get(type,message,null,x,y,z,null,null,null,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemInspector.get(type,message,x,y,z));

                break;
            }

            case systemWaitLookup:
            case systemResults:
            case systemEnabled:
            case systemDisabled:
            case systemDataBaseBusy:
            case systemNothingFound:
            case systemPleaseWait:{
                getEvent(type,message,null,null,null,null,null,null);
                break;
            }




        }
    }


    private static boolean cancelled;


    public static void getEvent(MessagesTypes type, Component message, String username, String value, Long time, String addStrValue, Integer amount, Integer addInt){

        switch(type){
            //1 value (username)
            case rowSessionJoin:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.Row.get(type,message,username,time,null,null,null,null,null,null,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowSession.get(type,message,username,time));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowSessionJoin.get(type,message,username,time));
                break;
            }
            case rowSessionExit:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.Row.get(type,message,username,time,null,null,null,null,null,null,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowSession.get(type,message,username,time));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowSessionExit.get(type,message,username,time));
                break;
            }


            //2 values (username, item/block/entity/newUsername)
            case rowBlockPlace: {
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.Row.get(type,message,username,time,value,null,null,null,null,null,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowBlock.get(type,message,username,time,value));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowBlockPlace.get(type,message,username,time,value));
                break;
            }
            case rowBlockBreak:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.Row.get(type,message,username,time,value,null,null,null,null,null,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowBlock.get(type,message,username,time,value));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowBlockBreak.get(type,message,username,time,value));
                break;
            }
            case rowKill:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.Row.get(type,message,username,time,null,null,value,null,null,null,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowKill.get(type,message,username,time,value));
                break;
            }
            case rowUsername:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.Row.get(type,message,username,time,null,null,null,value,null,null,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowUsername.get(type,message,username,time,value));
                break;
            }
            case rowText:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.Row.get(type,message,username,time,null,null,null,null,value,null,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowText.get(type,message,username,time,value));
                break;
            }
            case rowClick: {
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.Row.get(type,message,username,time,null,null,null,null,null,value,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowClick.get(type,message,username,time,value));
                break;
            }


            //3 values (username, amount, item/block)
            case rowItemPickup:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.Row.get(type,message,username,time,null,value,null,null,null,null,amount));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowItem.get(type,message,username,time,value,amount));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowItemPickup.get(type,message,username,time,value,amount));
                break;
            }
            case rowItemDrop:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.Row.get(type,message,username,time,null,value,null,null,null,null,amount));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowItem.get(type,message,username,time,value,amount));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowItemDrop.get(type,message,username,time,value,amount));
                break;
            }
            case rowItemThrew:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.Row.get(type,message,username,time,null,value,null,null,null,null,amount));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowItem.get(type,message,username,time,value,amount));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowItemThrew.get(type,message,username,time,value,amount));
                break;
            }
//            case rowInventoryAdd:{
//                post(com.dedokok.events.CoreProtect.CoreProtectEvent.Row.get(type,message,username,time,null,value,null,null,null,null,amount));
//                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowInventory.get(type,message,username,time,value,amount));
//                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowInventoryAdd.get(type,message,username,time,value,amount));
//                break;
//            }
//            case rowInventoryRemove:{
//                post(com.dedokok.events.CoreProtect.CoreProtectEvent.Row.get(type,message,username,time,null,value,null,null,null,null,amount));
//                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowInventory.get(type,message,username,time,value,amount));
//                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowInventoryRemove.get(type,message,username,time,value,amount));
//                break;
//            }
            case rowContainerRemove:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.Row.get(type,message,username,time,null,value,null,null,null,null,amount));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowContainer.get(type,message,username,time,value,amount));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowContainerRemove.get(type,message,username,time,value,amount));
                break;
            }
            case rowContainerAdd:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.Row.get(type,message,username,time,null,value,null,null,null,null,amount));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowContainer.get(type,message,username,time,value,amount));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowContainerAdd.get(type,message,username,time,value,amount));
                break;
            }

            case systemPagesNext:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.System.get(type,message,null,null,null,null,amount,addInt,null,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemPages.get(type,message,amount,addInt));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemPagesNext.get(type,message,amount,addInt));
                break;
            }
            case systemPagesBack:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.System.get(type,message,null,null,null,null,amount,addInt,null,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemPages.get(type,message,amount,addInt));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemPagesBack.get(type,message,amount,addInt));
                break;
            }
            case systemPagesBoth:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.System.get(type,message,null,null,null,null,amount,addInt,null,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemPages.get(type,message,amount,addInt));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemPagesBoth.get(type,message,amount,addInt));
                break;
            }


            case systemWaitLookup:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.System.get(type,message,null,null,null,null,null,null,null,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemWaitLookup.get(type,message));
                break;
            }
            case systemResults:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.System.get(type,message,null,null,null,null,null,null,null,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemResults.get(type,message));
                break;
            }
            case systemEnabled:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.System.get(type,message,null,null,null,null,null,null,null,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemEnabled.get(type,message));
                break;
            }
            case systemDisabled:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.System.get(type,message,null,null,null,null,null,null,null,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemDisabled.get(type,message));
                break;
            }
            case systemDataBaseBusy:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.System.get(type,message,null,null,null,null,null,null,null,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemDataBaseBusy.get(type,message));
                break;
            }
            case systemNothingFound:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.System.get(type,message,null,null,null,null,null,null,null,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemNothingFound.get(type,message));
                break;
            }
            case systemPleaseWait:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.System.get(type,message,null,null,null,null,null,null,null,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemPleaseWait.get(type,message));
                break;
            }
        }
    }

    private static void post(Cancellable event) {
        DedTools.EVENT_BUS.post(event);
        if (event.isCancelled()) cancelled = true;
    }

    public static String getGroup(MessagesTypes type, String message, int group, String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);
        if(matcher.find()){
            if(matcher.groupCount()>=group){
                String res = matcher.group(group);
                return res;

            }
            return "-";
        }
        return "-";
    }


    public static long getTimeFromRow(Component message){
        HoverEvent hoverEvent = message.getSiblings().get(0).getStyle().getHoverEvent();
        HoverEvent.ShowText hoverEventValue = (HoverEvent.ShowText) hoverEvent;
        long timestamp = convertStringToDate(hoverEventValue.value().getString());
        return timestamp;
    }

    public static long convertStringToDate(String string){

        String regex = "^(\\d{4}(-\\d{2}){2} \\d{2}(:\\d{2}){2})";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);
        if(!matcher.find())return -1;

        String date_string = matcher.group(1);


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(date_string, formatter);
        ZoneId zoneid = ZoneId.of("Europe/Moscow");


        long seconds = localDateTime.atZone(zoneid)
                .toInstant()
                .toEpochMilli();
        return seconds/1000;
    }
}
