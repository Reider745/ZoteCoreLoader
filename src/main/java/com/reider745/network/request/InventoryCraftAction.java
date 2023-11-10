package com.reider745.network.request;

import cn.nukkit.network.protocol.DataPacket;

/**
 * @author geNAZt
 */
public class InventoryCraftAction extends InventoryActionRequest {

    private long recipeId;

    public InventoryCraftAction(byte type) {
        super(type);
    }

    @Override
    public void deserialize(DataPacket packet) throws Exception {
        this.recipeId = packet.getUnsignedVarInt();
    }

    @Override
    public int weight() {
        return 10;
    }

    public long getRecipeId() {
        return recipeId;
    }

    @Override
    public String toString() {
        return "InventoryCraftAction{" +
                "recipeId=" + recipeId +
                '}';
    }
}
