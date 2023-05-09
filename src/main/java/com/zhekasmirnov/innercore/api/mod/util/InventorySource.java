package com.zhekasmirnov.innercore.api.mod.util;

import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.NativeItemInstance;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 01.08.2017.
 */

public class InventorySource {
    public static boolean isUpdating = false;

    private static final ScriptableObject[] slots = new ScriptableObject[36];

    public static void tick() {
        if (isUpdating)
            for (int i = 0; i < 36; i++) {
                NativeItemInstance itemInstance = new NativeItemInstance(NativeAPI.getInventorySlot(i));
                ScriptableObject slot = getSource(i);
                slot.put("id", slot, itemInstance.id);
                slot.put("count", slot, itemInstance.count);
                slot.put("data", slot, itemInstance.data);
                slot.put("extra", slot, itemInstance.extra);
            }
    }

    public static ScriptableObject getSource(int slotId) {
        slotId %= 36;
        if (slots[slotId] == null) {
            ScriptableObject slot = new ScriptableObject() {
                @Override
                public String getClassName() {
                    return "slot";
                }
            };

            slot.put("id", slot, 0);
            slot.put("count", slot, 0);
            slot.put("data", slot, 0);
            slot.put("extra", slot, null);

            slots[slotId] = slot;
        }

        return slots[slotId];
    }

    public static void setSource(int slotId, int id, int count, int data, NativeItemInstanceExtra extra) {
        NativeAPI.setInventorySlot(slotId, id, count, data, NativeItemInstanceExtra.getValueOrNullPtr(extra));
        ScriptableObject slot = getSource(slotId);
        slot.put("id", slot, id);
        slot.put("count", slot, count);
        slot.put("data", slot, data);
        slot.put("extra", slot, extra);
    }
}
