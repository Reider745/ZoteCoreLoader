package com.zhekasmirnov.innercore.api.nbt;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class NativeCompoundTag {
    public final long pointer;
    private boolean isFinalizable = true;

    public NativeCompoundTag() {
        pointer = nativeConstruct();
    }

    public NativeCompoundTag(NativeCompoundTag tag) {
        pointer = tag != null ? nativeClone(tag.pointer) : nativeConstruct();
    }

    public NativeCompoundTag(long pointer) {
        this.pointer = pointer;
    }

    public NativeCompoundTag setFinalizable(boolean value) {
        isFinalizable = value;
        return this;
    }

    @Override
    protected void finalize()  {
        if (isFinalizable) {
            nativeFinalize(pointer);
        }
    }

    private static native long nativeConstruct();
    static native long nativeClone(long ptr);
    static native void nativeFinalize(long ptr);

    public native String[] getAllKeys();
    public native boolean contains(String key);
    public native boolean containsValueOfType(String key, int type);
    public native int getValueType(String key);

    public native int getByte(String key);
    public native int getShort(String key);
    public native int getInt(String key);
    public native long getInt64(String key);
    public native float getFloat(String key);
    public native double getDouble(String key);
    public native String getString(String key);

    public NativeCompoundTag getCompoundTagNoClone(String key) {
        long ptr = nativeGetCompoundTag(key);
        return ptr != 0 ? new NativeCompoundTag(ptr).setFinalizable(false) : null;
    }
    public NativeListTag getListTagNoClone(String key) {
        long ptr = nativeGetListTag(key);
        return ptr != 0 ? new NativeListTag(ptr).setFinalizable(false) : null;
    }
    public NativeCompoundTag getCompoundTag(String key) {
        NativeCompoundTag tag = getCompoundTagNoClone(key);
        return tag != null ? new NativeCompoundTag(tag) : null;
    }
    public NativeListTag getListTag(String key) {
        NativeListTag tag = getListTagNoClone(key);
        return tag != null ? new NativeListTag(tag) : null;
    }

    public native void putByte(String key, int value);
    public native void putShort(String key, int value);
    public native void putInt(String key, int value);
    public native void putInt64(String key, long value);
    public native void putFloat(String key, float value);
    public native void putDouble(String key, double value);
    public native void putString(String key, String value);
    public void putCompoundTag(String key, NativeCompoundTag tag) {
        nativePutTag(key, tag != null ? NativeCompoundTag.nativeClone(tag.pointer) : 0);
    }
    public void putListTag(String key, NativeListTag tag) {
        nativePutTag(key, tag != null ? NativeListTag.nativeClone(tag.pointer) : 0);
    }

    public native void remove(String key);
    public native void clear();
    
    native long nativeGetCompoundTag(String key);
    native long nativeGetListTag(String key);
    native void nativePutTag(String key, long tag);


    public Scriptable toScriptable() {
        ScriptableObject result = ScriptableObjectHelper.createEmpty();
        
        String[] keys = getAllKeys();
        if (keys != null) {
            for (String key : keys) {
                int type = getValueType(key);
                switch(type) {
                    case NbtDataType.TYPE_BYTE:
                        result.put(key, result, getByte(key));
                        break;
                    case NbtDataType.TYPE_SHORT:
                        result.put(key, result, getShort(key));
                        break;
                    case NbtDataType.TYPE_INT:
                        result.put(key, result, getInt(key));
                        break;
                    case NbtDataType.TYPE_INT64:
                        result.put(key, result, getInt64(key));
                        break;
                    case NbtDataType.TYPE_FLOAT:
                        result.put(key, result, getFloat(key));
                        break;
                    case NbtDataType.TYPE_DOUBLE:
                        result.put(key, result, getDouble(key));
                        break;
                    case NbtDataType.TYPE_STRING:
                        result.put(key, result, getString(key));
                        break;
                    case NbtDataType.TYPE_LIST:
                        NativeListTag listTag = getListTagNoClone(key);
                        result.put(key, result, listTag != null ? listTag.toScriptable() : null);
                        break;
                    case NbtDataType.TYPE_COMPOUND:
                        NativeCompoundTag compoundTag = getCompoundTagNoClone(key);
                        result.put(key, result, compoundTag != null ? compoundTag.toScriptable() : null);
                        break;
                    case NbtDataType.TYPE_BYTE_ARRAY:
                        result.put(key, result, "UNSUPPORTED:TYPE_BYTE_ARRAY");
                        break;
                    case NbtDataType.TYPE_INT_ARRAY:
                        result.put(key, result, "UNSUPPORTED:INT_ARRAY");
                        break;
                }
            }
        }
        return result;
    }
}