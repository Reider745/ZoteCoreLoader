package com.reider745.network.request;

/**
 * @author geNAZt
 */
public class InventoryDestroyCreativeAction extends InventoryTransferAction {

    public InventoryDestroyCreativeAction(byte type) {
        super(true, false, type);
    }

}
