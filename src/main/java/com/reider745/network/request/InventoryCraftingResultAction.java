package com.reider745.network.request;

import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.DataPacket;

import java.util.ArrayList;


public class InventoryCraftingResultAction extends InventoryActionRequest {

    private ArrayList<Item> resultItems;
    private int amount;

    public InventoryCraftingResultAction(byte type) {
        super(type);
    }

    @Override
    public void deserialize(DataPacket packet) throws Exception {
        this.resultItems = readVectorList(packet, packet::getSlot);
        this.amount = packet.getByte();
    }

    @Override
    public int weight() {
        return 9;
    }

    @Override
    public String toString() {
        return "InventoryCraftingResultAction{" +
                "resultItems=" + resultItems +
                ", amount=" + amount +
                '}';
    }

    public ArrayList<Item> getResultItems() {
        return resultItems;
    }

    public int getAmount() {
        return amount;
    }

}
