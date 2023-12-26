package com.reider745.hooks;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.inventory.transaction.RepairItemTransaction;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.RuntimeItems;
import cn.nukkit.nbt.tag.CompoundTag;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.HookController;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.pointers.PointersStorage;
import com.reider745.item.CustomItem;
import com.reider745.item.ItemExtraDataProvider;
import com.reider745.item.NukkitIdConvertor;
import com.reider745.item.Repairs;
import com.zhekasmirnov.innercore.api.NativeFurnaceRegistry;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.unlimited.IDRegistry;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.OptionalInt;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Hooks(className = "cn.nukkit.item.Item")
public class ItemUtils implements HookClass {
    public static final PointersStorage<Item> items_pointers = new PointersStorage<>("items",
            ItemExtraDataProvider::new);

    @Inject(signature = "(ILjava/lang/Integer;I[B)Lcn/nukkit/item/Item;")
    public static Item get(int id, Integer meta, int count, byte[] tags) {
        try {
            Class<?> c = null;
            if (id < 0) {
                int blockId = 255 - id;
                c = Block.list[blockId];
            } else {
                c = Item.list[id];
            }

            Item item;
            if (c == null) {
                item = new Item(id, meta, count);
            } else if ((id < 256 && id != 166) || id > 8000) {
                if (meta >= 0) {
                    item = new ItemBlock(Block.get(id, meta), meta, count);
                } else {
                    item = new ItemBlock(Block.get(id), meta, count);
                }
            } else if (id > 2000) {
                item = ((Item) c.getConstructor(int.class, Integer.class, int.class).newInstance(id, meta, count));
            } else {
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

    private static final Pattern ITEM_STRING_PATTERN = Pattern.compile(
            // 1:namespace 2:name 3:damage 4:num-id 5:damage
            "^(?:(?:([a-z_]\\w*):)?([a-z._]\\w*)(?::(-?\\d+))?|(-?\\d+)(?::(-?\\d+))?)$");

    @Inject
    public static Item fromString(String str) {
        String normalized = str.trim().replace(' ', '_').toLowerCase();
        Matcher matcher = ITEM_STRING_PATTERN.matcher(normalized);
        if (!matcher.matches()) {
            return Item.AIR_ITEM.clone();
        }

        String name = matcher.group(2);
        OptionalInt meta = OptionalInt.empty();
        String metaGroup;
        if (name != null) {
            metaGroup = matcher.group(3);
        } else {
            metaGroup = matcher.group(5);
        }
        if (metaGroup != null) {
            meta = OptionalInt.of(Short.parseShort(metaGroup));
        }

        String numericIdGroup = matcher.group(4);
        if (name != null) {
            String namespaceGroup = matcher.group(1);
            String namespacedId;
            if (namespaceGroup != null) {
                namespacedId = namespaceGroup + ":" + name;
            } else {
                namespacedId = "minecraft:" + name;
            }
            if ("minecraft:air".equals(namespacedId)) {
                return Item.AIR_ITEM.clone();
            }

            Supplier<Item> constructor = Item.NAMESPACED_ID_ITEM.get(namespacedId);
            if (constructor != null) {
                try {
                    return constructor.get();
                } catch (Exception e) {
                    System.out.printf("Could not create a new instance of {} using the namespaced id {}", constructor,
                            namespacedId, e);
                }
            }

            // common item
            int id = RuntimeItems.getLegacyIdFromLegacyString(namespacedId);
            if (id > 0) {
                return Item.get(id, meta.orElse(0));
            } else if (namespaceGroup != null && !namespaceGroup.equals("minecraft:")) {
                return Item.AIR_ITEM.clone();
            }
        } else if (numericIdGroup != null) {
            int id = Integer.parseInt(numericIdGroup);
            return Item.get(id, meta.orElse(0));
        }

        if (name == null) {
            return Item.AIR_ITEM.clone();
        }

        int id = 0;
        try {
            id = BlockID.class.getField(name.toUpperCase()).getInt(null);
            if (id > 255) {
                id = 255 - id;
            }
        } catch (Exception ignore) {
            try {
                id = ItemID.class.getField(name.toUpperCase()).getInt(null);
            } catch (Exception ignore1) {
                try {
                    id = IDRegistry.getIdByNameId(name);
                } catch (Exception e) {
                }
            }
        }
        return Item.get(id, meta.orElse(0));
    }

    @Inject
    public static void initCreativeItems(){
        CustomItem.init();
    }

    @Inject(signature = "()Ljava/lang/Short;", type = TypeHook.BEFORE)
    public static void getFuelTime(HookController controller) {
        short fuel = NativeFurnaceRegistry.getBurnTime(controller.getSelf());
        if (fuel > 0) {
            controller.setReplace(true);
            controller.setResult(Short.valueOf(fuel));
        }
    }

    public static final String INNER_CORE_TAG_NAME = "$mod";

    public static Item get(int id, int count, int meta, NativeItemInstanceExtra extra) {
        NukkitIdConvertor.EntryItem entry = NukkitIdConvertor.getNukkitForInnerCore(id, meta);
        Item item = Item.get(entry.id, entry.data, count, new byte[] {});
        if (extra != null) {
            CompoundTag tag = item.getOrCreateNamedTag();

            JSONObject custom = extra.getCustomDataJSON();
            if (custom != null)
                tag.putString(INNER_CORE_TAG_NAME, custom.toString());

            item.setCompoundTag(tag);

            ItemExtraDataProvider provider = (ItemExtraDataProvider) items_pointers.getInstance(extra.getValue());
            provider.apply(item);
            items_pointers.replace(items_pointers.getPointerForInstance(item), provider);
        }
        return item;
    }

    public static Item get(int id, int count, int meta, long extra) {
        ItemExtraDataProvider provider = (ItemExtraDataProvider) items_pointers.getInstance(extra);
        return get(id, count, meta, provider == null ? null : provider.extra);
    }

    public static NativeItemInstanceExtra getItemInstanceExtra(Item item) {
        CompoundTag tag = item.getOrCreateNamedTag();
        String custom = tag.getString(INNER_CORE_TAG_NAME);
        if (!custom.equals("")) {
            NativeItemInstanceExtra extra = new NativeItemInstanceExtra();
            extra.getExtraProvider().apply(item);
            extra.setAllCustomData(custom);
            return extra;
        }
        return null;
    }

    public static void removePointer(long ptr) {
        items_pointers.removePointer(ptr);
    }

    @Inject(className = "cn.nukkit.inventory.transaction.RepairItemTransaction", type = TypeHook.BEFORE, signature = "()Z")
    public static void isMapRecipe(HookController controller, RepairItemTransaction self) {
        ArrayList<Integer> repairs = Repairs.getRepairs(self.getInputItem().getId());
        if (repairs != null)
            controller.setResult(repairs.contains(self.getOutputItem().getId()));
        controller.setReplace(false);
    }

    @Inject
    public static void addCreativeItem(int protocol, Item item){
        if(protocol == 407){
            int id = item.getId();
            int damage = item.getDamage();
            if(id == BlockID.PLANKS && damage == 0)
                CustomItem.addCreativeItemsBuild();
            else if(id == ItemID.ARROW && damage == 0)
                CustomItem.addCreativeItemsWeapons();
            else if(id == ItemID.STICK)
                CustomItem.addCreativeItems();
            else if(id == BlockID.BLOCK_NETHER_WART_BLOCK)
                CustomItem.addCreativeItemsNature();
        }
    }
}
