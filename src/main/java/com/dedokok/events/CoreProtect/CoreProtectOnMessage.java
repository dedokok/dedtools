package com.dedokok.events.CoreProtect;

import com.dedokok.DedTools;
import com.dedokok.events.Cancellable;
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
    public static HashMap<MessagesTypes,String>messagesTypes = new HashMap<>(Map.ofEntries(
            Map.entry(MessagesTypes.rowBlockPlace,"^.+ назад . (.+) поставил (.+)\\.$"),
            Map.entry(MessagesTypes.rowBlockBreak,"^.+ назад . (.+) сломал (.+)\\.$"),
            Map.entry(MessagesTypes.rowItemPickup,"^.+ назад . (.+) подобрал x(.+) (.+)\\.$"),
            Map.entry(MessagesTypes.rowItemDrop,"^.+ назад . (.+) бросил x(.+) (.+)\\.$"),
            Map.entry(MessagesTypes.rowItemThrew,"^.+ назад . (.+) выбросил x(.+) (.+)\\.$"),
            Map.entry(MessagesTypes.rowSessionJoin,"^.+ назад . (.+) зашёл\\.$"),
            Map.entry(MessagesTypes.rowSessionExit,"^.+ назад . (.+) вышел\\.$"),
            Map.entry(MessagesTypes.rowInventoryAdd,"^.+ назад . (.+) положил x(.+) (.+)\\.$"),
            Map.entry(MessagesTypes.rowInventoryRemove,"^.+ назад . (.+) забрал x(.+) (.+)\\.$"),
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
    public boolean onMessage(Component message, boolean overlay){
        cancelled = false;
        String message_text = message.getString();
        for(MessagesTypes type : messagesTypes.keySet()){
            if(checkMessage(type,message_text)){
                getValues(type,message);
            }
        }
        return !cancelled;

    }

    public static boolean checkMessage(MessagesTypes type, String message){
        String regex = messagesTypes.get(type);
        if(regex==null){
            return false;
        }

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        return matcher.find();
    }


    public static void getValues(MessagesTypes type, Component message){
        switch(type){
            case rowBlockPlace:
            case rowBlockBreak:
            case rowKill:
            case rowUsername:
            case rowText:
            case rowClick: {
                long time = getTimeFromRow(message);
                String value = getGroup(type, message, 2);
                String username = getGroup(type, message, 1);
                getEvent(type,message,username,value,time,null,null,null);
                break;
            }
            case rowItemPickup:
            case rowItemDrop:
            case rowItemThrew:
            case rowInventoryAdd:
            case rowInventoryRemove:
            case rowContainerRemove:
            case rowContainerAdd:{
                long time = getTimeFromRow(message);
                String value = getGroup(type, message, 3);
                String username = getGroup(type, message, 1);
                int amount = -1;
                try {
                    amount = Integer.parseInt(getGroup(type, message, 3));
                }
                catch (NumberFormatException _){}
                if(amount==-1)return;
                getEvent(type,message,username,value,time,null,amount,null);
                break;
            }
            case rowSessionJoin:
            case rowSessionExit:{
                long time = getTimeFromRow(message);
                String username = getGroup(type, message, 1);
                getEvent(type,message,username,null,time,null,null,null);
                break;
            }

            case systemFoundRowsAmount:{
                String amount_str =  getGroup(type, message, 1);
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
                int x = -1,y=-1,z=-1;
                String world = null;
                try{
                    x = Integer.parseInt(getGroup(type,message,1));
                    y = Integer.parseInt(getGroup(type,message,2));
                    z = Integer.parseInt(getGroup(type,message,3));
                    world =  getGroup(type,message,4);
                }
                catch (NumberFormatException _){}
                if(x==-1 || y==-1 || z==-1 || world == null){
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
                    pageNow = Integer.parseInt(getGroup(type,message,1));
                    maxPages = Integer.parseInt(getGroup(type,message,2));
                }
                catch (NumberFormatException _){}
                if(pageNow==-1 || maxPages==-1){
                    return;
                }

                getEvent(type,message,null,null,null,null,pageNow,maxPages);
                break;
            }

            case systemNoData:{
                String block = getGroup(type,message,1);
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.System.get(type,message,null,null,null,null,null,null,null,block));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemNoData.get(type,message,block));
                break;
            }

            case systemTeleport:{
                int x=-1,y=-1,z=-1;
                String world = null;
                try{
                    x = Integer.parseInt(getGroup(type,message,1));
                    y = Integer.parseInt(getGroup(type,message,2));
                    z = Integer.parseInt(getGroup(type,message,3));
                    world =  getGroup(type,message,4);
                }
                catch (NumberFormatException _){}
                if(x==-1 || y==-1 || z==-1 || world == null){
                    return;
                }
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.System.get(type,message,null,x,y,z,null,null,world,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemTeleport.get(type,message,x,y,z,world));
                break;
            }

            case systemInspectorCords:{
                int x=-1,y=-1,z=-1;
                try{
                    x = Integer.parseInt(getGroup(type,message,1));
                    y = Integer.parseInt(getGroup(type,message,2));
                    z = Integer.parseInt(getGroup(type,message,3));
                }
                catch (NumberFormatException _){}
                if(x==-1 || y==-1 || z==-1){
                    return;
                }
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.System.get(type,message,null,x,y,z,null,null,null,null));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.SystemInspectorCords.get(type,message,x,y,z));
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
            case rowInventoryAdd:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.Row.get(type,message,username,time,null,value,null,null,null,null,amount));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowInventory.get(type,message,username,time,value,amount));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowInventoryAdd.get(type,message,username,time,value,amount));
                break;
            }
            case rowInventoryRemove:{
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.Row.get(type,message,username,time,null,value,null,null,null,null,amount));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowInventory.get(type,message,username,time,value,amount));
                post(com.dedokok.events.CoreProtect.CoreProtectEvent.RowInventoryRemove.get(type,message,username,time,value,amount));
                break;
            }
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

    public static String getGroup(MessagesTypes type, Component message, int group){
        String regex = messagesTypes.get(type);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message.getString());
        if(matcher.find()){
            if(matcher.groupCount()>=group){
                return matcher.group(group);

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
