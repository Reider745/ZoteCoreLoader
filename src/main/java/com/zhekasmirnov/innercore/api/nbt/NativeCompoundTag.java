package com.zhekasmirnov.innercore.api.nbt;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;

import cn.nukkit.nbt.tag.ByteArrayTag;
import cn.nukkit.nbt.tag.ByteTag;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.IntArrayTag;
import cn.nukkit.nbt.tag.IntTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.LongTag;
import cn.nukkit.nbt.tag.ShortTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.nbt.tag.Tag;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class NativeCompoundTag {
    public final long pointer = 0;
    public final CompoundTag tag;

    public NativeCompoundTag() {
        tag = new CompoundTag();
    }

    public NativeCompoundTag(NativeCompoundTag tag) {
        this(tag != null ? tag.tag : null);
    }

    public NativeCompoundTag(CompoundTag tag) {
        this.tag = tag != null ? tag.copy() : new CompoundTag();
    }

    public NativeCompoundTag(long pointer) {
        throw new UnsupportedOperationException("NativeCompoundTag(pointer)");
    }

    public NativeCompoundTag setFinalizable(boolean value) {
        return this;
    }

    public String[] getAllKeys() {
        return tag.getTags().keySet().toArray(new String[0]);
    }

    public boolean contains(String key) {
        return tag.contains(key);
    }

    public boolean containsValueOfType(String key, int type) {
        return tag.contains(key, getClassFromNbtType(type));
    }

    public int getValueType(String key) {
        Tag childrenTag = tag.get(key);
        if (childrenTag == null) {
            throw new IndexOutOfBoundsException("NativeCompoundTag.getValueType(key): " + key);
        }
        return getNbtTypeFromClass(childrenTag.getClass());
    }

    public int getByte(String key) {
        Tag childrenTag = tag.get(key);
        if (childrenTag == null) {
            throw new IndexOutOfBoundsException("NativeCompoundTag.getByte(key): " + key);
        }
        return (int) tag.get(key).parseValue();
    }

    public int getShort(String key) {
        Tag childrenTag = tag.get(key);
        if (childrenTag == null) {
            throw new IndexOutOfBoundsException("NativeCompoundTag.getShort(key): " + key);
        }
        return (int) tag.get(key).parseValue();
    }

    public int getInt(String key) {
        Tag childrenTag = tag.get(key);
        if (childrenTag == null) {
            throw new IndexOutOfBoundsException("NativeCompoundTag.getInt(key): " + key);
        }
        return (int) tag.get(key).parseValue();
    }

    public long getInt64(String key) {
        Tag childrenTag = tag.get(key);
        if (childrenTag == null) {
            throw new IndexOutOfBoundsException("NativeCompoundTag.getInt64(key): " + key);
        }
        return (long) tag.get(key).parseValue();
    }

    public float getFloat(String key) {
        Tag childrenTag = tag.get(key);
        if (childrenTag == null) {
            throw new IndexOutOfBoundsException("NativeCompoundTag.getFloat(key): " + key);
        }
        return (float) tag.get(key).parseValue();
    }

    public double getDouble(String key) {
        Tag childrenTag = tag.get(key);
        if (childrenTag == null) {
            throw new IndexOutOfBoundsException("NativeCompoundTag.getFloat(key): " + key);
        }
        return (double) tag.get(key).parseValue();
    }

    public String getString(String key) {
        Tag childrenTag = tag.get(key);
        if (childrenTag == null) {
            throw new IndexOutOfBoundsException("NativeCompoundTag.getFloat(key): " + key);
        }
        return (String) tag.get(key).parseValue();
    }

    public NativeCompoundTag getCompoundTagNoClone(String key) {
        CompoundTag childrenTag = (CompoundTag) tag.get(key);
        return childrenTag != null ? new NativeCompoundTag(childrenTag).setFinalizable(false) : null;
    }

    @SuppressWarnings("unchecked")
    public NativeListTag getListTagNoClone(String key) {
        ListTag<Tag> childrenTag = (ListTag<Tag>) tag.get(key);
        return childrenTag != null ? new NativeListTag(childrenTag).setFinalizable(false) : null;
    }

    public NativeCompoundTag getCompoundTag(String key) {
        NativeCompoundTag tag = getCompoundTagNoClone(key);
        return tag != null ? new NativeCompoundTag(tag) : null;
    }

    public NativeListTag getListTag(String key) {
        NativeListTag tag = getListTagNoClone(key);
        return tag != null ? new NativeListTag(tag) : null;
    }

    public void putByte(String key, int value) {
        tag.putByte(key, value);
    }

    public void putShort(String key, int value) {
        tag.putShort(key, value);
    }

    public void putInt(String key, int value) {
        tag.putInt(key, value);
    }

    public void putInt64(String key, long value) {
        tag.putLong(key, value);
    }

    public void putFloat(String key, float value) {
        tag.putFloat(key, value);
    }

    public void putDouble(String key, double value) {
        tag.putDouble(key, value);
    }

    public void putString(String key, String value) {
        tag.putString(key, value);
    }

    public void putCompoundTag(String key, NativeCompoundTag childrenTag) {
        if (childrenTag != null) {
            tag.put(key, childrenTag.tag.copy());
        }
    }

    public void putListTag(String key, NativeListTag childrenTag) {
        if (childrenTag != null) {
            tag.put(key, childrenTag.tag.copy());
        }
    }

    public void remove(String key) {
        tag.remove(key);
    }

    public void clear() {
        for (String key : getAllKeys()) {
            tag.remove(key);
        }
    }

    public Scriptable toScriptable() {
        ScriptableObject result = ScriptableObjectHelper.createEmpty();

        String[] keys = getAllKeys();
        if (keys != null) {
            for (String key : keys) {
                int type = getValueType(key);
                switch (type) {
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

    public static int getNbtTypeFromClass(Class<? extends Tag> type) {
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
        throw new RuntimeException("Unknown tag type: " + type);
    }

    public static Class<? extends Tag> getClassFromNbtType(int type) {
        return switch (type) {
            case NbtDataType.TYPE_BYTE -> ByteTag.class;
            case NbtDataType.TYPE_SHORT -> ShortTag.class;
            case NbtDataType.TYPE_INT -> IntTag.class;
            case NbtDataType.TYPE_INT64 -> LongTag.class;
            case NbtDataType.TYPE_FLOAT -> FloatTag.class;
            case NbtDataType.TYPE_DOUBLE -> DoubleTag.class;
            case NbtDataType.TYPE_STRING -> StringTag.class;
            case NbtDataType.TYPE_LIST -> ListTag.class;
            case NbtDataType.TYPE_COMPOUND -> CompoundTag.class;
            case NbtDataType.TYPE_BYTE_ARRAY -> ByteArrayTag.class;
            case NbtDataType.TYPE_INT_ARRAY -> IntArrayTag.class;
            default -> throw new RuntimeException("Unknown tag type: " + type);
        };
    }
}
