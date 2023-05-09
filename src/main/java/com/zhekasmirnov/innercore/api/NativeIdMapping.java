package com.zhekasmirnov.innercore.api;

public class NativeIdMapping {

    public static native int getItemNumericId(String stringId);

    public static native String getItemStringId(int numericId);

    public interface IIdIterator {
        void onIdDataIterated(int id, int data);
    }

    public static void iterateMetadata(int id, Object data, IIdIterator iterator) {
        if (data instanceof Number && ((Number) data).intValue() != -1) {
            iterator.onIdDataIterated(id, ((Number) data).intValue());
        }
        else {
            for (int i = 0; i < getMaxData(); i++) {
                iterator.onIdDataIterated(id, i);
            }
        }
    }

    public static int getMaxData(){
        return 16;
    }
}