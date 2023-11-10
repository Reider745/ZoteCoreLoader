package com.reider745.network.request;

public class InventoryMoveAction extends InventoryTransferAction {

    public InventoryMoveAction(byte type) {
        super(true, true, type);
    }
}
