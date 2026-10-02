package com.dedokok.events.CoreProtect;


import com.dedokok.events.Cancellable;
import com.dedokok.utils.classes.CoreProtect.MessagesTypes;
import net.minecraft.network.chat.Component;

public class CoreProtectEvent {
    public static class Row extends Cancellable {
        protected static final Row INSTANCE = new Row();
        public MessagesTypes type;
        public Component message;
        public String username;
        public Long time;
        public String block;
        public String item;
        public String entity;
        public String newUsername;
        public String text;
        public String clicked;
        public Integer amount;

        public static Row get(MessagesTypes type, Component message, String username, Long time, String block, String item, String entity, String newUsername, String text, String clicked, Integer amount) {
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.username = username;
            INSTANCE.time = time;
            INSTANCE.block = block;
            INSTANCE.item = item;
            INSTANCE.entity = entity;
            INSTANCE.newUsername = newUsername;
            INSTANCE.text = text;
            INSTANCE.clicked = clicked;
            INSTANCE.amount = amount;

            return INSTANCE;
        }
    }



    //block
    public static class RowBlock extends Cancellable {
        protected static final RowBlock INSTANCE = new RowBlock();
        public MessagesTypes type;
        public Component message;
        public String username;
        public long time;
        public String block;


        public static RowBlock get(MessagesTypes type, Component message,String username, long time, String block) {
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.username = username;
            INSTANCE.time = time;
            INSTANCE.block = block;
            return INSTANCE;
        }
    }

    //blockPlace
    public static class RowBlockPlace extends Cancellable {
        protected static final RowBlockPlace INSTANCE = new RowBlockPlace();
        public MessagesTypes type;
        public Component message;
        public String username;
        public long time;
        public String block;


        public static RowBlockPlace get(MessagesTypes type, Component message,String username, long time, String block) {
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.username = username;
            INSTANCE.time = time;
            INSTANCE.block = block;
            return INSTANCE;
        }
    }

    //blockBreak
    public static class RowBlockBreak extends Cancellable {
        protected static final RowBlockBreak INSTANCE = new RowBlockBreak();
        public MessagesTypes type;
        public Component message;
        public String username;
        public long time;
        public String block;


