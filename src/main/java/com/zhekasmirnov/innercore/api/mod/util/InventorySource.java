package com.zhekasmirnov.innercore.api.mod.util;

import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 01.08.2017.
 */

@Deprecated(since = "Zote")
public class InventorySource {
    public static boolean isUpdating = false;

    private static final ScriptableObject[] slots = new ScriptableObject[36];

    public static void tick() {
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
        ScriptableObject slot = getSource(slotId);
        slot.put("id", slot, id);
        slot.put("count", slot, count);
        slot.put("data", slot, data);
        slot.put("extra", slot, extra);
    }
}
