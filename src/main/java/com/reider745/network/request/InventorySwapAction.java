package com.reider745.network.request;

public class InventorySwapAction extends InventoryTransferAction {
    public InventorySwapAction(byte type) {
        super(false, true, type);
    }
}
