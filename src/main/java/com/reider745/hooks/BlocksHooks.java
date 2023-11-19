package com.reider745.hooks;

import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import com.reider745.api.ReflectHelper;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import javassist.*;

@Hooks(class_name = "cn.nukkit.block.Block")
public class BlocksHooks implements HookClass {
    private static final int MAX_ID = 16000;

    @Override
    public void rebuildField(CtClass ctClass, CtField field) {
        String name = field.getName();
        if(name.equals("MAX_BLOCK_ID") || name.equals("usesFakeWater"))
            field.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
    }

    @Inject
    public static void init(){
        ReflectHelper.setField(Block.class, "MAX_BLOCK_ID", 16000);
        ReflectHelper.setField(Block.class, "usesFakeWater", new boolean[MAX_ID]);
    }

    @Inject(class_name = "cn.nukkit.item.Item", signature = "(ILjava/lang/Integer;I[B)Lcn/nukkit/item/Item;")
    public static Item get(int id, Integer meta, int count, byte[] tags){
        try {
            Class c = Item.list[id];
            Item item;

            if (c == null) {
                item = new Item(id, meta, count);
            } else if (id < 256 || id > 8000){
                if (meta != null) {
                    item = new ItemBlock(Block.get(id, meta), meta, count);
                } else {
                    item = new ItemBlock(Block.get(id), meta, count);
                }
            }else{
                if(id > 2000)
                    item = ((Item) c.getConstructor(int.class, Integer.class, int.class).newInstance(id, meta, count));
                else
                    item = ((Item) c.getConstructor(Integer.class, int.class).newInstance(meta, count));
            }

            if (tags.length != 0) {
                item.setCompoundTag(tags);
            }

            return item;
        } catch (Exception e) {
            return new Item(id, meta, count).setCompoundTag(tags);
        }
    }
}
