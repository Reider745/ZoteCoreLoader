package com.reider745.network.request;

public class InventoryConsumeAction extends InventoryTransferAction {

    public InventoryConsumeAction(byte type) {
        super(true, false, type);
    }

    @Override
    public int weight() {
        return 8;
    }
}