        public static RowBlockBreak get(MessagesTypes type, Component message,String username, long time, String block) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.username = username;
            INSTANCE.time = time;
            INSTANCE.block = block;
            return INSTANCE;
        }
    }


    //item
    public static class RowItem extends Cancellable {
        protected static final RowItem INSTANCE = new RowItem();
        public MessagesTypes type;
        public Component message;
        public String username;
        public long time;
        public String item;
        public int amount;


        public static RowItem get(MessagesTypes type, Component message,String username, long time, String item, int amount) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.username = username;
            INSTANCE.time = time;
            INSTANCE.item = item;
            INSTANCE.amount = amount;
            return INSTANCE;
        }
    }

    //itemPickup
    public static class RowItemPickup extends Cancellable {
        protected static final RowItemPickup INSTANCE = new RowItemPickup();
        public MessagesTypes type;
        public Component message;
        public String username;
        public long time;
        public String item;
        public int amount;

        public static RowItemPickup get(MessagesTypes type, Component message,String username, long time, String item, int amount) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.username = username;
            INSTANCE.time = time;
            INSTANCE.item = item;
            INSTANCE.amount = amount;
            return INSTANCE;
        }
    }

    //item
    public static class RowItemDrop extends Cancellable {
        protected static final RowItemDrop INSTANCE = new RowItemDrop();
        public MessagesTypes type;
        public Component message;
        public String username;
        public long time;
        public String item;
        public int amount;


        public static RowItemDrop get(MessagesTypes type, Component message,String username, long time, String item, int amount) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.username = username;
            INSTANCE.time = time;
            INSTANCE.item = item;
            INSTANCE.amount = amount;
            return INSTANCE;
        }
    }

    //itemThrew
    public static class RowItemThrew extends Cancellable {
        protected static final RowItemThrew INSTANCE = new RowItemThrew();
        public MessagesTypes type;
        public Component message;
        public String username;
        public long time;
        public String item;
        public int amount;

        public static RowItemThrew get(MessagesTypes type, Component message,String username, long time, String item,  int amount) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.username = username;
            INSTANCE.time = time;
            INSTANCE.item = item;
            INSTANCE.amount = amount;
            return INSTANCE;
        }
    }


    //session
    public static class RowSession extends Cancellable {
        protected static final RowSession INSTANCE = new RowSession();
        public MessagesTypes type;
        public Component message;
        public String username;
        public long time;


        public static RowSession get(MessagesTypes type, Component message,String username, long time) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.username = username;
            INSTANCE.time = time;
            return INSTANCE;
        }
    }

    //sessionJoin
    public static class RowSessionJoin extends Cancellable {
        protected static final RowSessionJoin INSTANCE = new RowSessionJoin();
        public MessagesTypes type;
        public Component message;
        public String username;
        public long time;


        public static RowSessionJoin get(MessagesTypes type, Component message,String username, long time) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.username = username;
            INSTANCE.time = time;
            return INSTANCE;
        }
    }

    //sessionExit
    public static class RowSessionExit extends Cancellable {
        protected static final RowSessionExit INSTANCE = new RowSessionExit();
        public MessagesTypes type;
        public Component message;
        public String username;
        public long time;


        public static RowSessionExit get(MessagesTypes type, Component message,String username, long time) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.username = username;
            INSTANCE.time = time;
            return INSTANCE;
        }
    }


    //inventory
    public static class RowInventory extends Cancellable {
        protected static final RowInventory INSTANCE = new RowInventory();
        public MessagesTypes type;
        public Component message;
        public String username;
        public long time;
        public String item;
        public int amount;
        public static RowInventory get(MessagesTypes type, Component message,String username, long time, String item, int amount) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.username = username;
            INSTANCE.time = time;
            INSTANCE.item = item;
            INSTANCE.amount = amount;
            return INSTANCE;
        }
    }

    //inventory
    public static class RowInventoryAdd extends Cancellable {
        protected static final RowInventoryAdd INSTANCE = new RowInventoryAdd();
        public MessagesTypes type;
        public Component message;
        public String username;
        public long time;
        public String item;
        public int amount;
        public static RowInventoryAdd get(MessagesTypes type, Component message,String username, long time, String item, int amount) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.username = username;
            INSTANCE.time = time;
            INSTANCE.item = item;
            INSTANCE.amount = amount;
            return INSTANCE;
        }
    }

    //inventory
    public static class RowInventoryRemove extends Cancellable {
        protected static final RowInventoryRemove INSTANCE = new RowInventoryRemove();
        public MessagesTypes type;
        public Component message;
        public String username;
        public long time;
        public String item;
        public int amount;
        public static RowInventoryRemove get(MessagesTypes type, Component message,String username, long time, String item,  int amount) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.username = username;
            INSTANCE.time = time;
            INSTANCE.item = item;
            INSTANCE.amount = amount;
            return INSTANCE;
        }
    }


    //kill
    public static class RowKill extends Cancellable {
        protected static final RowKill INSTANCE = new RowKill();
        public MessagesTypes type;
        public Component message;
        public String username;
        public long time;
        public String entity;
        public static RowKill get(MessagesTypes type, Component message,String username, long time, String entity) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.username = username;
            INSTANCE.time = time;
            INSTANCE.entity = entity;
            return INSTANCE;
        }
    }

    //username
    public static class RowUsername extends Cancellable {
        protected static final RowUsername INSTANCE = new RowUsername();
        public MessagesTypes type;
        public Component message;
        public String username;
        public long time;
        public String newUsername;
        public static RowUsername get(MessagesTypes type, Component message,String username, long time, String newUsername) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.username = username;
            INSTANCE.time = time;
            INSTANCE.newUsername = newUsername;
            return INSTANCE;
        }
    }


    //text
    public static class RowText extends Cancellable {
        protected static final RowText INSTANCE = new RowText();
        public MessagesTypes type;
        public Component message;
        public String username;
        public long time;
        public String text;
        public static RowText get(MessagesTypes type, Component message,String username, long time,  String text) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.username = username;
            INSTANCE.time = time;
            INSTANCE.text = text;
            return INSTANCE;
        }
    }

    //text
    public static class RowClick extends Cancellable {
        protected static final RowClick INSTANCE = new RowClick();
        public MessagesTypes type;
        public Component message;
        public String username;
        public long time;
        public String clicked;
        public static RowClick get(MessagesTypes type, Component message,String username, long time,  String clicked) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.username = username;
            INSTANCE.time = time;
            INSTANCE.clicked = clicked;
            return INSTANCE;
        }
    }

    //container
    public static class RowContainer extends Cancellable {
        protected static final RowContainer INSTANCE = new RowContainer();
        public MessagesTypes type;
        public Component message;
        public String username;
        public long time;
        public String item;
        public int amount;
        public static RowContainer get(MessagesTypes type, Component message,String username, long time,  String item, int amount) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.username = username;
            INSTANCE.time = time;
            INSTANCE.item = item;
            INSTANCE.amount = amount;
            return INSTANCE;
        }
    }

    //container
    public static class RowContainerAdd extends Cancellable {
        protected static final RowContainerAdd INSTANCE = new RowContainerAdd();
        public MessagesTypes type;
        public Component message;
        public String username;
        public long time;
        public String item;
        public int amount;
        public static RowContainerAdd get(MessagesTypes type, Component message,String username, long time,  String item,  int amount) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.username = username;
            INSTANCE.time = time;
            INSTANCE.item = item;
            INSTANCE.amount = amount;
            return INSTANCE;
        }
    }

    //container
    public static class RowContainerRemove extends Cancellable {
        protected static final RowContainerRemove INSTANCE = new RowContainerRemove();
        public MessagesTypes type;
        public Component message;
        public String username;
        public long time;
        public String item;
        public int amount;
        public static RowContainerRemove get(MessagesTypes type, Component message,String username, long time,  String item,  int amount) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.username = username;
            INSTANCE.time = time;
            INSTANCE.item = item;
            INSTANCE.amount = amount;
            return INSTANCE;
        }
    }





    //system
    public static class System extends Cancellable {
        protected static final System INSTANCE = new System();
        public MessagesTypes type;
        public Component message;
        public Integer amount;
        public Integer x;
        public Integer y;
        public Integer z;
        public String world;
        public Integer pageNow;
        public Integer pageMax;
        public String block;

        public static System get(MessagesTypes type, Component message, Integer amount, Integer x, Integer y, Integer z, Integer pageNow, Integer pageMax, String world, String block) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.amount = amount;
            INSTANCE.x = x;
            INSTANCE.y = y;
            INSTANCE.z = z;
            INSTANCE.world = world;
            INSTANCE.pageNow = pageNow;
            INSTANCE.pageMax = pageMax;
            INSTANCE.block = block;


            return INSTANCE;
        }
    }



    //systemFoundRowsAmount
    public static class SystemFoundRowsAmount extends Cancellable {
        protected static final SystemFoundRowsAmount INSTANCE = new SystemFoundRowsAmount();
        public MessagesTypes type;
        public Component message;
        public int amount;
        public static SystemFoundRowsAmount get(MessagesTypes type, Component message, int amount) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.amount = amount;
            return INSTANCE;
        }
    }


    //systemWaitLookup
    public static class SystemWaitLookup extends Cancellable {
        protected static final SystemWaitLookup INSTANCE = new SystemWaitLookup();
        public MessagesTypes type;
        public Component message;
        public static SystemWaitLookup get(MessagesTypes type, Component message) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            return INSTANCE;
        }
    }

    //systemPleaseWait
    public static class SystemPleaseWait extends Cancellable {
        protected static final SystemPleaseWait INSTANCE = new SystemPleaseWait();
        public MessagesTypes type;
        public Component message;
        public static SystemPleaseWait get(MessagesTypes type, Component message) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            return INSTANCE;
        }
    }

    //systemResults
    public static class SystemResults extends Cancellable {
        protected static final SystemResults INSTANCE = new SystemResults();
        public MessagesTypes type;
        public Component message;
        public static SystemResults get(MessagesTypes type, Component message) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            return INSTANCE;
        }
    }

    //systemCords
    public static class SystemCords extends Cancellable {
        protected static final SystemCords INSTANCE = new SystemCords();
        public MessagesTypes type;
        public Component message;
        public int x;
        public int y;
        public int z;
        public String world;

        public static SystemCords get(MessagesTypes type, Component message, int x, int y, int z, String world) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.x = x;
            INSTANCE.y = y;
            INSTANCE.z = z;
            INSTANCE.world = world;
            return INSTANCE;
        }
    }

    //systemPages
    public static class SystemPages extends Cancellable {
        protected static final SystemPages INSTANCE = new SystemPages();
        public MessagesTypes type;
        public Component message;
        public int pageNow;
        public int pageMin;
        public int pageMax;
        public static SystemPages get(MessagesTypes type, Component message,  int pageNow, int pageMax) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.pageNow = pageNow;
            INSTANCE.pageMax = pageMax;
            return INSTANCE;
        }
    }

    //systemPages
    public static class SystemPagesNext extends Cancellable {
        protected static final SystemPagesNext INSTANCE = new SystemPagesNext();
        public MessagesTypes type;
        public Component message;
        public int pageNow;
        public int pageMin;
        public int pageMax;
        public static SystemPagesNext get(MessagesTypes type, Component message,  int pageNow, int pageMax) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.pageNow = pageNow;
            INSTANCE.pageMax = pageMax;
            return INSTANCE;
        }
    }
    //systemPages
    public static class SystemPagesBack extends Cancellable {
        protected static final SystemPagesBack INSTANCE = new SystemPagesBack();
        public MessagesTypes type;
        public Component message;
        public int pageNow;
        public int pageMin;
        public int pageMax;
        public static SystemPagesBack get(MessagesTypes type, Component message,  int pageNow, int pageMax) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.pageNow = pageNow;
            INSTANCE.pageMax = pageMax;
            return INSTANCE;
        }
    }
    //systemPages
    public static class SystemPagesBoth extends Cancellable {
        protected static final SystemPagesBoth INSTANCE = new SystemPagesBoth();
        public MessagesTypes type;
        public Component message;
        public int pageNow;
        public int pageMax;
        public static SystemPagesBoth get(MessagesTypes type, Component message,  int pageNow, int pageMax) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.pageNow = pageNow;
            INSTANCE.pageMax = pageMax;
            return INSTANCE;
        }
    }
    //systemEnabled
    public static class SystemEnabled extends Cancellable {
        protected static final SystemEnabled INSTANCE = new SystemEnabled();
        public MessagesTypes type;
        public Component message;
        public static SystemEnabled get(MessagesTypes type, Component message) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            return INSTANCE;
        }
    }

    //systemDisabled
    public static class SystemDisabled extends Cancellable {
        protected static final SystemDisabled INSTANCE = new SystemDisabled();
        public MessagesTypes type;
        public Component message;
        public static SystemDisabled get(MessagesTypes type, Component message) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            return INSTANCE;
        }
    }

    //systemDataBaseBusy
    public static class SystemDataBaseBusy extends Cancellable {
        protected static final SystemDataBaseBusy INSTANCE = new SystemDataBaseBusy();
        public MessagesTypes type;
        public Component message;
        public static SystemDataBaseBusy get(MessagesTypes type, Component message) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            return INSTANCE;
        }
    }

    //systemNoData
    public static class SystemNoData extends Cancellable {
        protected static final SystemNoData INSTANCE = new SystemNoData();
        public MessagesTypes type;
        public Component message;
        public String block;
        public static SystemNoData get(MessagesTypes type, Component message, String block) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.block = block;
            return INSTANCE;
        }
    }

    //systemNothingFound
    public static class SystemNothingFound extends Cancellable {
        protected static final SystemNothingFound INSTANCE = new SystemNothingFound();
        public MessagesTypes type;
        public Component message;
        public static SystemNothingFound get(MessagesTypes type, Component message) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            return INSTANCE;
        }
    }

    //systemTeleport
    public static class SystemTeleport extends Cancellable {
        protected static final SystemTeleport INSTANCE = new SystemTeleport();
        public MessagesTypes type;
        public Component message;
        public int x;
        public int y;
        public int z;
        public String world;
        public static SystemTeleport get(MessagesTypes type, Component message, int x, int y, int z, String world) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.x = x;
            INSTANCE.y = y;
            INSTANCE.z = z;
            INSTANCE.world = world;
            return INSTANCE;
        }
    }

    //systemInspectorCords
    public static class SystemInspectorCords extends Cancellable {
        protected static final SystemInspectorCords INSTANCE = new SystemInspectorCords();
        public MessagesTypes type;
        public Component message;
        public int x;
        public int y;
        public int z;
        public static SystemInspectorCords get(MessagesTypes type, Component message, int x, int y, int z) {            INSTANCE.setCancelled(false);
            INSTANCE.setCancelled(false);
            INSTANCE.type = type;
            INSTANCE.message = message;
            INSTANCE.x = x;
            INSTANCE.y = y;
            INSTANCE.z = z;
            return INSTANCE;
        }
    }


}
