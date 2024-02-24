package com.zhekasmirnov.innercore.api;

import com.zhekasmirnov.innercore.api.unlimited.IDRegistry;

public class NativeIdMapping {

    public static int getItemNumericId(String stringId) {
        int id = IDRegistry.getIdByNameId(stringId);
        return id != 0 ? id : IDRegistry.getIDByName(stringId);
    }

    public static String getItemStringId(int numericId) {
        return IDRegistry.getStringIdForItemId(numericId);
    }

    public interface IIdIterator {
        void onIdDataIterated(int id, int data);
    }

    public static void iterateMetadata(int id, Object data, IIdIterator iterator) {
        if (data instanceof Number && ((Number) data).intValue() != -1) {
            iterator.onIdDataIterated(id, ((Number) data).intValue());
        } else {
            for (int i = 0; i < getMaxData(); i++) {
                iterator.onIdDataIterated(id, i);
            }
        }
    }

    public static int getMaxData() {
        return 16;
    }
}
