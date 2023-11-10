package com.reider745.network.request;

public class InventoryPlaceAction extends InventoryTransferAction {

    public InventoryPlaceAction(byte type) {
        super(true, true, type);
    }

    @Override
    public int weight() {
        return 2;
    }
}
