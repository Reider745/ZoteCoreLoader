package com.zhekasmirnov.innercore.api.nbt;

import cn.nukkit.nbt.tag.*;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class NativeCompoundTag {
    public final CompoundTag pointer;
    private boolean isFinalizable = true;

    public NativeCompoundTag() {
        pointer = nativeConstruct();
    }

    public NativeCompoundTag(NativeCompoundTag tag) {
        pointer = tag != null ? nativeClone(tag.pointer) : nativeConstruct();
    }

    public NativeCompoundTag(CompoundTag pointer) {
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

    private static CompoundTag nativeConstruct(){
        return new CompoundTag();
    }
    public static CompoundTag nativeClone(CompoundTag ptr){
        return ptr.clone();
    }
    private static void nativeFinalize(CompoundTag ptr){

    }

    public String[] getAllKeys(){
        Tag[] tags = pointer.getAllTags().toArray(new Tag[]{});
        String[] strings = new String[tags.length];
        for (int i = 0; i < tags.length; i++)
            strings[i] = tags[i].getName();
        return strings;
    }
    public boolean contains(String key){
        return pointer.contains(key);
    }

    public static Class<? extends  Tag> getFromType(int type){
        switch(type) {
            case NbtDataType.TYPE_BYTE:
                return ByteTag.class;
            case NbtDataType.TYPE_SHORT:
                return ShortTag.class;
            case NbtDataType.TYPE_INT:
                return IntTag.class;
            case NbtDataType.TYPE_INT64:
                return LongTag.class;
            case NbtDataType.TYPE_FLOAT:
                return FloatTag.class;
            case NbtDataType.TYPE_DOUBLE:
                return DoubleTag.class;
            case NbtDataType.TYPE_STRING:
                return StringTag.class;
            case NbtDataType.TYPE_LIST:
                return ListTag.class;
            case NbtDataType.TYPE_COMPOUND:
                return CompoundTag.class;
            case NbtDataType.TYPE_BYTE_ARRAY:
                return ByteArrayTag.class;
            case NbtDataType.TYPE_INT_ARRAY:
                return IntArrayTag.class;
        }
        throw new RuntimeException("Unknown type: "+type);
    }

    public static int getFromClass(Class<? extends  Tag> type){
        if (type.equals(ByteTag.class)) {
            return NbtDataType.TYPE_BYTE;
        } else if (type.equals(ShortTag.class)) {
            return NbtDataType.TYPE_SHORT;
        } else if (type.equals(IntTag.class)) {
            return NbtDataType.TYPE_INT;
        } else if (type.equals(LongTag.class)) {
            return NbtDataType.TYPE_INT64;
        } else if (type.equals(FloatTag.class)) {
            return NbtDataType.TYPE_FLOAT;
        } else if (type.equals(DoubleTag.class)) {
            return NbtDataType.TYPE_DOUBLE;
        } else if (type.equals(StringTag.class)) {
            return NbtDataType.TYPE_STRING;
        } else if (type.equals(ListTag.class)) {
            return NbtDataType.TYPE_LIST;
        } else if (type.equals(CompoundTag.class)) {
            return NbtDataType.TYPE_COMPOUND;
        } else if (type.equals(ByteArrayTag.class)) {
            return NbtDataType.TYPE_BYTE_ARRAY;
        } else if (type.equals(IntArrayTag.class)) {
            return NbtDataType.TYPE_INT_ARRAY;
        }
        throw new RuntimeException("Unknown type: "+type);
    }

    public boolean containsValueOfType(String key, int type){
        return pointer.contains(key, getFromType(type));
    }
    public int getValueType(String key){
        return getFromClass(pointer.get(key).getClass());
    }

    public int getByte(String key){
        return pointer.getByte(key);
    }
    public int getShort(String key){
        return pointer.getShort(key);
    }
    public int getInt(String key){
        return pointer.getInt(key);
    }
    public long getInt64(String key){
        return pointer.getLong(key);
    }
    public float getFloat(String key){
        return pointer.getFloat(key);
    }
    public double getDouble(String key){
        return pointer.getDouble(key);
    }
    public String getString(String key){
        return pointer.getString(key);
    }

    public NativeCompoundTag getCompoundTagNoClone(String key) {
        CompoundTag ptr = nativeGetCompoundTag(key);
        return ptr != null ? new NativeCompoundTag(ptr).setFinalizable(false) : null;
    }
    public NativeListTag getListTagNoClone(String key) {
        ListTag<Tag> ptr = nativeGetListTag(key);
        return ptr != null ? new NativeListTag(ptr).setFinalizable(false) : null;
    }
    public NativeCompoundTag getCompoundTag(String key) {
        NativeCompoundTag tag = getCompoundTagNoClone(key);
        return tag != null ? new NativeCompoundTag(tag) : null;
    }
    public NativeListTag getListTag(String key) {
        NativeListTag tag = getListTagNoClone(key);
        return tag != null ? new NativeListTag(tag) : null;
    }

    public void putByte(String key, int value){
        pointer.putByte(key, value);
    }
    public void putShort(String key, int value){
        pointer.putShort(key, value);
    }
    public void putInt(String key, int value){
        pointer.putInt(key, value);
    }
    public void putInt64(String key, long value){
        pointer.putLong(key, value);
    }
    public void putFloat(String key, float value){
        pointer.putFloat(key, value);
    }
    public void putDouble(String key, double value){
        pointer.putDouble(key, value);
    }
    public void putString(String key, String value){
        pointer.putString(key, value);
    }
    public void putCompoundTag(String key, NativeCompoundTag tag) {
        nativePutTag(key, tag != null ? NativeCompoundTag.nativeClone(tag.pointer) : null);
    }
    public void putListTag(String key, NativeListTag tag) {
        nativePutTag(key, tag != null ? NativeListTag.nativeClone(tag.pointer) : null);
    }

    public void remove(String key){
        pointer.remove(key);
    }
    public void clear(){
        String[] keys = getAllKeys();
        for(String key : keys)
            pointer.remove(key);
    }
    
    CompoundTag nativeGetCompoundTag(String key){
        return pointer.getCompound(key);
    }
    ListTag<Tag> nativeGetListTag(String key){
        return (ListTag<Tag>) pointer.getList(key);
    }
    void nativePutTag(String key, Tag tag){
        pointer.put(key, tag);
    }


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