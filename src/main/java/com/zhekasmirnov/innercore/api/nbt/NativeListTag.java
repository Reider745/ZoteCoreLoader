package com.zhekasmirnov.innercore.api.nbt;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;

public class NativeListTag {
    public final long pointer;
    private boolean isFinalizable = true;

    public NativeListTag() {
        pointer = nativeConstruct();
    }

    public NativeListTag(NativeListTag tag) {
        pointer = tag != null ? nativeClone(tag.pointer) : nativeConstruct();
    }

    public NativeListTag(long pointer) {
        this.pointer = pointer;
    }

    public NativeListTag setFinalizable(boolean value) {
        isFinalizable = value;
        return this;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (isFinalizable) {
            nativeFinalize(pointer);
        }
    }

    private static native long nativeConstruct();
    static native long nativeClone(long ptr);
    static native void nativeFinalize(long ptr);

    public native int length();
    public native int getValueType(int key);
    public native int getByte(int key);
    public native int getShort(int key);
    public native int getInt(int key);
    public native long getInt64(int key);
    public native float getFloat(int key);
    public native double getDouble(int key);
    public native String getString(int key);
    public NativeCompoundTag getCompoundTagNoClone(int key) {
        long ptr = nativeGetCompoundTag(key);
        return ptr != 0 ? new NativeCompoundTag(ptr).setFinalizable(false) : null;
    }
    public NativeListTag getListTagNoClone(int key) {
        long ptr = nativeGetListTag(key);
        return ptr != 0 ? new NativeListTag(ptr).setFinalizable(false) : null;
    }
    public NativeCompoundTag getCompoundTag(int key) {
        NativeCompoundTag tag = getCompoundTagNoClone(key);
        return tag != null ? new NativeCompoundTag(tag) : null;
    }
    public NativeListTag getListTag(int key) {
        NativeListTag tag = getListTagNoClone(key);
        return tag != null ? new NativeListTag(tag) : null;
    }

    public native void putByte(int key, int value);
    public native void putShort(int key, int value);
    public native void putInt(int key, int value);
    public native void putInt64(int key, long value);
    public native void putFloat(int key, float value);
    public native void putDouble(int key, double value);
    public native void putString(int key, String value);
    public void putCompoundTag(int key, NativeCompoundTag tag) {
        nativePutTag(key, tag != null ? NativeCompoundTag.nativeClone(tag.pointer) : 0);
    }
    public void putListTag(int key, NativeListTag tag) {
        nativePutTag(key, tag != null ? NativeListTag.nativeClone(tag.pointer) : 0);
    }
    
    public native void clear();
    
    native long nativeGetCompoundTag(int key);
    native long nativeGetListTag(int key);
    native void nativePutTag(int key, long tag);


    public Scriptable toScriptable() {
        int len = length();
        Object[] result = new Object[len];
        for (int key = 0; key < len; key++) {
            int type = getValueType(key);
            switch(type) {
                case NbtDataType.TYPE_BYTE:
                    result[key] = (getByte(key));
                    break;
                case NbtDataType.TYPE_SHORT:
                    result[key] = (getShort(key));
                    break;
                case NbtDataType.TYPE_INT:
                    result[key] = (getInt(key));
                    break;
                case NbtDataType.TYPE_INT64:
                    result[key] = (getInt64(key));
                    break;
                case NbtDataType.TYPE_FLOAT:
                    result[key] = (getFloat(key));
                    break;
                case NbtDataType.TYPE_DOUBLE:
                    result[key] = (getDouble(key));
                    break;
                case NbtDataType.TYPE_STRING:
                    result[key] = (getString(key));
                    break;
                case NbtDataType.TYPE_LIST:
                    NativeListTag listTag = getListTagNoClone(key);
                    result[key] = (listTag != null ? listTag.toScriptable() : null);
                    break;
                case NbtDataType.TYPE_COMPOUND:
                    NativeCompoundTag compoundTag = getCompoundTagNoClone(key);
                    result[key] = (compoundTag != null ? compoundTag.toScriptable() : null);
                    break;
                case NbtDataType.TYPE_BYTE_ARRAY:
                    result[key] = ("UNSUPPORTED:TYPE_BYTE_ARRAY");
                    break;
                case NbtDataType.TYPE_INT_ARRAY:
                    result[key] = ("UNSUPPORTED:INT_ARRAY");
                    break;
            }
        }
    
        return ScriptableObjectHelper.createArray(result);
    }

    public void fromScriptable(Scriptable scriptable) {
        if (scriptable instanceof NativeArray) {
            Object[] array =((NativeArray) scriptable).toArray();
            
        }
    }
}