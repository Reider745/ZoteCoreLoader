package cn.nukkit.network.protocol;

import cn.nukkit.Server;
import cn.nukkit.item.Item;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import lombok.Value;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ItemStackRequestPacket extends DataPacket {
    @Override
    public byte pid() {
        return ProtocolInfo.ITEM_STACK_REQUEST_PACKET;
    }

    public ArrayList<ArrayData> list = new ArrayList<>();


    public static class ItemInfo {
        public int byte1;
        public int slot_id;
        public int id;

        public boolean result = false;
        public ItemStackRequestPacket self;
        public boolean drop;
        public int type;
        public int createByte;
        public int int1;
        public int int2;

        public long recipe_int;

        public long recipe_optional;
        public int recipe_optional_int;

        public ItemInfo(ItemStackRequestPacket self){
            this.self = self;
        }

        public void slotInfoRead(){
            byte1 = self.getByte();
            slot_id = self.getByte();
            id = self.getVarInt();
        }

        public void transferBaseRead(boolean readSecondSlotInfo, boolean requiresReadByte){
            int firstByte = 0;

            if (requiresReadByte) {
                firstByte = self.getByte();
                result = (firstByte - 1 < 0x40); // ?
            } else
                result = true;


            slotInfoRead(); // first
            if (readSecondSlotInfo) {
               slotInfoRead();// second
            } else {
                // reset second ItemStackRequestSlotInfo
            }
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
        }

        public void destroy(){
            transferBaseRead(false, true);
        }

        public void consume(){
            transferBaseRead(false, true);
        }

        public void create(){
            createByte = self.getByte();
        }

        public void dataless(){

        }

        public void recipe(){
            recipe_int = self.getUnsignedVarInt();
        }

        public void recipeOptional(){
            recipe_optional = self.getUnsignedVarInt();
            //recipe_optional_int = self.get
        }

        public void beaconPayment(){
            int1 = self.getInt();
            int2 = self.getInt();
        }

        public void action(){
            type = self.getByte();
            Logger.debug("type:"+type);
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
                    break;
            }
        }

        @Override
        public String toString() {
            return "\n byte1:"+byte1+", slot_id:"+slot_id+", id:"+id+", result:"+result+", drop:"+drop+", type:"+type+", createByte:"+createByte+", int1:"+int1+", int2"+int2+", recipe_int:"+recipe_int+", recipe_optional"+recipe_optional+"\n";
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
        public int hz;
        public ArrayList<ItemInfo> items;
        public ArrayList<String> slots;

        @Override
        public String toString() {
            return "ArrayData{" +
                    "\nhz=" + hz +
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
        result.hz = this.getVarInt();
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
        Logger.debug(list.toString());
    }

    @Override
    public void encode() {

    }
}
