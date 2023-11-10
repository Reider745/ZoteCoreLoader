package com.reider745.network.request;

import cn.nukkit.network.protocol.DataPacket;

import java.util.ArrayList;

public abstract class InventoryActionRequest {
    public void deserialize(DataPacket packet) throws Exception {};
    public abstract int weight();

    interface IRead<T> {
        T read(int protocol);
    }

    public byte type;

    public InventoryActionRequest(byte type){
        this.type = type;
    }
    public <T> ArrayList<T> readVectorList(DataPacket packet, IRead<T> self){
        try {
            int protocol = packet.protocol;
            ArrayList<T> items = new ArrayList<>();
            long length = packet.getUnsignedVarInt();
            if (length > 4096) length = 4096;
            for (int i = 0; i < length; i++) {
                items.add(self.read(protocol));
            }
            return items;
        }catch (Exception e){

        }
        return null;
    }

}
