package com.reider745.hooks;

import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.nbt.tag.CompoundTag;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.pointers.PointerClassAdditionalValue;
import com.reider745.api.pointers.PointersStorage;
import com.reider745.item.ItemExtraDataProvider;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import org.json.JSONObject;

@Hooks(class_name = "cn.nukkit.item.Item")
public class ItemUtils implements HookClass {
    public static PointersStorage<Item> items_pointers;

    @Inject(signature = "(ILjava/lang/Integer;I[B)Lcn/nukkit/item/Item;")
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
            //items_pointers.addPointer(item);
            return item;
        } catch (Exception e) {
            Item item = new Item(id, meta, count).setCompoundTag(tags);
            //items_pointers.addPointer(item);
            return item;
        }
    }

    @Inject
    public static void init(){
        items_pointers = new PointersStorage<>("items", ItemExtraDataProvider::new);
    }

    public static final String INNER_CORE_TAG_NAME = "$mod";


    public static Item get(int id, int count, int meta, NativeItemInstanceExtra extra){
        Item item = Item.get(id, meta, count, new byte[] {});
        if(extra != null) {
            CompoundTag tag = item.getOrCreateNamedTag();

            JSONObject custom = extra.getCustomDataJSON();
            if(custom != null)
                tag.putString(INNER_CORE_TAG_NAME, custom.toString());

            item.setCompoundTag(tag);

            ItemExtraDataProvider provider = (ItemExtraDataProvider) items_pointers.getInstance(extra.getPtr());
            provider.apply(item);
            items_pointers.replace(items_pointers.getPointerForInstance(item), provider);
        }
        return item;
    }

    public static Item get(int id, int count, int meta, long extra){
        ItemExtraDataProvider provider = (ItemExtraDataProvider) items_pointers.getInstance(extra);
        return get(id, count, meta, provider == null ? null : provider.extra);
    }

    public static NativeItemInstanceExtra getItemInstanceExtra(Item item){
        CompoundTag tag = item.getOrCreateNamedTag();
        String custom = tag.getString(INNER_CORE_TAG_NAME);
        if(!custom.equals("")){
            NativeItemInstanceExtra extra = new NativeItemInstanceExtra();
            extra.setAllCustomData(custom);
            return extra;
        }
        return null;
    }
}
