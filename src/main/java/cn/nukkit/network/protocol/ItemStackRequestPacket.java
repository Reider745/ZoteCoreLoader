package cn.nukkit.network.protocol;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import lombok.Value;

import java.awt.event.ContainerAdapter;
import java.lang.reflect.Method;
import java.util.*;

public class ItemStackRequestPacket extends DataPacket {
    @Override
    public byte pid() {
        return ProtocolInfo.ITEM_STACK_REQUEST_PACKET;
    }

    public ArrayList<ArrayData> list = new ArrayList<>();


    public static class ItemInfo {
        public ItemStackRequestPacket self;
        public HashMap<String, Object> info = new HashMap<>();

        public ItemInfo(ItemStackRequestPacket self){
            this.self = self;
        }

        public void slotInfoRead(String prefix){
            info.put(prefix+"byte1", self.getByte());
            info.put(prefix+"slot_id", self.getByte());
            info.put(prefix+"_network_id", self.getVarInt());
        }

        public void transferBaseRead(boolean readSecondSlotInfo, boolean requiresReadByte){
            int firstByte = 0;

            boolean result = false;
            if (requiresReadByte) {
                firstByte = self.getByte();
                result = (firstByte - 1 < 0x40);
            } else
                result = true;
            info.put("result", result);

            slotInfoRead("");
            if (readSecondSlotInfo)
               slotInfoRead("second_");
        }

        public void take(){
            transferBaseRead(true, true);
        }

        public void place(){
            transferBaseRead(true, true);
        }

        public void swap(){
            transferBaseRead(true, false);
        }

        public void drop(){
            transferBaseRead(false, true);
            info.put("drop", self.getBoolean());
        }

        public void destroy(){
            transferBaseRead(false, true);
        }

        public void consume(){
            transferBaseRead(false, true);
        }

        public void create(){
            info.put("createByte", self.getByte());
        }

        public void dataless(){

        }

        public void recipe(){
            info.put("recipe_int", self.getUnsignedVarInt());
        }

        public void recipeOptional(){
            info.put("recipe_optional", self.getUnsignedVarInt());
            info.put("recipe_optional_int", self.getInt());
        }

        public void beaconPayment(){
            info.put("beacon1", self.getInt());
            info.put("beacon2", self.getInt());
        }

        public void action(){
            int type = self.getByte();
            info.put("type", type);
            switch (type){
                case 0:
                    take();
                    break;
                case 1:
                    place();
                    break;
                case 2:
                    swap();
                    break;
                case 3:
                    drop();
                    break;
                case 4:
                    destroy();
                    break;
                case 5:
                    consume();
                    break;
                case 6:
                    create();
                    break;
                case 7:
                    dataless();
                    break;
                case 8:
                    beaconPayment();
                    break;
                case 9:
                    recipe();
                    break;
                case 10:
                    recipe();
                case 11:
                    recipe();
                    break;
                case 12:
                    recipeOptional();
                    break;
                case 13:
                    break;
                case 14:
                    info.put("items", self.<Item>readVectorList(self::getSlot));
                    break;
            }
        }

        @Override
        public String toString() {
            return "ItemInfo{" +
                    "info=" + info +
                    '}';
        }
    }

    interface IRead<T> {
        T read();
    }


    public <T>ArrayList<T> readVectorList(IRead<T> self){
        try {
            ArrayList<T> items = new ArrayList<>();
            long length = this.getUnsignedVarInt();
            if (length > 4096) length = 4096;
            for (int i = 0; i < length; i++) {
                items.add(self.read());
            }
            return items;
        }catch (Exception e){
            Server.getInstance().getLogger().logException(e);
        }
        return null;
    }

    public static class ArrayData {
        public int id_event;
        public ArrayList<ItemInfo> items;
        public ArrayList<String> slots;

        @Override
        public String toString() {
            return "ArrayData{" +
                    "\nid_event=" + id_event +
                    ", \nitems=" + items +
                    ", \nslots=" + slots +
                    '}';
        }
    }

    public void BatchRead(){
        try{
            this.list = this.<ArrayData>readVectorList(this::DataRead);
        }catch (Exception e){
            Server.getInstance().getLogger().logException(e);
        }
    }


    public ArrayData DataRead(){
        ArrayData result = new ArrayData();
        result.id_event = this.getVarInt();
        ItemStackRequestPacket self = this;
        result.items = this.<ItemInfo>readVectorList(() -> {
            ItemInfo item = new ItemInfo(self);
            item.action();
            return item;
        });
        result.slots = this.<String>readVectorList(this::getString);
        return result;
    }

    @Override
    public void decode() {
        BatchRead();

        list.forEach((v) -> v.items.forEach((item_info) -> {
            Logger.debug(item_info.toString());
            Integer slot_id = (Integer) item_info.info.get("slot_id");
            Integer second_slot_id = (Integer) item_info.info.get("second_slot_id");
            Boolean drop = (Boolean) item_info.info.get("drop");

            if(drop != null){
                this.client_player.dropItem(this.client_player.getInventory().getItem(slot_id));
                return;
            }

            Item item = null;
            ArrayList<Item> items = (ArrayList<Item>) item_info.info.get("items");
            if(slot_id != null) {
                item = this.client_player.getInventory().getItem(slot_id);
                this.client_player.getInventory().clear(slot_id);
            } else if(items != null)
               item = items.get(0);
            if(second_slot_id != null && item != null)
                this.client_player.getInventory().setItem(second_slot_id, item);
        }));
    }

    @Override
    public void encode() {

    }
}
