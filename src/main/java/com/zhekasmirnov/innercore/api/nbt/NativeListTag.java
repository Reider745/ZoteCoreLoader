package com.zhekasmirnov.innercore.api.nbt;

import cn.nukkit.nbt.tag.*;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;

public class NativeListTag {
    public final ListTag<Tag> pointer;
    private boolean isFinalizable = true;

    public NativeListTag() {
        pointer = nativeConstruct();
    }

    public NativeListTag(NativeListTag tag) {
        pointer = tag != null ? nativeClone(tag.pointer) : nativeConstruct();
    }

    public NativeListTag(ListTag<Tag> pointer) {
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

    private static ListTag<Tag> nativeConstruct(){
        return new ListTag<>();
    }
    static ListTag<Tag> nativeClone(ListTag<Tag> ptr){
        return (ListTag<Tag>) ptr.copy();
    }
    static void nativeFinalize(ListTag<Tag> ptr){

    }

    public int length(){
        return pointer.size();
    }
    public int getValueType(int key){
        return NativeCompoundTag.getFromClass(pointer.get(key).getClass());
    }
    public int getByte(int key){
        return ((ByteTag) pointer.get(key)).data;
    }
    public int getShort(int key){
        return ((ShortTag) pointer.get(key)).data;
    }
    public int getInt(int key){
        return ((IntTag) pointer.get(key)).data;
    }
    public long getInt64(int key){
        return ((LongTag) pointer.get(key)).data;
    }
    public float getFloat(int key){
        return ((FloatTag) pointer.get(key)).data;
    }
    public double getDouble(int key){
        return ((DoubleTag) pointer.get(key)).data;
    }
    public String getString(int key){
        return ((StringTag) pointer.get(key)).data;
    }
    public NativeCompoundTag getCompoundTagNoClone(int key) {
        CompoundTag ptr = nativeGetCompoundTag(key);
        return ptr != null ? new NativeCompoundTag(ptr).setFinalizable(false) : null;
    }
    public NativeListTag getListTagNoClone(int key) {
        ListTag<Tag> ptr = nativeGetListTag(key);
        return ptr != null ? new NativeListTag(ptr).setFinalizable(false) : null;
    }
    public NativeCompoundTag getCompoundTag(int key) {
        NativeCompoundTag tag = getCompoundTagNoClone(key);
        return tag != null ? new NativeCompoundTag(tag) : null;
    }
    public NativeListTag getListTag(int key) {
        NativeListTag tag = getListTagNoClone(key);
        return tag != null ? new NativeListTag(tag) : null;
    }

    public void putByte(int key, int value){
        pointer.add(key, new ByteTag(String.valueOf(key), value));
    }
    public void putShort(int key, int value){
        pointer.add(key, new ShortTag(String.valueOf(key), value));
    }
    public void putInt(int key, int value){
        pointer.add(key, new IntTag(String.valueOf(key), value));
    }
    public void putInt64(int key, long value){
        pointer.add(key, new LongTag(String.valueOf(key), value));
    }
    public void putFloat(int key, float value){
        pointer.add(key, new FloatTag(String.valueOf(key), value));
    }
    public void putDouble(int key, double value){
        pointer.add(key, new DoubleTag(String.valueOf(key), value));
    }
    public void putString(int key, String value){
        pointer.add(key, new StringTag(String.valueOf(key), value));
    }
    public void putCompoundTag(int key, NativeCompoundTag tag) {
        nativePutTag(key, tag != null ? NativeCompoundTag.nativeClone(tag.pointer) : null);
    }
    public void putListTag(int key, NativeListTag tag) {
        nativePutTag(key, tag != null ? NativeListTag.nativeClone(tag.pointer) : null);
    }
    
    public void clear(){
        pointer.getAllUnsafe().clear();
    }
    
    CompoundTag nativeGetCompoundTag(int key){
        return (CompoundTag) pointer.get(key);
    }
    ListTag<Tag> nativeGetListTag(int key){
        return (ListTag<Tag>) pointer.get(key);
    }
    void nativePutTag(int key, Tag tag){
        pointer.add(key, tag);
    }

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