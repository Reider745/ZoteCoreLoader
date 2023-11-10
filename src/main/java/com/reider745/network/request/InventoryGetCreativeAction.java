package com.reider745.network.request;

import cn.nukkit.network.protocol.DataPacket;

/**
 * @author geNAZt
 */
public class InventoryGetCreativeAction extends InventoryActionRequest {

    private long creativeItemId;

    public InventoryGetCreativeAction(byte type) {
        super(type);
    }

    @Override
    public void deserialize(DataPacket packet) throws Exception {
        this.creativeItemId = packet.getUnsignedVarInt();
    }

    @Override
    public int weight() {
        return 11;
    }

    public long getCreativeItemId() {
        return creativeItemId;
    }

    @Override
    public String toString() {
        return "InventoryGetCreativeAction{" +
                "creativeItemId=" + creativeItemId +
                '}';
    }
}
