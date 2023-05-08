package cn.nukkit.network.protocol.types.request;

import cn.nukkit.network.protocol.DataPacket;

import java.util.ArrayList;

public abstract class InventoryAction {
    public void deserialize(DataPacket packet) throws Exception {};
    public abstract int weight();

    interface IRead<T> {
        T read();
    }
    public <T> ArrayList<T> readVectorList(DataPacket packet, IRead<T> self){
        try {
            ArrayList<T> items = new ArrayList<>();
            long length = packet.getUnsignedVarInt();
            if (length > 4096) length = 4096;
            for (int i = 0; i < length; i++) {
                items.add(self.read());
            }
            return items;
        }catch (Exception e){

        }
        return null;
    }

}
