package com.zhekasmirnov.innercore.api.nbt;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;

import cn.nukkit.nbt.tag.ByteTag;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.IntTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.LongTag;
import cn.nukkit.nbt.tag.ShortTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.nbt.tag.Tag;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;

public class NativeListTag {
    public final long pointer = 0;
    public final ListTag<Tag> tag;

    public NativeListTag() {
        tag = new ListTag<>();
    }

    public NativeListTag(NativeListTag tag) {
        this(tag != null ? tag.tag : null);
    }

    @SuppressWarnings("unchecked")
    public NativeListTag(ListTag<Tag> tag) {
        this.tag = tag != null ? (ListTag<Tag>) tag.copy() : new ListTag<>();
    }

    @Deprecated(since = "Zote")
    public NativeListTag(long pointer) {
        throw new UnsupportedOperationException("NativeListTag(pointer)");
    }

    public NativeListTag setFinalizable(boolean value) {
        return this;
    }

    public int length() {
        return tag.size();
    }

    public int getValueType(int key) {
        Tag childrenTag = tag.get(key);
        // if (childrenTag == null) {
        //     throw new IndexOutOfBoundsException("NativeListTag.getValueType(key): " + key);
        // }
        return childrenTag != null ? NativeCompoundTag.getNbtTypeFromClass(childrenTag.getClass()) : NbtDataType.TYPE_END_TAG;
    }

    public int getByte(int key) {
        Tag childrenTag = tag.get(key);
        // if (childrenTag == null) {
        //     throw new IndexOutOfBoundsException("NativeListTag.getByte(key): " + key);
        // }
        return childrenTag != null ? (int) childrenTag.parseValue() : 0x0;
    }

    public int getShort(int key) {
        Tag childrenTag = tag.get(key);
        // if (childrenTag == null) {
        //     throw new IndexOutOfBoundsException("NativeListTag.getShort(key): " + key);
        // }
        return childrenTag != null ? (int) childrenTag.parseValue() : 0;
    }

    public int getInt(int key) {
        Tag childrenTag = tag.get(key);
        // if (childrenTag == null) {
        //     throw new IndexOutOfBoundsException("NativeListTag.getInt(key): " + key);
        // }
        return childrenTag != null ? (int) childrenTag.parseValue() : 0;
    }

    public long getInt64(int key) {
        Tag childrenTag = tag.get(key);
        // if (childrenTag == null) {
        //     throw new IndexOutOfBoundsException("NativeListTag.getInt64(key): " + key);
        // }
        return childrenTag != null ? (long) childrenTag.parseValue() : 0L;
    }

    public float getFloat(int key) {
        Tag childrenTag = tag.get(key);
        // if (childrenTag == null) {
        //     throw new IndexOutOfBoundsException("NativeListTag.getFloat(key): " + key);
        // }
        return childrenTag != null ? (float) childrenTag.parseValue() : 0.0f;
    }

    public double getDouble(int key) {
        Tag childrenTag = tag.get(key);
        // if (childrenTag == null) {
        //     throw new IndexOutOfBoundsException("NativeListTag.getDouble(key): " + key);
        // }
        return childrenTag != null ? (double) childrenTag.parseValue() : 0.0d;
    }

    public String getString(int key) {
        Tag childrenTag = tag.get(key);
        // if (childrenTag == null) {
        //     throw new IndexOutOfBoundsException("NativeListTag.getString(key): " + key);
        // }
        return childrenTag != null ? (String) childrenTag.parseValue() : null;
    }

    public NativeCompoundTag getCompoundTagNoClone(int key) {
        CompoundTag childrenTag = (CompoundTag) tag.get(key);
        return childrenTag != null ? new NativeCompoundTag(childrenTag).setFinalizable(false) : null;
    }

    @SuppressWarnings("unchecked")
    public NativeListTag getListTagNoClone(int key) {
        ListTag<Tag> childrenTag = (ListTag<Tag>) tag.get(key);
        return childrenTag != null ? new NativeListTag(childrenTag).setFinalizable(false) : null;
    }

    public NativeCompoundTag getCompoundTag(int key) {
        NativeCompoundTag tag = getCompoundTagNoClone(key);
        return tag != null ? new NativeCompoundTag(tag) : null;
    }

    public NativeListTag getListTag(int key) {
        NativeListTag tag = getListTagNoClone(key);
        return tag != null ? new NativeListTag(tag) : null;
    }

    public void putByte(int key, int value) {
        tag.add(key, new ByteTag(String.valueOf(key), value));
    }

    public void putShort(int key, int value) {
        tag.add(key, new ShortTag(String.valueOf(key), value));
    }

    public void putInt(int key, int value) {
        tag.add(key, new IntTag(String.valueOf(key), value));
    }

    public void putInt64(int key, long value) {
        tag.add(key, new LongTag(String.valueOf(key), value));
    }

    public void putFloat(int key, float value) {
        tag.add(key, new FloatTag(String.valueOf(key), value));
    }

    public void putDouble(int key, double value) {
        tag.add(key, new DoubleTag(String.valueOf(key), value));
    }

    public void putString(int key, String value) {
        tag.add(key, new StringTag(String.valueOf(key), value));
    }

    public void putCompoundTag(int key, NativeCompoundTag childrenTag) {
        if (childrenTag != null) {
            tag.add(key, childrenTag.tag.copy());
        }
    }

    public void putListTag(int key, NativeListTag childrenTag) {
        if (childrenTag != null) {
            tag.add(key, childrenTag.tag.copy());
        }
    }

    public void clear() {
        tag.getAllUnsafe().clear();
    }

    public Scriptable toScriptable() {
        int len = length();
        Object[] result = new Object[len];
        for (int key = 0; key < len; key++) {
            int type = getValueType(key);
            switch (type) {
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
            // Object[] array =((NativeArray) scriptable).toArray();
        }
    }
}
