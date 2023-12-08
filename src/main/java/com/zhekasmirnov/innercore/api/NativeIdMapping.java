package com.zhekasmirnov.innercore.api;

import com.reider745.InnerCoreServer;

public class NativeIdMapping {
    public static int getItemNumericId(String stringId) {
        InnerCoreServer.useNotCurrentSupport("NativeIdMapping.getItemNumericId(stringId)");
        return 0;
    }

    public static String getItemStringId(int numericId) {
        InnerCoreServer.useNotCurrentSupport("NativeIdMapping.getItemStringId(numericId)");
        return "";
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
