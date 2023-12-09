package com.zhekasmirnov.innercore.api;

import cn.nukkit.item.Item;
import com.reider745.hooks.ItemUtils;

/**
 * Created by zheka on 26.07.2017.
 */

public class NativeItemInstance {
    public Item item;
    public int id, count, data;
    public NativeItemInstanceExtra extra;

    public boolean isValid = false;

    public NativeItemInstance(Item ptr) {
        if (ptr == null) {
            item = Item.get(0).clone();
            this.id = this.count = this.data = 0;
            this.extra = null;
        } else {
            item = ptr;
            this.id = ptr.getId();
            this.count = ptr.getCount();
            this.data = ptr.getAttackDamage() == 0 ? ptr.getAttackDamage() : ptr.getDamage();
            if(id == 351 && data == 15){//bone_meal fix
                id = 858;
                data = 0;
            }else if(id == 325 && data == 10){//lava bucket fix
                id = 843;
                data = 0;
            }else if(id == 325 && data == 8){//water bucket fix
                id = 850;
                data = 0;
            }else if(id == 325 && data == 1){//milk bucket fix
                id = 876;
                data = 0;
            }

            this.extra = ItemUtils.getItemInstanceExtra(ptr);
        }

        isValid = true;
    }

    public NativeItemInstance(int id, int count, int data) {
        this.item = createItemInstanceData(id, count, data);
        this.id = id;
        this.count = count;
        this.data = data;

        isValid = true;
    }

    //public long getPointer() {
        //setItemInstance(pointer, id, count, data);
        //return pointer;
    //}

    public void destroy() {
        isValid = false;
        // destroy(pointer);
    }

    public String toString() {
        return "[item=" + id + "," + count + "," + data + "]";
    }


    public static Item createItemInstanceData(int id, int count, int data){
        return Item.get(id, count, data);
    }

    public static native int setItemInstance(long ptr, int id, int count, int data);
    public static native int destroy(long ptr);
    public static long getExtra(Item ptr){
        if(ptr == null) return 0;
        NativeItemInstanceExtra extra1 = ItemUtils.getItemInstanceExtra(ptr);
        return extra1 != null ? extra1.getPtr() : 0;
    }
    public static native void setExtra(long ptr, int ench);

}
